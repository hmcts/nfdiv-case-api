package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.PaperCasePaymentMethod.CHEQUE_OR_POSTAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.PaperCasePaymentMethod.PHONE;

@Component
public class PaperFormDetailsTransformer implements Function<TransformationDetails, TransformationDetails> {

    private static final String THE_SOLE_APPLICANT_OR_APPLICANT_1 = "theSoleApplicantOrApplicant1";
    private static final String FOR_THE_CHILDREN = "forTheChildren";
    private static final String THE_SOLE_APPLICANT_OR_APPLICANT_1_FOR_THE_CHILDREN = "theSoleApplicantOrApplicant1,forTheChildren";

    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        if (SOLE_APPLICATION.equals(caseData.getApplicationType()) && isEmpty(ocrDataFields.getServeOutOfUK())) {
            transformationDetails.getTransformationWarnings().add("Please review serve respondent outside UK in scanned form");
        }

        caseData.getPaperFormDetails().setServeOutOfUK(
            from(toBoolean(ocrDataFields.getServeOutOfUK()))
        );

        caseData.getPaperFormDetails().setRespondentServePostOnly(
            from(toBoolean(ocrDataFields.getRespondentServePostOnly()))
        );

        caseData.getPaperFormDetails().setApplicantWillServeApplication(
            from(toBoolean(ocrDataFields.getApplicantWillServeApplication()))
        );
        caseData.getPaperFormDetails().setRespondentDifferentServiceAddress(
            from(toBoolean(ocrDataFields.getRespondentDifferentServiceAddress()))
        );
        caseData.getPaperFormDetails().setSummaryApplicant1FinancialOrdersFor(
            applicantSummaryFinancialOrderFor(ocrDataFields.getSoleOrApplicant1prayerFinancialOrderFor())
        );
        caseData.getPaperFormDetails().setSummaryApplicant2FinancialOrdersFor(
            applicantSummaryFinancialOrderFor(ocrDataFields.getApplicant2PrayerFinancialOrderFor())
        );

