package uk.gov.hmcts.divorce.citizen.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.payment.PaymentSetupService;

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

    private final PaymentSetupService paymentSetupService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_CREATE_SERVICE_REQUEST)
            .forStates(AwaitingPayment, AwaitingFinalOrderPayment)
            .showCondition(NEVER_SHOW)
            .name("Create Payment Service Request")
            .description("Create Payment Service Request")
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Create Service Request about to submit callback invoked CaseID: {}", details.getId());

        final State state = details.getState();

        if (AwaitingPayment.equals(state)) {
            prepareServiceRequestForApplicationPayment(details.getData(), details.getId());
        } else if (AwaitingFinalOrderPayment.equals(state)) {
            prepareServiceRequestForFinalOrderPayment(details.getData(), details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    private void prepareServiceRequestForApplicationPayment(CaseData data, long caseId) {
        Application application = data.getApplication();

        String serviceRequest = paymentSetupService.createApplicationFeeServiceRequest(
            data, caseId, data.getCitizenPaymentCallbackUrl()
        );

        application.setApplicationFeeServiceRequestReference(serviceRequest);
    }

    private void prepareServiceRequestForFinalOrderPayment(CaseData data, long caseId) {
        FinalOrder finalOrder = data.getFinalOrder();

        String serviceRequest = paymentSetupService.createFinalOrderFeeServiceRequest(
            data, caseId, data.getCitizenPaymentCallbackUrl(), finalOrder.getApplicant2FinalOrderFeeOrderSummary()
        );

        finalOrder.setApplicant2FinalOrderFeeServiceRequestReference(serviceRequest);
    }
}

