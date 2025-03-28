package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FeeDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.service.PaymentService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Component
@RequiredArgsConstructor
public class GeneralApplicationSelectFee implements CcdPageConfiguration {

    private final PaymentService paymentService;

    private final PbaService pbaService;

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
        var generalApplication = caseData.getGeneralApplication();

        DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

        generalApplication.getGeneralApplicationFee().setPbaNumbers(pbaNumbersDynamicList);

        prepareOrderSummary(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void prepareOrderSummary(CaseData data) {
        GeneralApplicationFee feeType = data.getGeneralApplication().getGeneralApplicationFeeType();
        FeeDetails feeDetails = data.getGeneralApplication().getGeneralApplicationFee();

        String keyword = FEE0227.getLabel().equals(feeType.getLabel())
            ? KEYWORD_NOTICE
            : KEYWORD_WITHOUT_NOTICE;

        var orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword);
        feeDetails.setOrderSummary(orderSummary);
    }
}
