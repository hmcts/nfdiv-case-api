package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.AmendGeneralApplicationActionPage;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Pages;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftSubmissionService;

import static uk.gov.hmcts.divorce.divorcecase.model.State.GENERAL_APPLICATION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorApplicant2AmendGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_APPLICANT2_AMEND_GEN_APP = "solicitor-app2-amend-gen-app";

    private final GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    private static final String AMEND_SHOW_CONDITION = "applicant2DraftApplicationAction=\"amend\"";

    private static final String APPLICANT_2 = "applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        new AmendGeneralApplicationActionPage(CaseData::getApplicant2).addTo(pageBuilder);

        GeneralApplicationD11Pages.addPages(
            pageBuilder,
            CaseData::getApplicant2,
            CaseData::getApplicant1,
            APPLICANT_2,
            AMEND_SHOW_CONDITION
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_APPLICANT2_AMEND_GEN_APP, details.getId());

        final CaseData caseData = details.getData();
        final Applicant applicant = caseData.getApplicant2();

        generalApplicationDraftSubmissionService.buildGeneralApplication(details, caseData, applicant);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_APPLICANT2_AMEND_GEN_APP)
            .forStates(GENERAL_APPLICATION_STATES)
            .name("Amend General Application")
            .description("Amend General Application")
            .showSummary()
            .showEventNotes()
            .showCondition("applicant2InterimApplicationType=\"digitisedGeneralApplicationD11\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Submit")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
