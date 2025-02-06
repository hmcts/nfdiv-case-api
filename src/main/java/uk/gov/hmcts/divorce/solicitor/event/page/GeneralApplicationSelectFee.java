package uk.gov.hmcts.divorce.solicitor.event.page;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import static org.springframework.cloud.openfeign.security.OAuth2AccessTokenInterceptor.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationFee.FEE0227;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
@RequiredArgsConstructor
public class GeneralApplicationSelectFee implements CcdPageConfiguration {

    private final PaymentService paymentService;

    private final PbaService pbaService;

    private final CcdAccessService ccdAccessService;

    private final HttpServletRequest httpServletRequest;

    @Value("${idam.client.redirect_uri}")
    private String redirectUrl;

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

        prepareCaseDataForGeneralApplicationPayment(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void prepareCaseDataForGeneralApplicationPayment(CaseDetails<CaseData, State> details) {
        CaseData data = details.getData();
        GeneralApplicationFee feeType = data.getGeneralApplication().getGeneralApplicationFeeType();
        FeeDetails feeDetails = data.getGeneralApplication().getGeneralApplicationFee();

        prepareOrderSummary(feeType, feeDetails);
        prepareServiceRequest(details.getId(), data, feeDetails);
    }

    private void prepareOrderSummary(GeneralApplicationFee feeType, FeeDetails feeDetails) {
        String keyword = FEE0227.getLabel().equals(feeType.getLabel())
            ? KEYWORD_NOTICE
            : KEYWORD_WITHOUT_NOTICE;

        var orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword);
        feeDetails.setOrderSummary(orderSummary);
    }

    private void prepareServiceRequest(long caseId, CaseData data, FeeDetails feeDetails) {
        final String serviceRequest = paymentService.createServiceRequestReference(
            redirectUrl, caseId, responsiblePartyName(caseId, data), feeDetails.getOrderSummary()
        );
        feeDetails.setServiceRequestReference(serviceRequest);
    }

    private String responsiblePartyName(long caseId, CaseData data) {
        String authHeader = httpServletRequest.getHeader(AUTHORIZATION);

        return ccdAccessService.isApplicant1(authHeader, caseId)
            ? data.getApplicant1().getFullName()
            : data.getApplicant2().getFullName();
    }
}
