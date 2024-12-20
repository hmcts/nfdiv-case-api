package uk.gov.hmcts.divorce.solicitor.service.task;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.payment.PaymentSetupService;

@Component
@RequiredArgsConstructor
public class SetApplicationFeeServiceRequest implements CaseTask {

    private final PaymentSetupService paymentSetupService;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> details) {
        var data = details.getData();
        var application = data.getApplication();

        OrderSummary orderSummary = paymentSetupService.createApplicationFeeOrderSummary(
            data, details.getId()
        );
        application.setApplicationFeeOrderSummary(orderSummary);

        String serviceRequest = paymentSetupService.createApplicationFeeServiceRequest(
            data, details.getId()
        );
        application.setApplicationFeeServiceRequestReference(serviceRequest);

        return details;
    }
}
