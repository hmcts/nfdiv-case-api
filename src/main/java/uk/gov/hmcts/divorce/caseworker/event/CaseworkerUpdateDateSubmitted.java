package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CaseworkerUpdateDateSubmitted implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_DATE_SUBMITTED = "caseworker-update-date-submitted";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_DATE_SUBMITTED)
            .forAllStates()
            .name("Update Date Submitted")
            .description("Update Date Submitted")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                SUPER_USER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit))
            .page("updateDateSubmitted")
            .complex(CaseData::getApplication)
                .mandatory(Application::getDateSubmitted)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Callback invoked for {}, Case Id: {}", CASEWORKER_UPDATE_DATE_SUBMITTED, details.getId());

        CaseData caseData = details.getData();

        if (null == caseData.getDueDate()) {
            LocalDate dueDate = caseData.getApplication().getDateOfSubmissionResponse();
            log.info("dueDate is Null for Case Id: {}, Setting dueDate to {}", details.getId(), dueDate);
            caseData.setDueDate(dueDate);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
