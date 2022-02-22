package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class PaperFormDetails {

    private String serviceOutsideUK;

    private YesOrNo applicantWillServeApplication;

    private YesOrNo respondentDifferentServiceAddress;

    private Set<FinancialOrderFor> summaryApplicant1FinancialOrdersFor;

    private Set<FinancialOrderFor> summaryApplicant2FinancialOrdersFor;

    private YesOrNo applicant1SigningSOT;

    private YesOrNo applicant1LegalRepSigningSOT;

    private YesOrNo applicant2SigningSOT;

    private YesOrNo applicant2LegalRepSigningSOT;

    private String applicant1LegalRepPosition;

    private String applicant2LegalRepPosition;

    private String applicant1SOTSignedOn;

    private String applicant2SOTSignedOn;

    private String feeInPounds;

    private YesOrNo applicant1NoPaymentIncluded;

    private YesOrNo applicant2NoPaymentIncluded;

    private YesOrNo soleOrApplicant1PaymentOther;

    private YesOrNo applicant2PaymentOther;

    private String soleOrApplicant1PaymentOtherDetail;

    private YesOrNo debitCreditCardPayment;

    private YesOrNo debitCreditCardPaymentPhone;

    private YesOrNo howToPayEmail;

    private String paymentDetailEmail;

    private YesOrNo chequeOrPostalOrderPayment;
}
