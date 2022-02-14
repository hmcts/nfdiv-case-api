package uk.gov.hmcts.divorce.bulkscan.transformation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.bulkscan.validation.data.OcrDataFields;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.from;
import static uk.gov.hmcts.divorce.bulkscan.transformation.D8FormToCaseTransformer.OCR_FIELD_VALUE_NO;
import static uk.gov.hmcts.divorce.bulkscan.transformation.D8FormToCaseTransformer.OCR_FIELD_VALUE_YES;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;

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
        caseData.getApplication().getMarriageDetails().setFormationType(
            toBoolean(ocrDataFields.getJurisdictionReasonsSameSex()) ? SAME_SEX_COUPLE : OPPOSITE_SEX_COUPLE
        );

        if (isEmpty(ocrDataFields.getPlaceOfMarriageOrCivilPartnership())
            && (YES.equals(caseData.getApplication().getMarriageDetails().getMarriedInUk())
            || YES.equals(caseData.getApplication().getMarriageDetails().getIssueApplicationWithoutMarriageCertificate()))) {
            warnings.add("Please review place of marriage or civil partnership in scanned form");
        }
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage(ocrDataFields.getPlaceOfMarriageOrCivilPartnership());

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
            warnings.add("Please review married outside UK in the scanned form");
            return null;
        }
    }

    private YesOrNo deriveApplicationWithoutCert(String makingAnApplicationWithoutCertificate, List<String> warnings) {
        if (OCR_FIELD_VALUE_NO.equalsIgnoreCase(makingAnApplicationWithoutCertificate)) {
            return NO;
        } else {
            warnings.add("Please review making an application without marriage certificate in the scanned form");
            return null;
        }
    }

    private LocalDate deriveMarriageDate(OcrDataFields ocrDataFields, List<String> warnings) {
        try {
            int dayParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipDay()); // format "18"
            int monthParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipMonth()); //format "06"
            int yearParsed = Integer.parseInt(ocrDataFields.getDateOfMarriageOrCivilPartnershipYear()); // format "2022"
            return LocalDate.of(yearParsed, dayParsed, monthParsed);
        } catch (DateTimeException | IllegalArgumentException exception) {
            // log and add validation it as will be corrected manually the caseworker
            warnings.add("Please review marriage date in the scanned form");
        }
        return null;
    }
}
