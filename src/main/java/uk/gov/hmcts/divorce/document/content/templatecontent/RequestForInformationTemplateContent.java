package uk.gov.hmcts.divorce.document.content.templatecontent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.REQUEST_FOR_INFORMATION_DETAILS;
import static uk.gov.hmcts.divorce.notification.CommonContent.SENT_TO_BOTH_APPLICANTS;
import static uk.gov.hmcts.divorce.notification.CommonContent.WEBFORM_URL;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestForInformationTemplateContent implements TemplateContent {

    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(REQUEST_FOR_INFORMATION_LETTER_TEMPLATE_ID, REQUEST_FOR_INFORMATION_SOLICITOR_LETTER_TEMPLATE_ID);
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        log.info("Generating RFI Template Content for CaseId {}", caseId);

        final boolean isApplicant1 = applicant.equals(caseData.getApplicant1());
        final Applicant partner = isApplicant1 ? caseData.getApplicant2() : caseData.getApplicant1();
        final RequestForInformation requestForInformation = caseData.getRequestForInformationList().getRequestForInformation();

        Map<String, Object> templateContent;
        if (applicant.isRepresented()) {
            templateContent = docmosisCommonContent.getBasicSolicitorTemplateContent(
                caseData,
                caseId,
                isApplicant1,
                applicant.getLanguagePreference()
            );
            templateContent.put(RECIPIENT_NAME, applicant.getSolicitor().getName());
        } else {
            templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
                applicant.getLanguagePreference());
            templateContent.put(RECIPIENT_NAME, applicant.getFullName());
        }

        templateContent.put(RECIPIENT_ADDRESS, applicant.getCorrespondenceAddressWithoutConfidentialCheck());
        templateContent.put(ISSUE_DATE, LocalDate.now());
        templateContent.put(CASE_REFERENCE, caseId);
        templateContent.put(WEBFORM_URL, commonContent.getWebFormUrl(applicant.getLanguagePreference()));
        templateContent.put(REQUEST_FOR_INFORMATION_DETAILS, requestForInformation.getRequestForInformationDetails());
        if (BOTH.equals(caseData.getRequestForInformationList().getRequestForInformation().getRequestForInformationJointParties())) {
            templateContent.put(SENT_TO_BOTH_APPLICANTS, YES);
            templateContent.put(PARTNER, commonContent.getPartner(caseData, partner, applicant.getLanguagePreference()));
        } else {
            templateContent.put(SENT_TO_BOTH_APPLICANTS, NO);
            templateContent.put(PARTNER, "");
        }

        return templateContent;
    }
}
