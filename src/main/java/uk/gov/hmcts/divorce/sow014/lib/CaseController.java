package uk.gov.hmcts.divorce.sow014.lib;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.runtime.CallbackController;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping(path = "/ccd")
public class CaseController {

    @Autowired
    private JdbcTemplate db;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private CallbackController runtime;

    @Autowired
    private CallbackEnumerator callbackEnumerator;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private HttpServletRequest request;


    @GetMapping(
            value = "/cases/{caseRef}",
            produces = "application/json"
    )
    @SneakyThrows
    public Map<String, Object> getCase(@PathVariable("caseRef") long caseRef) {
        var result = db.queryForMap(
                """
                        select
                              reference as id,
                              -- Format timestamp in iso 8601
                              to_json(c.created_date)#>>'{}' as created_date,
                              jurisdiction,
                              case_type_id,
                              state,
                              data::text as case_data,
                              '{}'::jsonb as data_classification,
                              security_classification,
                              version,
                              to_json(last_state_modified_date)#>>'{}' as last_state_modified_date,
                              to_json(coalesce(last_event.created_date, c.created_date))#>>'{}' as last_modified,
                              supplementary_data::text
                         from case_data c
                                 left join lateral (
                                  select created_date from case_event
                                  where case_reference = c.reference
                                  order by id desc limit 1
                                ) last_event on true
                         where reference = ?
                        """, caseRef);
        result.put("case_data", caseRepository.getCase(caseRef, (ObjectNode) mapper.readTree((String) result.get("case_data"))));
        return result;
    }

    @SneakyThrows
    @PostMapping("/cases")
    public ResponseEntity<Map<String, Object>> createEvent(
            @RequestBody POCCaseEvent event,
            @RequestHeader HttpHeaders headers) {
        log.info("case Details: {}", event);

        transactionTemplate.execute(status -> {
            dispatchAboutToSubmit(event);
            var id = saveCaseReturningAuditId(event);
            if (callbackEnumerator.hasSubmittedCallbackForEvent(event.getEventDetails().getEventId())) {
                enqueueSubmittedCallback(id, event, headers);
            }
            return status;
        });

        var response = getCase((Long) event.getCaseDetails().get("id"));
        log.info("case response: {}", response);
        return ResponseEntity.ok(response);
    }

    @SneakyThrows
    private void enqueueSubmittedCallback(long auditEventId, POCCaseEvent event, HttpHeaders headers) {
        var req = CallbackRequest.builder()
                .caseDetails(toCaseDetails(event.getCaseDetails()))
                .caseDetailsBefore(toCaseDetails(event.getCaseDetailsBefore()))
                .eventId(event.getEventDetails().getEventId())
                .build();

        db.update(
                """
                        insert into ccd.submitted_callback_queue (case_event_id, event_id, payload, headers)
                        values (?, ?, ?::jsonb, ?::jsonb)
                        """,
                auditEventId,
                event.getEventDetails().getEventId(),
                mapper.writeValueAsString(req),
                mapper.writeValueAsString(headers.toSingleValueMap())
        );
    }

    @SneakyThrows
    private long saveCaseReturningAuditId(POCCaseEvent event) {
        Map<String, Object> caseDetails = event.getCaseDetails();
        var state = event.getEventDetails().getStateId() != null
                ? event.getEventDetails().getStateId()
                : caseDetails.get("state");
        int version = (int) Optional.ofNullable(event.getCaseDetails().get("version")).orElse(1);
        // Upsert the case - create if it doesn't exist, update if it does.
        var rowsAffected = db.update("""
                        insert into case_data (last_modified, jurisdiction, case_type_id, state, data, data_classification, reference, security_classification, version)
                        -- TODO: separate private data model from public view so we don't duplicate eg. notes in the json
                        values (now(), ?, ?, ?, (?::jsonb - 'notes' - 'markdownTabField'), ?::jsonb, ?, ?::securityclassification, ?)
                        on conflict (reference)
                        do update set
                            state = excluded.state,
                            data = excluded.data,
                            data_classification = excluded.data_classification,
                            security_classification = excluded.security_classification,
                            last_modified = now(),
                            version = case
                                        when case_data.data is distinct from excluded.data then case_data.version + 1
                                        else case_data.version
                                      end
                            WHERE case_data.version = EXCLUDED.version;
                        """,
                "DIVORCE",
                "NFD-4154",
                state,
                mapper.writeValueAsString(caseDetails.get("case_data")),
                mapper.writeValueAsString(caseDetails.get("data_classification")),
                caseDetails.get("id"),
                caseDetails.get("security_classification"),
                version
        );
        if (rowsAffected != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Case was updated concurrently");
        }

        return saveAuditRecord(event, 1);
    }

    @SneakyThrows
    private POCCaseEvent dispatchAboutToSubmit(POCCaseEvent event) {
        if (callbackEnumerator.hasAboutToSubmitCallbackForEvent(event.getEventDetails().getEventId())) {
            var req = CallbackRequest.builder()
                    .caseDetails(toCaseDetails(event.getCaseDetails()))
                    .caseDetailsBefore(toCaseDetails(event.getCaseDetailsBefore()))
                    .eventId(event.getEventDetails().getEventId())
                    .build();
            var cb = runtime.aboutToSubmit(req);

            event.getCaseDetails().put("case_data", mapper.readValue(mapper.writeValueAsString(cb.getData()), Map.class));
            if (cb.getState() != null) {
                event.getEventDetails().setStateId(cb.getState().toString());
            }
        }
        return event;
    }

    @GetMapping(
            value = "/cases/{caseRef}/history",
            produces = "application/json"
    )
    public String loadHistory(@PathVariable("caseRef") long caseRef) {
        return db.queryForObject(
                """
                         select jsonb_agg(to_jsonb(e) - 'case_reference' - 'event_id'
                         || jsonb_build_object('case_data_id', case_reference)
                         || jsonb_build_object('event_instance_id', id)
                         || jsonb_build_object('id', event_id)
                          order by id desc)
                         from case_event e
                         where case_reference = ?
                        """,
                new Object[]{caseRef}, String.class);
    }

    @SneakyThrows
    private long saveAuditRecord(POCCaseEvent details, int version) {
        var event = details.getEventDetails();
        var currentView = getCase((Long) details.getCaseDetails().get("id"));
        var result = db.queryForMap(
                """
                        insert into case_event (
                          data,
                          data_classification,
                          event_id,
                          user_id,
                          case_reference,
                          case_type_id,
                          case_type_version,
                          state_id,
                          user_first_name,
                          user_last_name,
                          event_name,
                          state_name,
                          summary,
                          description,
                          security_classification)
                        values (?::jsonb,?::jsonb,?,?,?,?,?,?,?,?,?,?,?,?,?::securityclassification)
                        returning id
                        """,
                mapper.writeValueAsString(currentView.get("case_data")),
                mapper.writeValueAsString(currentView.get("data_classification")),
                event.getEventId(),
                "user-id",
                currentView.get("id"),
                "NFD-4154",
                version,
                currentView.get("state"),
                "a-first-name",
                "a-last-name",
                event.getEventName(),
                event.getStateName(),
                event.getSummary(),
                event.getDescription(),
                currentView.get("security_classification")
        );
        return (long) result.get("id");
    }

    @SneakyThrows
    private CaseDetails toCaseDetails(Map<String, Object> data) {
        if (data == null) {
            return null;
        }
        return mapper.readValue(mapper.writeValueAsString(data), CaseDetails.class);
    }
}
