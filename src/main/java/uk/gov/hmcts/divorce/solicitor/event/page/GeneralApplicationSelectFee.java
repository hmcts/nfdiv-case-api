package uk.gov.hmcts.divorce.solicitor.event.page;

import jakarta.servlet.http.HttpServletRequest;
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

        var generalApplicationFeeType = caseData.getGeneralApplication().getGeneralApplicationFeeType();
        var unpaidGeneralApplicationFees = caseData.getUnpaidGeneralApplicationFees();
        var generalApplication = caseData.getGeneralApplication();

        final String keyword =
            FEE0227.getLabel().equals(generalApplicationFeeType.getLabel())
                ? KEYWORD_NOTICE
                : KEYWORD_WITHOUT_NOTICE;

        if (unpaidGeneralApplicationFees.containsKey(generalApplicationFeeType)) {
            var unpaidGeneralApplicationFeeDetails = unpaidGeneralApplicationFees.get(generalApplicationFeeType);

            reuseUnpaidOrderSummaryAndServiceRequest(details, unpaidGeneralApplicationFeeDetails);
        } else {
            createNewOrderSummaryAndServiceRequest(details, keyword);
        }

        DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

        generalApplication.getGeneralApplicationFee().setPbaNumbers(pbaNumbersDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void reuseUnpaidOrderSummaryAndServiceRequest(CaseDetails<CaseData, State> details, FeeDetails unpaidGeneralApplicationFee) {
        var unpaidOrderSummary = unpaidGeneralApplicationFee.getOrderSummary();
        var unpaidServiceRequest = unpaidGeneralApplicationFee.getServiceRequestReference();

        details.getData().getGeneralApplication().getGeneralApplicationFee().setOrderSummary(unpaidOrderSummary);
        details.getData().getGeneralApplication().getGeneralApplicationFee().setServiceRequestReference(unpaidServiceRequest);
    }

    private void createNewOrderSummaryAndServiceRequest(CaseDetails<CaseData, State> details, String keyword, GeneralApplicationFee fee) {
        var data = details.getData();
        var generalApplicationFee = data.getGeneralApplication().getGeneralApplicationFee();

        var orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, keyword);
        final String serviceRequestReference = paymentService.createServiceRequestReference(
            data.getCitizenPaymentCallbackUrl(), details.getId(),
            data.getApplicant2().getFullName(), orderSummary
        );

        String responsibleParty = ccdAccessService.isApplicant1(httpServletRequest.getHeader(AUTHORIZATION), details.getId())

        generalApplicationFee.setOrderSummary(orderSummary);
        generalApplicationFee.setServiceRequestReference(serviceRequestReference);


        details.getData().getUnpaidGeneralApplicationFees().put(fee, generalApplicationFee);
    }

    private String getResponsibleParty(long caseId, CaseData data) {
        String authHeader = httpServletRequest.getHeader(AUTHORIZATION);

        return ccdAccessService.isApplicant1(authHeader, caseId) ?
            data.getApplicant1().getFullName() :
            data.getApplicant2().getFullName();
    }
}
