package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.InterimApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralApplicationReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenGeneralApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_GENERAL_APPLICATION = "citizen-general-application";

    public static final String AWAITING_DECISION_ERROR = """
        A general application has already been submitted and is awaiting a decision.
        """;

    private final PaymentSetupService paymentSetupService;

    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    private final DocumentRemovalService documentRemovalService;

    private final Clock clock;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest request;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_GENERAL_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Citizen general application")
            .description("Citizen general application")
            .showSummary()
            .showEventNotes()
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, APPLICANT_1_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        long caseId = details.getId();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION, details.getId());

        if (generalAppAwaitingDecision(data.getAlternativeService())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(AWAITING_DECISION_ERROR))
                .build();
        }

        Applicant applicant = isApplicant1(caseId) ? data.getApplicant1() : data.getApplicant2();
        InterimApplicationOptions userOptions = applicant.getInterimApplicationOptions();
        GeneralApplication newGeneralApplication = buildGeneralApplication(userOptions);
        data.updateCaseWithGeneralApplication(newGeneralApplication);
        FeeDetails applicationFee = newGeneralApplication.getGeneralApplicationFee();

        if (userOptions.willMakePayment()) {
            applicationFee.setPaymentMethod(ServicePaymentMethod.CARD);
            prepareCaseForGeneralApplicationPayment(newGeneralApplication, applicant, caseId);

            details.setState(AwaitingServicePayment);
        } else {
            applicationFee.setHelpWithFeesReferenceNumber(userOptions.getInterimAppsHwfRefNumber());
            applicationFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);

            details.setState(userOptions.awaitingDocuments() ? AwaitingDocuments : GeneralApplicationReceived);
        }

        DivorceDocument applicationDocument = interimApplicationSubmissionService.generateAnswerDocument(
            caseId, applicant, data
        );
        newGeneralApplication.setGeneralApplicationDocument(applicationDocument);

        applicant.setInterimApplicationOptions(new InterimApplicationOptions());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_GENERAL_APPLICATION, details.getId());

        CaseData data = details.getData();
        AlternativeService alternativeService = data.getAlternativeService();

        if (!YesOrNo.YES.equals(alternativeService.getAlternativeServiceFeeRequired())) {
            interimApplicationSubmissionService.sendNotifications(details.getId(), alternativeService.getAlternativeServiceType(), data);
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private GeneralApplication buildGeneralApplication(InterimApplicationOptions userOptions) {
        boolean evidenceNotSubmitted = YesOrNo.NO.equals(userOptions.getInterimAppsCanUploadEvidence())
            && userOptions.getInterimAppsEvidenceDocs() != null;

        if (evidenceNotSubmitted && !CollectionUtils.isEmpty(userOptions.getInterimAppsEvidenceDocs())) {
            documentRemovalService.deleteDocument(userOptions.getInterimAppsEvidenceDocs());
        }

        return GeneralApplication.builder()
            .generalApplicationDocuments(evidenceNotSubmitted ? null : userOptions.getInterimAppsEvidenceDocs())
            .receivedGeneralApplicationDate(LocalDate.now(clock))
            .generalApplicationType(userOptions.getInterimApplicationType().getGeneralApplicationType())
            .generalApplicationDocsUploadedPreSubmission(userOptions.awaitingDocuments() ? YesOrNo.NO : YesOrNo.YES)
            .generalApplicationSubmittedOnline(YesOrNo.YES)
            .build();
    }

    private void prepareCaseForGeneralApplicationPayment(GeneralApplication generalApplication, Applicant applicant, long caseId) {
        FeeDetails serviceFee = generalApplication.getGeneralApplicationFee();
        OrderSummary orderSummary = paymentSetupService.createGeneralApplicationOrderSummary(
            generalApplication, caseId
        );
        serviceFee.setOrderSummary(orderSummary);

        String serviceRequest = paymentSetupService.createGeneralApplicationPaymentServiceRequest(
            generalApplication, caseId, applicant.getFullName()
        );
        serviceFee.setServiceRequestReference(serviceRequest);

    }

    private boolean generalAppAwaitingDecision(AlternativeService alternativeService) {
        return alternativeService != null && alternativeService.getAlternativeServiceType() != null;
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }
}
