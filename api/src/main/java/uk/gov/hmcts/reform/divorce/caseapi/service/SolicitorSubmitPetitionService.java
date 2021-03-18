package uk.gov.hmcts.reform.divorce.caseapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.caseapi.clients.FeesAndPaymentsClient;
import uk.gov.hmcts.reform.divorce.caseapi.model.payments.FeeResponse;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeItem;
import uk.gov.hmcts.reform.divorce.ccd.model.FeeValue;
import uk.gov.hmcts.reform.divorce.ccd.model.OrderSummary;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.ccd.model.FeeValue.getValueInPence;

@Service
@Slf4j
public class SolicitorSubmitPetitionService {
    @Autowired
    private FeesAndPaymentsClient feesAndPaymentsClient;

    public void setFeesAndRolesForSubmitPetition(CaseData caseData) {
        FeeResponse feeResponse = feesAndPaymentsClient.getPetitionIssueFee(
            "default",
            "issue",
            "family",
            "family court",
            "divorce",
            null
        );

        // Fees Item is a list as it may have multiple fees but for now it is only one
        caseData.setOrderSummary(
            OrderSummary
                .builder()
                .fees(singletonList(getFeeItem(feeResponse)))
                .paymentTotal(getValueInPence(feeResponse.getAmount()))
                .build()
        );
    }

    private FeeItem getFeeItem(FeeResponse feeResponse) {
        return FeeItem
            .builder()
            .value(
                FeeValue
                    .builder()
                    .feeAmount(getValueInPence(feeResponse.getAmount()))
                    .feeCode(feeResponse.getFeeCode())
                    .feeDescription(feeResponse.getDescription())
                    .feeVersion(String.valueOf(feeResponse.getVersion()))
                    .build()
            )
            .build();
    }
}
