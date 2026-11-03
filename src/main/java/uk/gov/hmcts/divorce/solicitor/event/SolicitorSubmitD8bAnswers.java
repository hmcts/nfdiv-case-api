package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD8bPage1;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD8bPage2;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AOS_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorSubmitD8bAnswers implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_D8B_ANSWER = "solicitor-d8b-answer";

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = addEventConfig(configBuilder);

        List<CcdPageConfiguration> pages = List.of(
                new GeneralApplicationD8bPage1(),
                new GeneralApplicationD8bPage2()
        );
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_D8B_ANSWER, details.getId());

        final CaseData caseData = details.getData();
        InterimApplicationOptions options = caseData.getApplicant2().getInterimApplicationOptions();
        options.setInterimApplicationType(InterimApplicationType.SOLICITOR_D8B_ANSWERS);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
                .event(SOLICITOR_D8B_ANSWER)
                .forStates(AOS_STATES)
                .name("D8b Answer")
                .description("D8b Answer")
                .showSummary()
                .showEventNotes()
                .showCondition("howToRespondApplication=\"disputeDivorce\"")
                .aboutToSubmitCallback(this::aboutToSubmit)
                .endButtonLabel("Save General Application")
                .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
