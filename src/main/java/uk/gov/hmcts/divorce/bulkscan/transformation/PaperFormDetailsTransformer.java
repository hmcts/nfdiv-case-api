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
            caseData.getTransformationAndOcrWarnings().add("Please review serve respondent outside UK in scanned form");
        }

        caseData.getPaperFormDetails().setServiceOutsideUK(ocrDataFields.getServeOutOfUK());
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

        applicant1StatementOfTruth(ocrDataFields, caseData);
        applicant2StatementOfTruth(ocrDataFields, caseData);
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

    private void applicant1StatementOfTruth(OcrDataFields ocrDataFields, CaseData caseData) {
        if (!toBoolean(ocrDataFields.getSoleApplicantOrApplicant1StatementOfTruth())
            && !toBoolean(ocrDataFields.getSoleApplicantOrApplicant1LegalRepStatementOfTruth())) {
            caseData.getTransformationAndOcrWarnings().add("Please review statement of truth for applicant1 in scanned form");
        }
        if (!toBoolean(ocrDataFields.getSoleApplicantOrApplicant1Signing())
            && !toBoolean(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepSignature())) {
            caseData.getTransformationAndOcrWarnings().add("Please review statement of truth signing for applicant1 in scanned form");
        }
        if (isEmpty(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepFullName())) {
            caseData.getTransformationAndOcrWarnings().add("Please review sole or applicant1/legal representative name in scanned form");
        }

        caseData.getPaperFormDetails().setApplicant1SigningSOT(
            from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1Signing()))
        );
        caseData.getPaperFormDetails().setApplicant1LegalRepSigningSOT(
            from(toBoolean(ocrDataFields.getSoleApplicantOrApplicant1OrLegalRepSignature()))
        );
        caseData.getPaperFormDetails().setApplicant1LegalRepPosition(ocrDataFields.getSoleApplicantOrApplicant1LegalRepPosition());

        String day = ocrDataFields.getStatementOfTruthDateDay();
        String month = ocrDataFields.getStatementOfTruthDateMonth();
        String year = ocrDataFields.getStatementOfTruthDateYear();
        if (isEmpty(day) || isEmpty(month) || isEmpty(year)) {
            caseData.getTransformationAndOcrWarnings().add("Please review statement of truth date for applicant1 in scanned form");
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

    private void applicant2StatementOfTruth(OcrDataFields ocrDataFields, CaseData caseData) {
        if (!toBoolean(ocrDataFields.getApplicant2StatementOfTruth())
            && !toBoolean(ocrDataFields.getApplicant2LegalRepStatementOfTruth())) {
            caseData.getTransformationAndOcrWarnings().add("Please review statement of truth for applicant2 in scanned form");
        }
        if (!toBoolean(ocrDataFields.getApplicant2Signing())
            && !toBoolean(ocrDataFields.getApplicant2LegalRepSigning())) {
            caseData.getTransformationAndOcrWarnings().add("Please review statement of truth for applicant2 in scanned form");
        }
        if (isEmpty(ocrDataFields.getApplicant2OrLegalRepFullName())) {
            caseData.getTransformationAndOcrWarnings().add("Please sole or applicant2/legal representative name in scanned form");
        }

        caseData.getPaperFormDetails().setApplicant2SigningSOT(
            from(toBoolean(ocrDataFields.getApplicant2Signing()))
        );
        caseData.getPaperFormDetails().setApplicant2LegalRepSigningSOT(
            from(toBoolean(ocrDataFields.getApplicant2LegalRepSigning()))
        );

        caseData.getPaperFormDetails().setApplicant2LegalRepPosition(ocrDataFields.getApplicant2LegalRepPosition());

        String day = ocrDataFields.getApplicant2StatementOfTruthDateDay();
        String month = ocrDataFields.getApplicant2StatementOfTruthDateMonth();
        String year = ocrDataFields.getApplicant2StatementOfTruthDateYear();
        if (isEmpty(day) || isEmpty(month) || isEmpty(year)) {
            caseData.getTransformationAndOcrWarnings().add("Please review statement of truth date for applicant2 in scanned form");
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

        caseData.getPaperFormDetails().setSoleOrApplicant1PaymentOther(
            from(toBoolean(ocrDataFields.getSoleOrApplicant1PaymentOther()))
        );
        caseData.getPaperFormDetails().setApplicant2PaymentOther(
            from(toBoolean(ocrDataFields.getApplicant2PaymentOther()))
        );
    }
}
