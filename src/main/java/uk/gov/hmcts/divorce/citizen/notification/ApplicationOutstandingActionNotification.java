package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;

@Component
@Slf4j
public class ApplicationOutstandingActionNotification {

    public static final String PAPERS_SERVED_ANOTHER_WAY = "papersServedAnotherWay";
    public static final String DIVORCE_SERVED_ANOTHER_WAY = "divorceServedAnotherWay";
    public static final String SERVE_WIFE_ANOTHER_WAY = "serveWifeAnotherWay";
    public static final String SERVE_HUSBAND_ANOTHER_WAY = "serveHusbandAnotherWay";
    public static final String DISSOLUTION_SERVED_ANOTHER_WAY = "dissolutionServedAnotherWay";

    public static final String MISSING_MARRIAGE_CERTIFICATE = "mariageCertificate";
    public static final String MISSING_CIVIL_PARTNERSHIP_CERTIFICATE = "civilPartnershipCertificate";
    public static final String MISSING_FOREIGN_MARRIAGE_CERTIFICATE = "foreignMarriageCertificate";
    public static final String MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE = "foreignCivilPartnershipCertificate";
    public static final String MISSING_MARRIAGE_CERTIFICATE_TRANSLATION = "marriageCertificateTranslation";
    public static final String MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION = "civilPartnershipCertificateTranslation";
    public static final String MISSING_NAME_CHANGE_PROOF = "nameChangeProof";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private CommonContent commonContent;

    public void sendToApplicant1(CaseData caseData, Long id) {
        log.info("Sending application outstanding actions notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            OUTSTANDING_ACTIONS,
            applicant1TemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    public void sendToApplicant2(CaseData caseData, Long id) {
        log.info("Sending application outstanding actions notification to applicant 2 for case : {}", id);

        notificationService.sendEmail(
            caseData.getCaseInvite().getApplicant2InviteEmailAddress(),
            OUTSTANDING_ACTIONS,
            this.applicant2TemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference()
        );
    }

    private Map<String, String> applicant1TemplateVars(final CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.putAll(
            missingDocsTemplateVars(caseData, caseData.getApplication().getApplicant1CannotUploadSupportingDocument())
        );
        boolean soleServingAnotherWay = caseData.getApplicationType().isSole()
            && caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay() == YesOrNo.YES;
        templateVars.putAll(serveAnotherWayTemplateVars(soleServingAnotherWay, caseData));
        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(final CaseData caseData, Long id) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.putAll(missingDocsTemplateVars(caseData, caseData.getApplication().getApplicant2CannotUploadSupportingDocument()));
        templateVars.putAll(serveAnotherWayTemplateVars(false, caseData));
        return templateVars;
    }

    private Map<String, String> serveAnotherWayTemplateVars(boolean soleServingAnotherWay, CaseData caseData) {
        Map<String, String> templateVars = new HashMap<>();

        templateVars.put(PAPERS_SERVED_ANOTHER_WAY, soleServingAnotherWay ? YES : NO);
        templateVars.put(DIVORCE_SERVED_ANOTHER_WAY, soleServingAnotherWay && isDivorce(caseData) ? YES : NO);
        templateVars.put(SERVE_WIFE_ANOTHER_WAY,
            soleServingAnotherWay && isDivorce(caseData) && caseData.getApplicant2().getGender().equals(Gender.FEMALE) ? YES : NO);
        templateVars.put(SERVE_HUSBAND_ANOTHER_WAY,
            soleServingAnotherWay && isDivorce(caseData) && caseData.getApplicant2().getGender().equals(Gender.MALE) ? YES : NO);
        templateVars.put(DISSOLUTION_SERVED_ANOTHER_WAY, soleServingAnotherWay && !isDivorce(caseData) ? YES : NO);
        return templateVars;
    }

    private Map<String, String> missingDocsTemplateVars(CaseData caseData, Set<DocumentType> missingDocTypes) {
        Map<String, String> templateVars = new HashMap<>();

        boolean nonNullMissingDocs = missingDocTypes != null && !missingDocTypes.isEmpty();
        boolean ukMarriage = caseData.getApplication().getMarriageDetails().getMarriedInUk().toBoolean();
        templateVars.put(MISSING_MARRIAGE_CERTIFICATE,
            nonNullMissingDocs && missingDocTypes.contains(MARRIAGE_CERTIFICATE) && ukMarriage && isDivorce(caseData) ? YES : NO);
        templateVars.put(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE,
            nonNullMissingDocs && missingDocTypes.contains(MARRIAGE_CERTIFICATE) && ukMarriage && !isDivorce(caseData) ? YES : NO);
        templateVars.put(MISSING_FOREIGN_MARRIAGE_CERTIFICATE,
            nonNullMissingDocs && missingDocTypes.contains(MARRIAGE_CERTIFICATE) && !ukMarriage && isDivorce(caseData) ? YES : NO);
        templateVars.put(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE,
            nonNullMissingDocs && missingDocTypes.contains(MARRIAGE_CERTIFICATE) && !ukMarriage && !isDivorce(caseData) ? YES : NO);
        templateVars.put(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION,
            nonNullMissingDocs && missingDocTypes.contains(MARRIAGE_CERTIFICATE_TRANSLATION) && isDivorce(caseData) ? YES : NO);
        templateVars.put(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION,
            nonNullMissingDocs && missingDocTypes.contains(MARRIAGE_CERTIFICATE_TRANSLATION) && !isDivorce(caseData) ? YES : NO);
        templateVars.put(MISSING_NAME_CHANGE_PROOF, nonNullMissingDocs && missingDocTypes.contains(NAME_CHANGE_EVIDENCE) ? YES : NO);
        return  templateVars;
    }
}
