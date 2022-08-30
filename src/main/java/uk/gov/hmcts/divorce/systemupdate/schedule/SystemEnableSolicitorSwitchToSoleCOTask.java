package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemEnableSolicitorSwitchToSoleCO.SYSTEM_ENABLE_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;

@Component
@Slf4j
public class SystemEnableSolicitorSwitchToSoleCOTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public void run() {

        log.info("Remind applicant 2 scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        try {
            final BoolQueryBuilder query =
                boolQuery()
                    .must(matchQuery(STATE, ConditionalOrderPending));

            final List<CaseDetails> casesInConditionalOrderPending =
                ccdSearchService.searchForAllCasesWithQuery(query, user, serviceAuthorization, ConditionalOrderPending);

            for (final CaseDetails caseDetails : casesInConditionalOrderPending) {
                try {
                    final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

                    if (shouldEnableSolicitorSwitchToSoleCo(caseData.getConditionalOrder())) {
                        log.info("Enabling Solicitor Switch to Sole CO for Case id {}", caseDetails.getId());
                        ccdUpdateService.submitEvent(caseDetails, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO, user, serviceAuthorization);
                    }
                } catch (final CcdManagementException e) {
                    log.error("Submit event failed for case id: {}, continuing to next case", caseDetails.getId());
                } catch (final IllegalArgumentException e) {
                    log.error("Deserialization failed for case id: {}, continuing to next case", caseDetails.getId());
                }
            }

        } catch (final CcdSearchCaseException e) {
            log.error("Enable Solicitor Switch to Sole CO schedule task stopped after search error", e);
        } catch (final CcdConflictException e) {
            log.info("Enable Solicitor Switch to Sole CO scheduled task stopping"
                + " due to conflict with another running Enable Solicitor Switch to Sole CO task"
            );
        }
    }

    private boolean shouldEnableSolicitorSwitchToSoleCo(ConditionalOrder conditionalOrder) {
        return conditionalOrder.shouldEnableSwitchToSoleCoForApplicant1Solicitor()
            || conditionalOrder.shouldEnableSwitchToSoleCoForApplicant2Solicitor();
    }
}
