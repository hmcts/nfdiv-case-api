package uk.gov.hmcts.divorce.document.content;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_JUSTICE_GOV_UK_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;

@Component
public class DocmosisCommonContent {

    public Map<String, Object> getBasicDocmosisTemplateContent(LanguagePreference languagePreference) {
        Map<String, Object> templateContent = new HashMap<>();

        if (ENGLISH.equals(languagePreference)) {
            templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
            templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
            templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        } else {
            templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY);
            templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY);
            templateContent.put(CONTACT_EMAIL, CONTACT_JUSTICE_GOV_UK_CY);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY);
        }

        return templateContent;
    }
}
