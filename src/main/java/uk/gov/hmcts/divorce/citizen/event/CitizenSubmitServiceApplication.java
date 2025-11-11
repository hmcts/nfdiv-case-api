package uk.gov.hmcts.divorce.citizen.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateAosSubmitted;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenSubmitServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SERVICE_APPLICATION = "citizen-service-application";

    public static final String AWAITING_DECISION_ERROR = """
        A service application has already been submitted and is awaiting a decision.
        """;

    private final PaymentSetupService paymentSetupService;

    private final InterimApplicationSubmissionService interimApplicationSubmissionService;

    private final DocumentRemovalService documentRemovalService;

    private final Clock clock;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_SERVICE_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Citizen service application")
            .description("Citizen service application")
            .showSummary()
            .showEventNotes()
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, LEGAL_ADVISOR, APPLICANT_1_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        long caseId = details.getId();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_SERVICE_APPLICATION, details.getId());

        if (serviceAppAwaitingDecision(data.getAlternativeService())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(AWAITING_DECISION_ERROR))
                .build();
        }

        List<String> errors = validateAosSubmitted(data);
        if (!errors.isEmpty()) {
            log.info("{} failed since partner has already responded for {} ", CITIZEN_SERVICE_APPLICATION, caseId);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(errors)
                .build();
        }

        Applicant applicant = data.getApplicant1();
        InterimApplicationOptions userOptions = applicant.getInterimApplicationOptions();
        AlternativeService newServiceApplication = buildServiceApplication(userOptions);
        data.setAlternativeService(newServiceApplication);

        FeeDetails serviceFee = newServiceApplication.getServicePaymentFee();
        if (userOptions.willMakePayment()) {
            serviceFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_CARD);
            serviceFee.setHasCompletedOnlinePayment(YesOrNo.NO);
            prepareCaseForServicePayment(newServiceApplication, applicant, caseId);

            details.setState(AwaitingServicePayment);
        } else {
            serviceFee.setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);
            serviceFee.setHelpWithFeesReferenceNumber(userOptions.getInterimAppsHwfRefNumber());

            details.setState(userOptions.awaitingDocuments() ? AwaitingDocuments : AwaitingServicePayment);
        }

        DivorceDocument applicationDocument = interimApplicationSubmissionService.generateServiceApplicationAnswerDocument(
            caseId, applicant, data
        );
        newServiceApplication.setServiceApplicationAnswers(applicationDocument);

        applicant.archiveInterimApplicationOptions();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_SERVICE_APPLICATION, details.getId());

        CaseData data = details.getData();
        AlternativeService alternativeService = data.getAlternativeService();
        ServicePaymentMethod paymentMethod = alternativeService.getServicePaymentFee().getPaymentMethod();

        if (ServicePaymentMethod.FEE_PAY_BY_HWF.equals(paymentMethod)) {
            interimApplicationSubmissionService.sendServiceApplicationNotifications(
                details.getId(), alternativeService.getAlternativeServiceType(), data
            );
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private AlternativeService buildServiceApplication(InterimApplicationOptions userOptions) {
        boolean evidenceNotSubmitted = YesOrNo.NO.equals(userOptions.getInterimAppsCanUploadEvidence())
            && userOptions.getInterimAppsEvidenceDocs() != null;

        if (evidenceNotSubmitted && !CollectionUtils.isEmpty(userOptions.getInterimAppsEvidenceDocs())) {
            documentRemovalService.deleteDocument(userOptions.getInterimAppsEvidenceDocs());
        }

        return AlternativeService.builder()
            .serviceApplicationDocuments(userOptions.getInterimAppsEvidenceDocs())
            .receivedServiceApplicationDate(LocalDate.now(clock))
            .receivedServiceAddedDate(LocalDate.now(clock))
            .alternativeServiceType(userOptions.getInterimApplicationType().getServiceType())
            .serviceApplicationDocsUploadedPreSubmission(userOptions.awaitingDocuments() ? YesOrNo.NO : YesOrNo.YES)
            .serviceApplicationSubmittedOnline(YesOrNo.YES)
            .serviceApplicationDocuments(evidenceNotSubmitted ? null : userOptions.getInterimAppsEvidenceDocs())
            .alternativeServiceFeeRequired(YesOrNo.YES)
            .build();
    }

    private void prepareCaseForServicePayment(AlternativeService serviceApplication, Applicant applicant, long caseId) {
        FeeDetails serviceFee = serviceApplication.getServicePaymentFee();
        OrderSummary orderSummary = paymentSetupService.createServiceApplicationOrderSummary(
            serviceApplication, caseId
        );
        serviceFee.setOrderSummary(orderSummary);

        String serviceRequest = paymentSetupService.createServiceApplicationPaymentServiceRequest(
            serviceApplication, caseId, applicant.getFullName()
        );
        serviceFee.setServiceRequestReference(serviceRequest);
    }

    private boolean serviceAppAwaitingDecision(AlternativeService alternativeService) {
        return alternativeService != null && alternativeService.getAlternativeServiceType() != null;
    }
}
