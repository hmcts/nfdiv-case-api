package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentService;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenCreateServiceRequest implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_CREATE_SERVICE_REQUEST = "citizen-create-service-request";

    private final PaymentService paymentService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_CREATE_SERVICE_REQUEST)
            .forStates(AwaitingPayment, AwaitingFinalOrderPayment)
            .showCondition(NEVER_SHOW)
            .name("Create Service Request for Payment")
            .description("Create Service Request for Payment")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Create Service Request about to submit callback invoked CaseID: {}", details.getId());

        final State state = details.getState();

        if (AwaitingPayment.equals(state)) {
            setServiceRequestForApplicationPayment(details);
        } else if (AwaitingFinalOrderPayment.equals(state)) {
            setServiceRequestForFinalOrderPayment(details);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    private void setServiceRequestForApplicationPayment(CaseDetails<CaseData, State> details) {
        final CaseData data = details.getData();
        final Application application = data.getApplication();
        final OrderSummary orderSummary = application.getApplicationFeeOrderSummary();

        final String serviceRequestReference = createServiceRequest(details, data.getApplicant1(), orderSummary);
        application.setApplicationFeeServiceRequestReference(serviceRequestReference);
    }

    private void setServiceRequestForFinalOrderPayment(CaseDetails<CaseData, State> details) {
        final CaseData data = details.getData();
        final FinalOrder finalOrder = data.getFinalOrder();
        final OrderSummary orderSummary = finalOrder.getApplicant2FinalOrderFeeOrderSummary();

        final String serviceRequestReference = createServiceRequest(
            details, data.getApplicant2(), orderSummary
        );
        finalOrder.setApplicant2FinalOrderFeeServiceRequestReference(serviceRequestReference);
    }

    private String createServiceRequest(final CaseDetails<CaseData, State> details, Applicant responsibleParty, OrderSummary orderSummary) {
        return paymentService.createServiceRequestReference(
            details.getData().getCitizenPaymentCallbackUrl(),
            details.getId(),
            responsibleParty.getFullName(),
            orderSummary
        );
    }
}

