package uk.gov.hmcts.divorce.sow014.nfd;

import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.sow014.lib.DynamicRadioListElement;
import uk.gov.hmcts.divorce.sow014.lib.MyRadioList;

import java.util.ArrayList;

import static org.jooq.nfdiv.ccd.Tables.FAILED_JOBS;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class RetryCallback implements CCDConfig<CaseData, State, UserRole> {
    @Autowired
    private DSLContext db;

    @Autowired
    @Lazy
    private JdbcTemplate jdbcTemplate;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event("retry-submitted-callback")
            .forAllStates()
            .name("Retry submitted callback")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE,
                SUPER_USER))
            .page("retryFailedCallback")
            .pageLabel("Choose the job to retry")
            .mandatory(CaseData::getCallbackJobs)
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var choices = new ArrayList<DynamicRadioListElement>();
        // Search subcases by applicantFirstName
        db.selectFrom(FAILED_JOBS)
            .where(FAILED_JOBS.REFERENCE.eq(details.getId())
            )
            .fetch()
            .forEach(fetch -> choices.add(
                DynamicRadioListElement.builder()
                    .code(fetch.getJobId().toString())
                    .label(fetch.getEventId() + " - " + fetch.getExceptionMessage().substring(0, 10))
                    .build()));

        MyRadioList radioList = MyRadioList.builder()
            .value(choices.get(0))
            .listItems(choices)
            .build();

        details.getData().setCallbackJobs(radioList);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        var choice = details.getData().getCallbackJobs().getValue();

        jdbcTemplate.update("""
            update ccd.submitted_callback_queue
             set attempted_at = null
             where id = ?
            """, Long.valueOf(choice.getCode()));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}

