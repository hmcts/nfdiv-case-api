package uk.gov.hmcts.divorce.solicitor.event.page;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolPayment implements CcdPageConfiguration {

    private final PbaService pbaService;
    private final PaymentSetupService paymentSetupService;

    @Value("${idam.client.redirect_uri}")
    private String redirectUrl;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolPayment", this::midEvent)
            .page("SolPayment")
            .pageLabel("Payment for this application")
            .label(
                "LabelSolPaymentPara-1",
                "Amount to pay: **£${solApplicationFeeInPounds}**")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicationFeeOrderSummary)
                .mandatory(Application::getSolPaymentHowToPay)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> detailsBefore
    ) {

        final Long caseId = details.getId();
        log.info("Mid-event callback triggered for SolPayment page Case Id: {}", caseId);

        final CaseData caseData = details.getData();

        if (!caseData.getApplication().isSolicitorPaymentMethodPba()) {
            log.info("Payment method is not PBA for Case Id: {}", caseId);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        try {
            final DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

            log.info("PBA Numbers {}, Case Id: {}", pbaNumbersDynamicList, caseId);
            Application application = caseData.getApplication();
            application.setPbaNumbers(pbaNumbersDynamicList);

            String serviceRequest = paymentSetupService.createApplicationFeeServiceRequest(
                caseData, caseId, redirectUrl
            );
            application.setApplicationFeeServiceRequestReference(serviceRequest);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        } catch (final FeignException e) {
            log.error("Failed to retrieve PBA numbers for Case Id: {}", caseId);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of("No PBA numbers associated with the provided email address"))
                .build();
        }
    }
}
