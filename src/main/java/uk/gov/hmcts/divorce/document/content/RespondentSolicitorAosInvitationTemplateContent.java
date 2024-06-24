package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ACCESS_CODE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_MIDDLE_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_POSTAL_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
@Slf4j
public class RespondentSolicitorAosInvitationTemplateContent {

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     final LocalDate createdDate) {

        Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateContent.put(ISSUE_DATE, createdDate.format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FIRST_NAME, caseData.getApplicant1().getFirstName());
        templateContent.put(APPLICANT_1_MIDDLE_NAME, caseData.getApplicant1().getMiddleName());
        templateContent.put(APPLICANT_1_LAST_NAME, caseData.getApplicant1().getLastName());

        templateContent.put(APPLICANT_2_FIRST_NAME, caseData.getApplicant2().getFirstName());
        templateContent.put(APPLICANT_2_MIDDLE_NAME, caseData.getApplicant2().getMiddleName());
        templateContent.put(APPLICANT_2_LAST_NAME, caseData.getApplicant2().getLastName());

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        templateContent.put(MARRIAGE_DATE,
            ofNullable(caseData.getApplication().getMarriageDetails().getDate())
                .map(marriageDate -> marriageDate.format(DATE_TIME_FORMATTER))
                .orElse(null));

        templateContent.put(APPLICANT_2_POSTAL_ADDRESS, caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck());

        templateContent.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        return templateContent;
    }
}
