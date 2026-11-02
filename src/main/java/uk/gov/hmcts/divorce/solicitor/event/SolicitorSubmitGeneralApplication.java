package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.citizen.notification.interimapplications.GeneralApplicationD11SubmittedNotification;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationPaymentSummaryPage;
import uk.gov.hmcts.divorce.solicitor.event.page.GeneralApplicationStatementOfTruthPage;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftSubmissionService;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationSubmitPaymentService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorSubmitGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT_GENERAL_APPLICATION = "solicitor-submit-general-application";

    private final GeneralApplicationSubmitPaymentService submitPaymentService;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    private final GeneralApplicationD11SubmittedNotification generalApplicationD11SubmittedNotification;

    private final GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = addEventConfig(configBuilder);

        List<CcdPageConfiguration> pages = List.of(
                new GeneralApplicationStatementOfTruthPage(),
                new GeneralApplicationPaymentSummaryPage()
        );
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        Applicant actingApplicant = isApplicant1(details.getId()) ? caseData.getApplicant1() : caseData.getApplicant2();

        OptionalInt activeIndex = generalApplicationDraftSubmissionService.findDraftIndex(caseData, actingApplicant);

        if (activeIndex.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(caseData)
                    .errors(singletonList("No active draft general application found for the acting applicant"))
                    .build();
        }

        caseData.setGeneralApplication(caseData.getGeneralApplications().get(activeIndex.getAsInt()).getValue());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_SUBMIT_GENERAL_APPLICATION, details.getId());

        CaseData caseData = details.getData();

        Applicant currentApplicant = isApplicant1(details.getId()) ? caseData.getApplicant1() : caseData.getApplicant2();

        Optional<String> paymentError = submitPaymentService.processSubmitPayment(details.getId(), caseData, currentApplicant);
        if (paymentError.isPresent()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(caseData)
                    .errors(singletonList(paymentError.get()))
                    .build();
        }

        GeneralApplication submittedGa = caseData.getGeneralApplication();

        OptionalInt activeIndex = generalApplicationDraftSubmissionService.findDraftIndex(caseData, currentApplicant);
        List<ListValue<GeneralApplication>> generalApplications = caseData.getGeneralApplications();

        if (activeIndex.isPresent() && generalApplications != null) {
            generalApplications.get(activeIndex.getAsInt()).setValue(submittedGa);
        }
        currentApplicant.setActiveGeneralApplication(null);
        currentApplicant.archiveInterimApplicationOptions();

        ServicePaymentMethod paymentMethod = caseData.getGeneralApplication().getGeneralApplicationFee().getPaymentMethod();
        State targetState = ServicePaymentMethod.FEE_PAY_BY_ACCOUNT.equals(paymentMethod)
                ? State.GeneralApplicationReceived
                : State.AwaitingGeneralReferralPayment;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(targetState)
                .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} submitted callback invoked for Case Id: {}", SOLICITOR_SUBMIT_GENERAL_APPLICATION, details.getId());

        CaseData caseData = details.getData();

        var generalApps = new ArrayList<>(
                Optional.ofNullable(caseData.getGeneralApplications())
                        .orElseGet(Collections::emptyList)
        );

        GeneralApplication newGeneralApplication = generalApps.getFirst().getValue();

        boolean isApplicant1 = isApplicant1(details.getId());
        generalApplicationD11SubmittedNotification.sendToSolicitor(
                caseData,
                details.getId(),
                isApplicant1,
                newGeneralApplication
        );

        return SubmittedCallbackResponse.builder().build();
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
                .event(SOLICITOR_SUBMIT_GENERAL_APPLICATION)
                .forStates(State.GENERAL_APPLICATION_STATES)
                .name("Submit General Application")
                .description("Submit General Application")
                .showSummary()
                .showEventNotes()
                .showCondition("applicant1InterimApplicationType=\"digitisedGeneralApplicationD11\" "
                        + "OR applicant2InterimApplicationType=\"digitisedGeneralApplicationD11\"")
                .endButtonLabel("Submit Application")
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }
}