        applicant1StatementOfTruth(transformationDetails);
        applicant2StatementOfTruth(transformationDetails);
        setCourtFee(ocrDataFields, caseData);
        return transformationDetails;
    }

    private Set<FinancialOrderFor> applicantSummaryFinancialOrderFor(String applicantPrayerFinancialOrderFor) {
        Set<FinancialOrderFor> financialOrdersFor = new HashSet<>();
        if (THE_SOLE_APPLICANT_OR_APPLICANT_1.equalsIgnoreCase(applicantPrayerFinancialOrderFor)) {
            financialOrdersFor.add(APPLICANT);
        } else if (FOR_THE_CHILDREN.equalsIgnoreCase(applicantPrayerFinancialOrderFor)) {
            financialOrdersFor.add(APPLICANT);
            financialOrdersFor.add(FinancialOrderFor.CHILDREN);
        } else if (THE_SOLE_APPLICANT_OR_APPLICANT_1_FOR_THE_CHILDREN.equalsIgnoreCase(applicantPrayerFinancialOrderFor)) {
            financialOrdersFor.add(FinancialOrderFor.CHILDREN);
        }
        return financialOrdersFor;
    }

    private void applicant1StatementOfTruth(TransformationDetails transformationDetails) {
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();
        if (!toBoolean(ocrDataFields.getSoleApplicantOrApplicant1StatementOfTruth())
            && !toBoolean(ocrDataFields.getSoleApplicantOrApplicant1LegalRepStatementOfTruth())) {
            transformationDetails.getTransformationWarnings().add("Please review statement of truth for applicant1 in scanned form");
        }
        if (!toBoolean(ocrDataFields.getSoleApplicantOrApplicant1Signing())
            && !toBoolean(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepSignature())) {
            transformationDetails.getTransformationWarnings().add(
                "Please review statement of truth signing for applicant1 in scanned form"
            );
        }
        if (isEmpty(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepFullName())) {
            transformationDetails.getTransformationWarnings().add(
                "Please review sole or applicant1/legal representative name in scanned form"
            );
        }

        CaseData caseData = transformationDetails.getCaseData();

        caseData.getPaperFormDetails().setApplicant1SigningSOT(
            from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1Signing()))
        );
        caseData.getPaperFormDetails().setApplicant1LegalRepPosition(ocrDataFields.getSoleApplicantOrApplicant1LegalRepPosition());

        String day = ocrDataFields.getStatementOfTruthDateDay();
        String month = ocrDataFields.getStatementOfTruthDateMonth();
        String year = ocrDataFields.getStatementOfTruthDateYear();
        if (isEmpty(day) || isEmpty(month) || isEmpty(year)) {
            transformationDetails.getTransformationWarnings().add("Please review statement of truth date for applicant1 in scanned form");
        }
        caseData.getPaperFormDetails().setApplicant1SOTSignedOn(
            deriveStatementOfTruthDate(day, month, year)
        );
    }

    private String deriveStatementOfTruthDate(String day, String month, String year) {
        return Stream.of(day, month, year)
            .filter(s -> s != null && !s.isEmpty())
            .collect(Collectors.joining(" "));
    }

    private void applicant2StatementOfTruth(TransformationDetails transformationDetails) {
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        if (!toBoolean(ocrDataFields.getApplicant2StatementOfTruth())
            && !toBoolean(ocrDataFields.getApplicant2LegalRepStatementOfTruth())) {
            transformationDetails.getTransformationWarnings().add("Please review statement of truth for applicant2 in scanned form");
        }
        if (!toBoolean(ocrDataFields.getApplicant2Signing())
            && !toBoolean(ocrDataFields.getApplicant2LegalRepSigning())) {
            transformationDetails.getTransformationWarnings().add("Please review statement of truth for applicant2 in scanned form");
        }
        if (isEmpty(ocrDataFields.getApplicant2OrLegalRepFullName())) {
            transformationDetails.getTransformationWarnings().add("Please sole or applicant2/legal representative name in scanned form");
        }

        CaseData caseData = transformationDetails.getCaseData();

        caseData.getPaperFormDetails().setApplicant2LegalRepSigningSOT(
            from(toBoolean(ocrDataFields.getApplicant2LegalRepSigning()))
        );

        caseData.getPaperFormDetails().setApplicant2LegalRepPosition(ocrDataFields.getApplicant2LegalRepPosition());

        String day = ocrDataFields.getApplicant2StatementOfTruthDateDay();
        String month = ocrDataFields.getApplicant2StatementOfTruthDateMonth();
        String year = ocrDataFields.getApplicant2StatementOfTruthDateYear();
        if (isEmpty(day) || isEmpty(month) || isEmpty(year)) {
            transformationDetails.getTransformationWarnings().add("Please review statement of truth date for applicant2 in scanned form");
        }
        caseData.getPaperFormDetails().setApplicant1SOTSignedOn(
            deriveStatementOfTruthDate(day, month, year)
        );

        caseData.getPaperFormDetails().setApplicant2SOTSignedOn(
            deriveStatementOfTruthDate(day, month, year)
        );
    }

    private void setCourtFee(OcrDataFields ocrDataFields, CaseData caseData) {
        caseData.getPaperFormDetails().setFeeInPounds(ocrDataFields.getCourtFee());
        caseData.getPaperFormDetails().setApplicant1NoPaymentIncluded(
            from(toBoolean(ocrDataFields.getSoleOrApplicant1NoPaymentIncluded()))
        );
        caseData.getPaperFormDetails().setApplicant2NoPaymentIncluded(
            from(toBoolean(ocrDataFields.getApplicant2NoPaymentIncluded()))
        );
        caseData.getPaperFormDetails().setSoleOrApplicant1PaymentOtherDetail(
            ocrDataFields.getSoleOrApplicant1PaymentOtherDetail()
        );
        caseData.getPaperFormDetails().setApplicant2PaymentOtherDetail(
            ocrDataFields.getApplicant2PaymentOtherDetail()
        );
        caseData.getPaperFormDetails().setSoleOrApplicant1PaymentOther(
            from(toBoolean(ocrDataFields.getSoleOrApplicant1PaymentOther()))
        );
        caseData.getPaperFormDetails().setApplicant2PaymentOther(
            from(toBoolean(ocrDataFields.getApplicant2PaymentOther()))
        );

        caseData.getPaperFormDetails().setDebitCreditCardPaymentPhone(
            from(toBoolean(ocrDataFields.getDebitCreditCardPaymentPhone()))
        );
        caseData.getPaperFormDetails().setChequeOrPostalOrderPayment(
            from(toBoolean(ocrDataFields.getChequeOrPostalOrderPayment()))
        );

        if (toBoolean(ocrDataFields.getDebitCreditCardPaymentPhone())) {
            caseData.getApplication().setPaperCasePaymentMethod(PHONE);
        } else if (toBoolean(ocrDataFields.getChequeOrPostalOrderPayment())) {
            caseData.getApplication().setPaperCasePaymentMethod(CHEQUE_OR_POSTAL_ORDER);
        }
    }
}
