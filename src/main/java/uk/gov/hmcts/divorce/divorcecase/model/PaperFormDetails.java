package uk.gov.hmcts.divorce.divorcecase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaperFormDetails {
    private YesOrNo serviceOutsideUK;

    private YesOrNo respondentServePostOnly;

    private YesOrNo applicantWillServeApplication;

    private YesOrNo respondentDifferentServiceAddress;

    private Set<ApplicationFor> summaryApplicationFor;

    private Set<FinancialOrderFor> summaryApplicant1FinancialOrdersFor;

    private Set<FinancialOrderFor> summaryApplicant2FinancialOrdersFor;

    private YesOrNo applicant1SigningSOT;

    private YesOrNo applicant1LegalRepSigningSOT;

    private YesOrNo applicant2SigningSOT;

    private YesOrNo applicant2LegalRepSigningSOT;

    private String applicant1LegalRepPosition;

    private String applicant2LegalRepPosition;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicant1SOTSignedOn;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applicant2SOTSignedOn;

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
