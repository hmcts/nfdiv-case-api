package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.event.page.AmendCase;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateMarriageDate;

@Slf4j
@RequiredArgsConstructor
@Component
public class CaseworkerAmendCase implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_AMEND_CASE = "caseworker-amend-case";
    private final CcdPageConfiguration amendCase = new AmendCase();

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var pageBuilder = addEventConfig(configBuilder);
        amendCase.addTo(pageBuilder);
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_AMEND_CASE)
            .forStates(POST_SUBMISSION_STATES)
            .name("Update case")
            .description("Update case")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE,
                CASE_WORKER)
            .grantHistoryOnly(
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_AMEND_CASE, details.getId());
        CaseData caseData = details.getData();

        log.info("Validating Marriage Date for Case Id: {}", details.getId());
        final List<String> caseValidationErrors = validateMarriageDate(caseData, "MarriageDate", true);

        if (!isEmpty(caseValidationErrors)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(caseValidationErrors)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
