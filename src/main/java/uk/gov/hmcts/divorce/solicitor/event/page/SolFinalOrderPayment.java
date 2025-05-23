package uk.gov.hmcts.divorce.solicitor.event.page;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.payment.service.PaymentSetupService;
import uk.gov.hmcts.divorce.solicitor.client.pba.PbaService;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SolFinalOrderPayment implements CcdPageConfiguration {

    private final PbaService pbaService;

    private final PaymentSetupService paymentSetupService;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolFinalOrderPayment", this::midEvent)
            .pageLabel("Payment for this final order")
            .label(
                "FOApp2SolAmountToPay",
                "Amount to pay: **£${applicant2SolFinalOrderFeeInPounds}**")
            .complex(CaseData::getFinalOrder)
                .mandatory(FinalOrder::getApplicant2SolFinalOrderFeeOrderSummary)
                .readonly(
                    FinalOrder::getApplicant2FinalOrderFeeServiceRequestReference,
                    "applicant2SolPaymentHowToPay=\"NEVER\""
                )
                .mandatory(FinalOrder::getApplicant2SolPaymentHowToPay)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> detailsBefore
    ) {

        final Long caseId = details.getId();
        log.info("Mid-event callback triggered for SolFinalOrderPayment page Case Id: {}", caseId);

        final CaseData caseData = details.getData();
        final FinalOrder finalOrder = caseData.getFinalOrder();

        if (!finalOrder.isSolicitorPaymentMethodPba()) {
            log.info("Payment method is not PBA for Case Id: {}", caseId);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        try {
            final DynamicList pbaNumbersDynamicList = pbaService.populatePbaDynamicList();

            log.info("PBA Numbers {}, Case Id: {}", pbaNumbersDynamicList, caseId);
            finalOrder.setFinalOrderPbaNumbers(pbaNumbersDynamicList);

            String serviceRequest = paymentSetupService.createFinalOrderFeeServiceRequest(
                caseData, caseId, finalOrder.getApplicant2SolFinalOrderFeeOrderSummary()
            );
            finalOrder.setApplicant2FinalOrderFeeServiceRequestReference(serviceRequest);

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
