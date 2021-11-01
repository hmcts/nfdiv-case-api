package uk.gov.hmcts.divorce.citizen.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.isDivorce;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_REFERENCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_SERVED_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_SERVED_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DISSOLUTION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_FOREIGN_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.MISSING_NAME_CHANGE_PROOF;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.NO;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.PAPERS_SERVED_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SERVE_HUSBAND_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.SERVE_WIFE_ANOTHER_WAY;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.YES;

@Component
@Slf4j
public class ApplicationOutstandingActionNotification {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailTemplatesConfig configVars;

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
        Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant1());
        templateVars.putAll(
            missingDocsTemplateVars(caseData, caseData.getApplication().getApplicant1CannotUploadSupportingDocument())
        );

        boolean soleServingAnotherWay = caseData.getApplicationType().isSole()
            && caseData.getApplication().getApplicant1WantsToHavePapersServedAnotherWay() == YesOrNo.YES;

        templateVars.put(PAPERS_SERVED_ANOTHER_WAY, soleServingAnotherWay ? YES : NO);
        templateVars.put(DIVORCE_SERVED_ANOTHER_WAY, soleServingAnotherWay && isDivorce(caseData) ? YES : NO);
        templateVars.put(SERVE_WIFE_ANOTHER_WAY,
            soleServingAnotherWay && isDivorce(caseData) && caseData.getApplicant2().getGender().equals(Gender.FEMALE) ? YES : NO);
        templateVars.put(SERVE_HUSBAND_ANOTHER_WAY,
            soleServingAnotherWay && isDivorce(caseData) && caseData.getApplicant2().getGender().equals(Gender.MALE) ? YES : NO);
        templateVars.put(DISSOLUTION_SERVED_ANOTHER_WAY, soleServingAnotherWay && !isDivorce(caseData) ? YES : NO);
        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(final CaseData caseData, Long id) {
        Map<String, String> templateVars = commonTemplateVars(caseData, id, caseData.getApplicant2());
        templateVars.putAll(missingDocsTemplateVars(caseData, caseData.getApplication().getApplicant2CannotUploadSupportingDocument()));
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

    private Map<String, String> commonTemplateVars(CaseData caseData, Long id, Applicant applicant) {
        Map<String, String> templateVars = new HashMap<>();
        templateVars.put(IS_DIVORCE, isDivorce(caseData) ? YES : NO);
        templateVars.put(IS_DISSOLUTION, !isDivorce(caseData) ? YES : NO);
        templateVars.put(APPLICATION_REFERENCE, formatId(id));
        templateVars.put(FIRST_NAME, applicant.getFirstName());
        templateVars.put(LAST_NAME, applicant.getLastName());
        templateVars.put(COURT_EMAIL,
            configVars.getTemplateVars().get(isDivorce(caseData) ? DIVORCE_COURT_EMAIL : DISSOLUTION_COURT_EMAIL));
        return  templateVars;
    }
}
