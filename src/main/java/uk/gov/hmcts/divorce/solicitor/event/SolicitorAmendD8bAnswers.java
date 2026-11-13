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
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.AmendGeneralApplicationActionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD8bPage1;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD8bPage2;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftSubmissionService;

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
public class SolicitorAmendD8bAnswers implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_AMEND_D8B_ANSWERS = "solicitor-amend-d8b-answers";

    private static final String AMEND_SHOW_CONDITION = "applicant2DraftApplicationAction=\"amend\"";

    private final GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        new AmendGeneralApplicationActionPage(CaseData::getApplicant2, "D8b Answer").addTo(pageBuilder);

        List<CcdPageConfiguration> pages = List.of(
            new GeneralApplicationD8bPage1(),
            new GeneralApplicationD8bPage2()
        );

        pages.forEach(page -> page.addWithShowCondition(pageBuilder, AMEND_SHOW_CONDITION));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_AMEND_D8B_ANSWERS, details.getId());

        final CaseData caseData = details.getData();

        generalApplicationDraftSubmissionService.buildGeneralApplication(details, caseData, caseData.getApplicant2());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_AMEND_D8B_ANSWERS)
            .forStates(AOS_STATES)
            .name("Amend General Application")
            .description("Amend General Application")
            .showSummary()
            .showEventNotes()
            .showCondition("applicant2InterimApplicationType=\"solicitorD8BAnswers\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Submit")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
