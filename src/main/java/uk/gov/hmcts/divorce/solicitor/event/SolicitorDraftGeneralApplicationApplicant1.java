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
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page1;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page2;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page3;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page4;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page5;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page6;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page7;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page8;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationD11Page9;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftJourneyService;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorDraftGeneralApplicationApplicant1 implements CCDConfig<CaseData, State, UserRole> {

    private final GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    public static final String SOLICITOR_DRAFT_GEN_APP_APPLICANT1 = "solicitor-draft-general-application-app1";

    private static final String DRAFT_GENERAL_APPLICATION = "Draft General Application";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            new GeneralApplicationD11Page1(CaseData::getApplicant1), new GeneralApplicationD11Page2(CaseData::getApplicant1,
                "applicant1"), new GeneralApplicationD11Page3(CaseData::getApplicant1, "applicant1"),
            new GeneralApplicationD11Page4(CaseData::getApplicant1), new GeneralApplicationD11Page5(CaseData::getApplicant1,
                "applicant1"), new GeneralApplicationD11Page6("applicant1"),
            new GeneralApplicationD11Page7("applicant1"), new GeneralApplicationD11Page8(CaseData::getApplicant1,
                CaseData::getApplicant2), new GeneralApplicationD11Page9("applicant1")
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", SOLICITOR_DRAFT_GEN_APP_APPLICANT1, details.getId());
        final CaseData data = details.getData();

        generalApplicationDraftJourneyService.prepareAboutToStart(details, data.getApplicant1());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_DRAFT_GEN_APP_APPLICANT1, details.getId());
        final CaseData data = details.getData();

        generalApplicationDraftJourneyService.prepareAboutToSubmit(details, data.getApplicant1());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(SOLICITOR_DRAFT_GEN_APP_APPLICANT1)
            .forStates(POST_SUBMISSION_STATES)
            .name(DRAFT_GENERAL_APPLICATION)
            .description(DRAFT_GENERAL_APPLICATION)
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .endButtonLabel("Submit Application")
            .grant(CREATE_READ_UPDATE_DELETE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
