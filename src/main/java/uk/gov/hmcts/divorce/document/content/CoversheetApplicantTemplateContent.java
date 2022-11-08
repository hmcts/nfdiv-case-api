package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class CoversheetApplicantTemplateContent {

    @Autowired
    private DocmosisCommonContent docmosisCommonContent;

    public Map<String, Object> apply(final CaseData caseData,
                                     final Long ccdCaseReference,
                                     final Applicant applicant) {

        final Map<String, Object> templateContent = docmosisCommonContent.getBasicDocmosisTemplateContent(
            applicant.getLanguagePreference());

        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put("applicantFirstName", applicant.getFirstName());
        templateContent.put("applicantLastName", applicant.getLastName());
        templateContent.put("applicantAddress", applicant.getPostalAddress());
        templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
        templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
        templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        return templateContent;
    }
}
