package uk.gov.hmcts.divorce.solicitor.event.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
public class GeneralApplicationSelectFee implements CcdPageConfiguration {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PbaService pbaService;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("generalApplicationSelectFeeType", this::midEvent)
            .pageLabel("Select Fee Type")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getGeneralApplicationFeeType)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final CaseData caseData = details.getData();

        final String keyword =
            FEE0227.getLabel().equals(caseData.getGeneralApplication().getGeneralApplicationFeeType().getLabel())
                ? KEYWORD_NOTICE
                : KEYWORD_WITHOUT_NOTICE;

        OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword);
        caseData.getGeneralApplication().getGeneralApplicationFee().setOrderSummary(orderSummary);

        DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

        caseData.getGeneralApplication().getGeneralApplicationFee().setPbaNumbers(pbaNumbersDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
