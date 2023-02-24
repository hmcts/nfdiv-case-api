package uk.gov.hmcts.divorce.document.content;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.util.AddressUtil.getPostalAddress;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HUSBAND_OR_WIFE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NAME_FORMAT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
@Slf4j
public class GeneralLetterTemplateContent {

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

    @Autowired
    private CommonContent commonContent;

    @Autowired
    private Clock clock;

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        log.info("For ccd case reference {} and type(divorce/dissolution) {} ", ccdCaseReference, caseData.getDivorceOrDissolution());

        var generalLetter = caseData.getGeneralLetter();

        mapRecipientDetails(templateContent, generalLetter, caseData);

        templateContent.put(FEEDBACK, generalLetter.getGeneralLetterDetails());
        templateContent.put(ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        templateContent.put(CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());
        templateContent.put(CTSC_CONTACT_DETAILS, CtscContactDetails
            .builder()
            .emailAddress(email)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build());

        return templateContent;
    }

    private void mapRecipientDetails(final Map<String, Object> templateContent,
                                     final GeneralLetter generalLetter,
                                     final CaseData caseData) {
        switch (generalLetter.getGeneralLetterParties()) {
            case APPLICANT -> {
                templateContent.put(RECIPIENT_NAME, getRecipientName(caseData.getApplicant1()));
                templateContent.put(RECIPIENT_ADDRESS, caseData.getApplicant1().getPostalAddress());
                templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
            }
            case RESPONDENT -> {
                templateContent.put(RECIPIENT_NAME, getRecipientName(caseData.getApplicant2()));
                templateContent.put(RECIPIENT_ADDRESS, caseData.getApplicant2().getPostalAddress());
                templateContent.put(RELATION, commonContent.getPartner(caseData, caseData.getApplicant2()));
            }
            default -> {
                templateContent.put(RECIPIENT_NAME, generalLetter.getOtherRecipientName());
                templateContent.put(RECIPIENT_ADDRESS, getPostalAddress(generalLetter.getOtherRecipientAddress()));
                templateContent.put(RELATION, caseData.isDivorce() ? HUSBAND_OR_WIFE : CIVIL_PARTNER);
            }
        }
    }

    private String getRecipientName(final Applicant applicant) {
        return String.format(NAME_FORMAT, applicant.getFirstName(), applicant.getLastName());
    }
}
