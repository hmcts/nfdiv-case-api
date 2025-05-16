package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationAnswers;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.InterimApplicationGeneratorService;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
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
public class CitizenSubmitServiceApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_SERVICE_APPLICATION = "citizen-service-application";

    private static final String AWAITING_DECISION_ERROR = """
        A service application has already been submitted and is awaiting a decision.
    """;

    private final PaymentSetupService paymentSetupService;

    private final InterimApplicationGeneratorService interimApplicationGeneratorService;

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
//            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR, CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grantHistoryOnly(SUPER_USER, JUDGE, APPLICANT_1_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        CaseData data = details.getData();
        long caseId = details.getId();
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_SERVICE_APPLICATION, details.getId());

        data.setAlternativeService(null);
        AlternativeService alternativeService = data.getAlternativeService();
        if (alternativeService != null && alternativeService.getReceivedServiceApplicationDate() != null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(AWAITING_DECISION_ERROR))
                .build();
        }

        Applicant applicant = data.getApplicant1();
        GeneralApplicationOptions userOptions = applicant.getGeneralApplicationOptions();
        ApplicationAnswers applicationAnswers = userOptions.getApplicationAnswers();

        AlternativeService newServiceApplication = createServiceApplication(userOptions, applicationAnswers);
        data.setAlternativeService(newServiceApplication);
        newServiceApplication.setServiceApplicationAnswers(
          interimApplicationGeneratorService.generateAnswerDocument(
            userOptions.getGeneralApplicationType(), caseId, applicant, data
        ));

        FeeDetails serviceFee = newServiceApplication.getServicePaymentFee();
        if (userOptions.getGenAppsUseHelpWithFees().equals(YesOrNo.NO)) {
            newServiceApplication.setAlternativeServiceFeeRequired(YesOrNo.YES);

            OrderSummary orderSummary = paymentSetupService.createServiceApplicationOrderSummary(
                    newServiceApplication, caseId
            );
            serviceFee.setOrderSummary(orderSummary);

            String serviceRequest = paymentSetupService.createServiceApplicationPaymentServiceRequest(
                newServiceApplication, caseId, applicant.getFullName()
            );
            serviceFee.setServiceRequestReference(serviceRequest);

            applicant.setServicePayments(new ArrayList<>());
            details.setState(AwaitingServicePayment);
        } else {
            serviceFee.setHelpWithFeesReferenceNumber(userOptions.getGenAppsHwfRefNumber());
            details.setState(userOptions.awaitingDocuments() ? AwaitingDocuments : AwaitingServicePayment);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_SERVICE_APPLICATION, details.getId());

        AlternativeService serviceApplication = details.getData().getAlternativeService();
        boolean citizenUsingHwf = isNotBlank(serviceApplication.getServicePaymentFee().getHelpWithFeesReferenceNumber());

        if (citizenUsingHwf) {
            // Send notifications;
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private AlternativeService createServiceApplication(
            GeneralApplicationOptions userOptions,
            ApplicationAnswers applicationAnswers
    ) {
        return AlternativeService.builder()
            .serviceApplicationDocuments(userOptions.getGenAppsEvidenceDocs())
            .receivedServiceApplicationDate(LocalDate.now(clock))
            .receivedServiceAddedDate(LocalDate.now(clock))
            .alternativeServiceType(applicationAnswers.serviceApplicationType())
            .build();
    }
}
