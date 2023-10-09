package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ChangedNameHow;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;

@Component
@Slf4j
public class ApplicationOutstandingActionNotification implements ApplicantNotification {

    public static final String PAPERS_SERVED_ANOTHER_WAY = "papersServedAnotherWay";
    public static final String DIVORCE_SERVED_ANOTHER_WAY = "divorceServedAnotherWay";
    public static final String SERVE_WIFE_ANOTHER_WAY = "serveWifeAnotherWay";
    public static final String SERVE_HUSBAND_ANOTHER_WAY = "serveHusbandAnotherWay";
    public static final String DISSOLUTION_SERVED_ANOTHER_WAY = "dissolutionServedAnotherWay";

    public static final String SEND_DOCUMENTS_TO_COURT = "sendDocumentsToCourt";
    public static final String SEND_DOCUMENTS_TO_COURT_DIVORCE = "sendDocumentsToCourtDivorce";
    public static final String SEND_DOCUMENTS_TO_COURT_DISSOLUTION = "sendDocumentsToCourtDissolution";
    public static final String CONDITIONAL_REFERENCE_NUMBER = "conditional reference number";
    public static final String CONDITIONAL_COURT_EMAIL = "conditional court email";
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

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        if (caseData.getApplication().hasAwaitingApplicant1Documents() || caseData.getApplication().hasAwaitingApplicant2Documents()) {
            log.info("Sending application outstanding actions notification to applicant 1 for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant1().getEmail(),
                OUTSTANDING_ACTIONS,
                applicant1TemplateVars(caseData, id),
                caseData.getApplicant1().getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()
            && (caseData.getApplication().hasAwaitingApplicant1Documents() || caseData.getApplication().hasAwaitingApplicant2Documents())) {
            log.info("Sending application outstanding actions notification to applicant 2 for case : {}", id);

            notificationService.sendEmail(
                caseData.getApplicant2EmailAddress(),
                OUTSTANDING_ACTIONS,
                this.applicant2TemplateVars(caseData, id),
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    private Map<String, String> applicant1TemplateVars(final CaseData caseData, final Long id) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant1(), caseData.getApplicant2());
        templateVars.putAll(courtDocumentDetails(caseData, templateVars.get(APPLICATION_REFERENCE), templateVars.get(COURT_EMAIL)));
        boolean soleServingAnotherWay = caseData.getApplicationType().isSole()
            && caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay() == YesOrNo.YES;
        templateVars.putAll(serveAnotherWayTemplateVars(soleServingAnotherWay, caseData));
        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(final CaseData caseData, final Long id) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.putAll(courtDocumentDetails(caseData, templateVars.get(APPLICATION_REFERENCE), templateVars.get(COURT_EMAIL)));
        templateVars.putAll(serveAnotherWayTemplateVars(false, caseData));
        return templateVars;
    }

    private Map<String, String> serveAnotherWayTemplateVars(boolean soleServingAnotherWay, CaseData caseData) {
        Map<String, String> templateVars = new HashMap<>();

        templateVars.put(PAPERS_SERVED_ANOTHER_WAY, soleServingAnotherWay ? YES : NO);
        templateVars.put(DIVORCE_SERVED_ANOTHER_WAY, soleServingAnotherWay && caseData.isDivorce() ? YES : NO);
        templateVars.put(SERVE_WIFE_ANOTHER_WAY,
            soleServingAnotherWay && caseData.isDivorce() && caseData.getApplicant2().getGender().equals(Gender.FEMALE) ? YES : NO);
        templateVars.put(SERVE_HUSBAND_ANOTHER_WAY,
            soleServingAnotherWay && caseData.isDivorce() && caseData.getApplicant2().getGender().equals(Gender.MALE) ? YES : NO);
        templateVars.put(DISSOLUTION_SERVED_ANOTHER_WAY, soleServingAnotherWay && !caseData.isDivorce() ? YES : NO);
        return templateVars;
    }

    private Map<String, String> courtDocumentDetails(CaseData caseData, String referenceNumber, String courtEmail) {
        Map<String, String> templateVars = new HashMap<>();
        boolean needsToSendDocuments = !isEmpty(caseData.getApplication().getMissingDocumentTypes());
        boolean isDivorceAndSendDocumentsToCourt = needsToSendDocuments && caseData.isDivorce();
        boolean isDissolutionAndSendDocumentsToCourt = needsToSendDocuments && !caseData.isDivorce();

        templateVars.put(SEND_DOCUMENTS_TO_COURT, needsToSendDocuments ? YES : NO);
        templateVars.put(SEND_DOCUMENTS_TO_COURT_DIVORCE, isDivorceAndSendDocumentsToCourt ? YES : NO);
        templateVars.put(SEND_DOCUMENTS_TO_COURT_DISSOLUTION, isDissolutionAndSendDocumentsToCourt ? YES : NO);
        templateVars.put(CONDITIONAL_REFERENCE_NUMBER, needsToSendDocuments ? referenceNumber : "");
        templateVars.put(CONDITIONAL_COURT_EMAIL, needsToSendDocuments ? courtEmail : "");

        templateVars.putAll(missingDocsTemplateVars(caseData));

        return templateVars;
    }

    private Map<String, String> missingDocsTemplateVars(CaseData caseData) {
        Map<String, String> templateVars = new HashMap<>();
        Set<DocumentType> missingDocTypes = caseData.getApplication().getMissingDocumentTypes();
        Set<ChangedNameHow> nameChangedHowSet = getNameChangedHowSet(caseData);

        boolean hasCertifiedTranslation = Optional.ofNullable(
            caseData.getApplication().getMarriageDetails().getCertifiedTranslation()).orElse(YesOrNo.NO).toBoolean();
        boolean ukMarriage = caseData.getApplication().getMarriageDetails().getMarriedInUk().toBoolean();
        boolean isMissingMarriageCertificateNameChangeEvidence = missingDocTypes.contains(NAME_CHANGE_EVIDENCE)
            && nameChangedHowSet.contains(ChangedNameHow.MARRIAGE_CERTIFICATE);

        boolean isMissingMarriageCertificate = missingDocTypes.contains(MARRIAGE_CERTIFICATE)
            || isMissingMarriageCertificateNameChangeEvidence && !hasCertifiedTranslation;

        boolean isMissingTranslatedMarriageCertificate = missingDocTypes.contains(MARRIAGE_CERTIFICATE_TRANSLATION)
            || isMissingMarriageCertificateNameChangeEvidence && hasCertifiedTranslation;

        templateVars.put(MISSING_MARRIAGE_CERTIFICATE,
            isMissingMarriageCertificate && ukMarriage && caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE,
            isMissingMarriageCertificate && ukMarriage && !caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_FOREIGN_MARRIAGE_CERTIFICATE,
            isMissingMarriageCertificate && !ukMarriage && caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE,
            isMissingMarriageCertificate && !ukMarriage && !caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION,
            isMissingTranslatedMarriageCertificate && caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION,
            isMissingTranslatedMarriageCertificate && !caseData.isDivorce() ? YES : NO);

        templateVars.put(MISSING_NAME_CHANGE_PROOF, missingDocTypes.contains(NAME_CHANGE_EVIDENCE) && !isEmpty(nameChangedHowSet)
            && !nameChangedHowSet.contains(ChangedNameHow.MARRIAGE_CERTIFICATE) ? YES : NO);

        return templateVars;
    }

    private Set<ChangedNameHow> getNameChangedHowSet(CaseData caseData) {
        HashSet<ChangedNameHow> resultSet = new HashSet<>();

        resultSet.addAll(Optional.ofNullable(caseData.getApplicant1().getNameDifferentToMarriageCertificateMethod()).orElse(Set.of()));
        resultSet.addAll(Optional.ofNullable(caseData.getApplicant1().getLastNameChangedWhenMarriedMethod()).orElse(Set.of()));
        resultSet.addAll(Optional.ofNullable(caseData.getApplicant2().getNameDifferentToMarriageCertificateMethod()).orElse(Set.of()));
        resultSet.addAll(Optional.ofNullable(caseData.getApplicant2().getLastNameChangedWhenMarriedMethod()).orElse(Set.of()));

        resultSet.addAll(Optional.ofNullable(caseData.getApplicant1().getNameChangedHow()).orElse(Set.of()));
        resultSet.addAll(Optional.ofNullable(caseData.getApplicant2().getNameChangedHow()).orElse(Set.of()));

        return resultSet;
    }
}
