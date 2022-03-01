package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PETITIONER_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.REFUSAL_REASON;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.THE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;

@Component
public class BailiffNotApprovedOrderContent {

    @Value("${court.locations.serviceCentre.poBox}")
    private String poBox;

    @Value("${court.locations.serviceCentre.town}")
    private String town;

    @Value("${court.locations.serviceCentre.postCode}")
    private String postcode;

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

    @Autowired
    private Clock clock;

    @Autowired
    private CommonContent commonContent;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        final Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(CCD_CASE_REFERENCE, ccdCaseReference);
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(PETITIONER_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant1Name());
        templateContent.put(RESPONDENT_FULL_NAME, caseData.getApplication().getMarriageDetails().getApplicant2Name());

        var alternativeService = caseData.getAlternativeService();
        templateContent.put(REFUSAL_REASON, alternativeService.getServiceApplicationRefusalReason());
        templateContent.put(SERVICE_APPLICATION_RECEIVED_DATE,
            alternativeService.getReceivedServiceApplicationDate().format(DATE_TIME_FORMATTER));
        templateContent.put(PARTNER, commonContent.getPartner(caseData, caseData.getApplicant2()));

        var ctscContactDetails = CtscContactDetails
            .builder()
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(THE_APPLICATION, DIVORCE_APPLICATION);
            ctscContactDetails.setEmailAddress(CONTACT_DIVORCE_JUSTICE_GOV_UK);
        } else {
            templateContent.put(THE_APPLICATION, APPLICATION_TO_END_THE_CIVIL_PARTNERSHIP);
            ctscContactDetails.setEmailAddress(CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        }

        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }
}
