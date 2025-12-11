package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

public class GeneralApplicationPaymentConfirmation implements CcdPageConfiguration {

    public static final String CARD_PAYMENT_ERROR = "General application fees must be paid by fee account, help with fees, or telephone.";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationPayment", this::midEvent)
            .pageLabel("Payment - general application payment")
            .complex(CaseData::getGeneralApplication)
                .complex(GeneralApplication::getGeneralApplicationFee)
                .mandatory(FeeDetails::getPaymentMethod)
                .mandatory(FeeDetails::getPbaNumbers,
                    "generalApplicationFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getAccountReferenceNumber,
                    "generalApplicationFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getHelpWithFeesReferenceNumber,
                    "generalApplicationFeePaymentMethod = \"feePayByHelp\"")
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final CaseData caseData = details.getData();
        var generalApplication = caseData.getGeneralApplication();

        var paymentMethod = generalApplication.getGeneralApplicationFee().getPaymentMethod();
        if (ServicePaymentMethod.FEE_PAY_BY_CARD.equals(paymentMethod)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(CARD_PAYMENT_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
