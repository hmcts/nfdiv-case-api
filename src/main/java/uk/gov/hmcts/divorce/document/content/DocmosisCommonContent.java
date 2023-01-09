package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT_CY;

@Component
public class DocmosisCommonContent {

    @Value("${court.locations.serviceCentre.serviceCentreName}")
    private String serviceCentre;

    @Value("${court.locations.serviceCentre.centreName}")
    private String centreName;

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.email}")
    private String email;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    public Map<String, Object> getBasicDocmosisTemplateContent(LanguagePreference languagePreference) {
        Map<String, Object> templateContent = new HashMap<>();

        if (ENGLISH.equals(languagePreference)) {
            templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT);
            templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT);
            templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT);
        } else {
            templateContent.put(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT_CY);
            templateContent.put(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT_CY);
            templateContent.put(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL);
            templateContent.put(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT_CY);
        }

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .emailAddress(email)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }
}
