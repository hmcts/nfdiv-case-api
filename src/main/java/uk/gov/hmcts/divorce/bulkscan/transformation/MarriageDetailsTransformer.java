package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.bulkscan.transformation.D8FormToCaseTransformer.OCR_FIELD_VALUE_BOTH;
import static uk.gov.hmcts.divorce.bulkscan.transformation.D8FormToCaseTransformer.OCR_FIELD_VALUE_NO;
import static uk.gov.hmcts.divorce.bulkscan.transformation.D8FormToCaseTransformer.OCR_FIELD_VALUE_YES;

@Component
public class MarriageDetailsTransformer implements Function<TransformationDetails, TransformationDetails> {
    @Override
    public TransformationDetails apply(TransformationDetails transformationDetails) {
        CaseData caseData = transformationDetails.getCaseData();
        OcrDataFields ocrDataFields = transformationDetails.getOcrDataFields();

        boolean marriageOrCivilPartnershipCert = toBoolean(ocrDataFields.getMarriageOrCivilPartnershipCertificate());
        boolean translation = toBoolean(ocrDataFields.getTranslation());

        caseData.getApplication().setScreenHasMarriageCert(from(marriageOrCivilPartnershipCert));

        if (marriageOrCivilPartnershipCert && !translation) {
            caseData.getApplication().getMarriageDetails().setCertificateInEnglish(YES);
        } else if (!marriageOrCivilPartnershipCert && translation) {
            caseData.getApplication().getMarriageDetails().setCertifiedTranslation(YES);
        } else {
            caseData.getTransformationAndOcrWarnings().add("Please review marriage certificate/translation in the scanned form");
        }

        setMarriageOrCivilPartnershipDetails(ocrDataFields, caseData, caseData.getTransformationAndOcrWarnings());

        return transformationDetails;
    }

    private void setMarriageOrCivilPartnershipDetails(OcrDataFields ocrDataFields, CaseData caseData, List<String> warnings) {
        if (isEmpty(ocrDataFields.getSoleOrApplicant1FullNameAsOnCert())) {
            warnings.add("Please review applicant1's full name as on marriage cert in the scanned form");
        }
        if (isEmpty(ocrDataFields.getRespondentOrApplicant2FullNameAsOnCert())) {
            warnings.add("Please review respondent/applicant2's full name as on marriage cert in the scanned form");
        }

        caseData.getApplication().getMarriageDetails().setMarriedInUk(
            deriveMarriedInTheUK(ocrDataFields.getMarriageOutsideOfUK(), warnings)
        );
        caseData.getApplication().getMarriageDetails().setIssueApplicationWithoutMarriageCertificate(
            deriveApplicationWithoutCert(ocrDataFields.getMakingAnApplicationWithoutCertificate(), warnings)
        );

        caseData.getApplication().getMarriageDetails().setDate(deriveMarriageDate(ocrDataFields, warnings));
        caseData.getApplication().getMarriageDetails().setApplicant1Name(ocrDataFields.getSoleOrApplicant1FullNameAsOnCert());
        caseData.getApplication().getMarriageDetails().setApplicant2Name(ocrDataFields.getRespondentOrApplicant2FullNameAsOnCert());
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage(setPlaceOfMarriageOrCivilPartnership(ocrDataFields, warnings));

        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getDetailsOnCertCorrect())) {
            caseData.getApplication().getMarriageDetails().setCertifyMarriageCertificateIsCorrect(YES);
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getDetailsOnCertCorrect())) {
            caseData.getApplication().getMarriageDetails().setCertifyMarriageCertificateIsCorrect(NO);
        } else {
            warnings.add("Please review marriage certificate details is correct in the scanned form");
        }

        if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(ocrDataFields.getDetailsOnCertCorrect())
            && isEmpty(ocrDataFields.getReasonWhyCertNotCorrect())) {
            warnings.add("Please review reasons why cert not correct in the scanned form");
        }
        caseData.getApplication().getMarriageDetails().setMarriageCertificateIsIncorrectDetails(ocrDataFields.getReasonWhyCertNotCorrect());
    }

    private YesOrNo deriveMarriedInTheUK(String marriageOutsideOfUK, List<String> warnings) {
        if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(marriageOutsideOfUK)) {
            return YES;
        } else if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(marriageOutsideOfUK)) {
            return NO;
        } else {
            warnings.add("Please review applicant1 name different to marriage certificate in the scanned form");
            return null;
        }
    }

    private YesOrNo deriveApplicationWithoutCert(String makingAnApplicationWithoutCertificate, List<String> warnings) {
        if (OCR_FIELD_VALUE_YES.equalsIgnoreCase(makingAnApplicationWithoutCertificate)
            || OCR_FIELD_VALUE_BOTH.equalsIgnoreCase(makingAnApplicationWithoutCertificate)) {
            warnings.add("Please review making an application with marriage certificate in the scanned form");
            return YES;
        } else if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(makingAnApplicationWithoutCertificate)) {
            return NO;
        } else {
            warnings.add("Please review making an application with marriage certificate in the scanned form");
            return null;
        }
    }

    private String setPlaceOfMarriageOrCivilPartnership(OcrDataFields ocrDataFields, List<String> warnings) {
        if (isEmpty(ocrDataFields.getPlaceOfMarriageOrCivilPartnership())
            && (OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getMarriageOutsideOfUK())
            || OCR_FIELD_VALUE_YES.equalsIgnoreCase(ocrDataFields.getMakingAnApplicationWithoutCertificate()))) {
            warnings.add("Please review making an application with marriage certificate in the scanned form");
            return ocrDataFields.getPlaceOfMarriageOrCivilPartnership();
        }
        return ocrDataFields.getPlaceOfMarriageOrCivilPartnership();
    }

    private LocalDate deriveMarriageDate(OcrDataFields ocrDataFields, List<String> warnings) {
        try {
            String marriageMonth = ocrDataFields.getDateOfMarriageOrCivilPartnershipMonth();
            int dayParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipDay()); // format "18"
            int monthParsed = Month.valueOf(marriageMonth.toUpperCase(Locale.UK)).getValue(); //format "January"
            int yearParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipYear()); // format "2022"
            return LocalDate.of(yearParsed, dayParsed, monthParsed);
        } catch (DateTimeException exception) {
            // log and add validation it as will be corrected manually the caseworker
            warnings.add("Please review marriage date in the scanned form");
        }
        return null;
    }
}
