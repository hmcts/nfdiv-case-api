package uk.gov.hmcts.divorce.document.content;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@Component
public class ConditionalOrderOfflineClarificationContent {

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
    private ConditionalOrderRefusalContent conditionalOrderRefusalContent;

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

    @Value("${court.locations.serviceCentre.phoneNumber}")
    private String phoneNumber;

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

        templateContent.put(LEGAL_ADVISOR_COMMENTS, conditionalOrderRefusalContent.generateLegalAdvisorComments(conditionalOrder));

        if (caseData.getDivorceOrDissolution().isDivorce()) {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, DIVORCE);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK);
        } else {
            templateContent.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP);
            templateContent.put(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CIVIL_PARTNERSHIP_CASE_JUSTICE_GOV_UK);
        }

        final var ctscContactDetails = CtscContactDetails
            .builder()
            .centreName(centreName)
            .serviceCentre(serviceCentre)
            .poBox(poBox)
            .town(town)
            .postcode(postcode)
            .phoneNumber(phoneNumber)
            .build();
        templateContent.put(CTSC_CONTACT_DETAILS, ctscContactDetails);

        return templateContent;
    }
}
