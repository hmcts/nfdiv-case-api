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
import uk.gov.hmcts.divorce.common.service.GeneralApplicationService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;

import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
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

    public static final String CITIZEN_SUBMIT_SERVICE_APPLICATION = "citizen-submit-service-application";

    private static final String AWAITING_DECISION_ERROR = """
        A service application has already been submitted and is awaiting a decision.
    """;

    private final PaymentSetupService paymentSetupService;

    private final GeneralApplicationService generalApplicationService;

    private final Clock clock;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_SUBMIT_SERVICE_APPLICATION)
            .forStates(POST_SUBMISSION_STATES)
            .name("Citizen Submit Service Application")
            .description("Citizen Submit Service Application")
            .showSummary()
            .showEventNotes()
            .showCondition(NEVER_SHOW)
            .grant(CREATE_READ_UPDATE, CREATOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, JUDGE, APPLICANT_1_SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} About to Submit callback invoked for Case Id: {}", CITIZEN_SUBMIT_SERVICE_APPLICATION, details.getId());

        if (beforeDetails.getData().getAlternativeService() != null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(AWAITING_DECISION_ERROR))
                .build();
        }

        CaseData data = details.getData();
        long caseId = details.getId();
        AlternativeService alternativeService = data.getAlternativeService();
        Applicant applicant = data.getApplicant1();
        JourneyOptions userOptions = alternativeService.getUserJourneyOptions(applicant);

        setServiceApplicationDetails(alternativeService, userOptions);

        FeeDetails serviceFee = alternativeService.getServicePaymentFee();
        if (userOptions.citizenWillMakePayment()) {
            alternativeService.setAlternativeServiceFeeRequired(YesOrNo.YES);

            OrderSummary orderSummary = paymentSetupService.createServiceApplicationOrderSummary(alternativeService, caseId);
            String serviceRequest = paymentSetupService.createServiceApplicationPaymentServiceRequest(
                alternativeService, caseId, applicant.getFullName()
            );
            serviceFee.setOrderSummary(orderSummary);
            serviceFee.setServiceRequestReference(serviceRequest);
            details.setState(AwaitingServicePayment);
        } else {
            serviceFee.setHelpWithFeesReferenceNumber(userOptions.citizenHwfReference());
            details.setState(AwaitingServiceConsideration);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                            CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_SUBMIT_SERVICE_APPLICATION, details.getId());

        AlternativeService serviceApplication = details.getData().getAlternativeService();
        boolean citizenUsingHwf = isNotBlank(serviceApplication.getServicePaymentFee().getHelpWithFeesReferenceNumber());

        if (citizenUsingHwf) {
            // Send notifications;
        }

        return SubmittedCallbackResponse.builder().build();
    }

    private void setServiceApplicationDetails(AlternativeService alternativeService, JourneyOptions journeyOptions) {
        alternativeService.setServiceApplicationAnswers(journeyOptions.generateAnswerDocument());
        alternativeService.setReceivedServiceApplicationDate(LocalDate.now(clock));
        alternativeService.setReceivedServiceAddedDate(LocalDate.now(clock));
        alternativeService.setAlternativeServiceType(journeyOptions.serviceApplicationType());
    }
}
