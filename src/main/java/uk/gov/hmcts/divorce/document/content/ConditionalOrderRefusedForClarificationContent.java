package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class ConditionalOrderRefusedForClarificationContent {

    public static final String LEGAL_ADVISOR_COMMENTS = "legalAdvisorComments";
    private static final String IS_SOLE = "isSole";
    private static final String IS_JOINT = "isJoint";
    public static final String JUDICIAL_SEPARATION = "judicialSeparation";
    public static final String REASON_JURISDICTION_DETAILS = "jurisdictionDetails";
    public static final String REASON_MARRIAGE_CERT_TRANSLATION = "marriageCertTranslation";
    public static final String REASON_MARRIAGE_CERTIFICATE = "marriageCertificate";
    public static final String REASON_PREVIOUS_PROCEEDINGS_DETAILS = "previousProceedingDetails";

    @Autowired
    private Clock clock;

    @Autowired
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

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

    public Map<String, Object> apply(final CaseData caseData, final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();
        final Set<ClarificationReason> clarificationReasons = conditionalOrder.getRefusalClarificationReason();

        templateContent.put(CCD_CASE_REFERENCE, formatId(ccdCaseReference));
        templateContent.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));

        templateContent.put(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());

        templateContent.put(IS_SOLE, caseData.getApplicationType().isSole());
        templateContent.put(IS_JOINT, !caseData.getApplicationType().isSole());

        templateContent.put(JUDICIAL_SEPARATION,
            caseData.getIsJudicialSeparation() != null && caseData.getIsJudicialSeparation().toBoolean());

        templateContent.put(REASON_JURISDICTION_DETAILS,
            clarificationReasons.contains(ClarificationReason.JURISDICTION_DETAILS));
        templateContent.put(REASON_MARRIAGE_CERT_TRANSLATION,
            clarificationReasons.contains(ClarificationReason.MARRIAGE_CERTIFICATE_TRANSLATION));
        templateContent.put(REASON_MARRIAGE_CERTIFICATE,
            clarificationReasons.contains(ClarificationReason.MARRIAGE_CERTIFICATE));
        templateContent.put(REASON_PREVIOUS_PROCEEDINGS_DETAILS,
            clarificationReasons.contains(ClarificationReason.PREVIOUS_PROCEEDINGS_DETAILS));

        templateContent.put(LEGAL_ADVISOR_COMMENTS, conditionalOrderRefusedForAmendmentContent
            .generateLegalAdvisorComments(conditionalOrder));

        LanguagePreference languagePreference = caseData.getApplicant1().getLanguagePreference();

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, WELSH.equals(languagePreference) ? MARRIAGE_CY : MARRIAGE);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, WELSH.equals(languagePreference) ? CIVIL_PARTNERSHIP_CY : CIVIL_PARTNERSHIP);
        }

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .build();
        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }
}
