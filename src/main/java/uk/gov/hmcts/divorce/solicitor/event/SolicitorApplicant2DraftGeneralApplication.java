package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Pages;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftJourneyService;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.ALWAYS_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GENERAL_APPLICATION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorApplicant2DraftGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    private final GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    public static final String SOLICITOR_APPLICANT2_DRAFT_GEN_APP = "solicitor-app2-draft-general-application";

    private static final String DRAFT_GENERAL_APPLICATION = "Draft General Application";

    private static final String APPLICANT_2 = "applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        GeneralApplicationD11Pages.addPages(
            pageBuilder,
            CaseData::getApplicant2,
            CaseData::getApplicant1,
            APPLICANT_2,
            ALWAYS_SHOW
        );
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} about to start callback invoked for Case Id: {}", SOLICITOR_APPLICANT2_DRAFT_GEN_APP, details.getId());

        final CaseData data = details.getData();
        generalApplicationDraftJourneyService.prepareAboutToStart(details, data.getApplicant2());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_APPLICANT2_DRAFT_GEN_APP, details.getId());
        final CaseData data = details.getData();

        generalApplicationDraftJourneyService.prepareAboutToSubmit(details, data.getApplicant2());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_APPLICANT2_DRAFT_GEN_APP)
            .forStates(GENERAL_APPLICATION_STATES)
            .name(DRAFT_GENERAL_APPLICATION)
            .description(DRAFT_GENERAL_APPLICATION)
            .showSummary()
            .showEventNotes()
            .showCondition("applicant2InterimApplicationType!=\"digitisedGeneralApplicationD11\"")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Save Application")
            .grant(CREATE_READ_UPDATE_DELETE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
