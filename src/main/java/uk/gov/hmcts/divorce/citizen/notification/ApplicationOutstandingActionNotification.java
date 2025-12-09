package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ChangedNameHow;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.JOINT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.notification.CommonContent.NO;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.CommonContent.YES;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.OUTSTANDING_ACTIONS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.payment.FeesAndPaymentsUtil.formatAmount;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.KEYWORD_WITHOUT_NOTICE;
import static uk.gov.hmcts.divorce.payment.service.PaymentService.SERVICE_OTHER;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationOutstandingActionNotification implements ApplicantNotification {


    public static final String SEND_DOCUMENTS_TO_COURT = "sendDocumentsToCourt";
    public static final String SEND_DOCUMENTS_TO_COURT_DIVORCE = "sendDocumentsToCourtDivorce";
    public static final String SEND_DOCUMENTS_TO_COURT_DISSOLUTION = "sendDocumentsToCourtDissolution";
    public static final String MISSING_MARRIAGE_CERTIFICATE = "mariageCertificate";
    public static final String MISSING_CIVIL_PARTNERSHIP_CERTIFICATE = "civilPartnershipCertificate";
    public static final String MISSING_FOREIGN_MARRIAGE_CERTIFICATE = "foreignMarriageCertificate";
    public static final String MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE = "foreignCivilPartnershipCertificate";
    public static final String MISSING_MARRIAGE_CERTIFICATE_TRANSLATION = "marriageCertificateTranslation";
    public static final String MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION = "civilPartnershipCertificateTranslation";
    public static final String MISSING_NAME_CHANGE_PROOF = "nameChangeProof";
    public static final String IS_ADDRESS_PROVIDED = "isAddressProvided";
    public static final String ALTERNATIVE_APPLICATION_FEE = "alternativeApplicationFee";
    public static final String UPDATE_POSTAL_ADDRESS = "updatePostalAddress";
    public static final String APPLY_TO_PROGRESS_ANOTHER_WAY = "applyToProgressAnotherWay";
    public static final String GET_HELP_WITH_FEE = "getHelpWithFee";
    public static final String IS_ADDRESS_PROVIDED_DIVORCE = "isAddressProvidedDivorce";
    public static final String IS_ADDRESS_PROVIDED_DISSOLUTION = "isAddressProvidedDissolution";
    public static final String SEND_DOCUMENTS_REFERENCE_NUMBER = "sendDocumentsToCourtReferenceNumber";
    public static final String UPLOAD_DOCUMENTS_USING_FORM = "uploadDocumentsUsingForm";


    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final PaymentService paymentService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending application outstanding actions notification to applicant 1 for case : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            OUTSTANDING_ACTIONS,
            applicant1TemplateVars(caseData, id),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        if (!caseData.getApplicationType().isSole()) {
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

        templateVars.putAll(courtDocumentDetails(caseData, id));

        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(final CaseData caseData, final Long id) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        templateVars.putAll(courtDocumentDetails(caseData, id));

        return templateVars;
    }

    private Map<String, String> courtDocumentDetails(CaseData caseData, Long id) {
        Map<String, String> templateVars = new HashMap<>();
        boolean needsToSendDocuments = !isEmpty(caseData.getApplication().getMissingDocumentTypes());
        boolean isDivorceAndSendDocumentsToCourt = needsToSendDocuments && caseData.isDivorce();
        boolean isDissolutionAndSendDocumentsToCourt = needsToSendDocuments && !caseData.isDivorce();

        templateVars.put(SEND_DOCUMENTS_TO_COURT, needsToSendDocuments ? YES : NO);
        templateVars.put(SEND_DOCUMENTS_TO_COURT_DIVORCE, isDivorceAndSendDocumentsToCourt ? YES : NO);
        templateVars.put(SEND_DOCUMENTS_TO_COURT_DISSOLUTION, isDissolutionAndSendDocumentsToCourt ? YES : NO);
        templateVars.put(JOINT_CONDITIONAL_ORDER, !caseData.getApplicationType().isSole() ? YES : NO);
        templateVars.put(SEND_DOCUMENTS_REFERENCE_NUMBER, needsToSendDocuments ? id != null ? formatId(id) : null : "");
        templateVars.put(UPLOAD_DOCUMENTS_USING_FORM, needsToSendDocuments
            ? "[upload your documents using our online form](https://contact-us-about-a-divorce-application.form.service.justice.gov.uk/)."
            : "");

        boolean addressNotProvided = !caseData.getApplication().isAddressProvided();

        templateVars.putAll(missingDocsTemplateVars(caseData, needsToSendDocuments));
        templateVars.put(IS_ADDRESS_PROVIDED, addressNotProvided ? YES : NO);
        templateVars.put(ALTERNATIVE_APPLICATION_FEE, addressNotProvided
            ? formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_GENERAL,KEYWORD_WITHOUT_NOTICE)) : "");
        templateVars.put(UPDATE_POSTAL_ADDRESS, addressNotProvided
            ? String.format("[Update your partner’s postal address](%s)", commonContent.getSignInUrl(caseData)) : "");
        templateVars.put(APPLY_TO_PROGRESS_ANOTHER_WAY, addressNotProvided
            ? String.format("[apply to progress your application another way](%s)", commonContent.getSignInUrl(caseData)) : "");
        templateVars.put(GET_HELP_WITH_FEE, addressNotProvided ? "[get help paying this fee](https://www.gov.uk/get-help-with-court-fees)"
            : "");
        templateVars.put(IS_ADDRESS_PROVIDED_DIVORCE, addressNotProvided && caseData.isDivorce() ? YES : NO);
        templateVars.put(IS_ADDRESS_PROVIDED_DISSOLUTION, addressNotProvided && !caseData.isDivorce() ? YES : NO);
        templateVars.put(PARTNER, addressNotProvided
            ? commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant1().getLanguagePreference()) : "");


        return templateVars;
    }

    private Map<String, String> missingDocsTemplateVars(CaseData caseData, boolean needsToSendDocuments) {
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
            needsToSendDocuments && isMissingMarriageCertificate && ukMarriage && caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE,
            needsToSendDocuments && isMissingMarriageCertificate && ukMarriage && !caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_FOREIGN_MARRIAGE_CERTIFICATE,
            needsToSendDocuments && isMissingMarriageCertificate && !ukMarriage && caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_FOREIGN_CIVIL_PARTNERSHIP_CERTIFICATE,
            needsToSendDocuments && isMissingMarriageCertificate && !ukMarriage && !caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_MARRIAGE_CERTIFICATE_TRANSLATION,
            needsToSendDocuments && isMissingTranslatedMarriageCertificate && caseData.isDivorce() ? YES : NO);
        templateVars.put(MISSING_CIVIL_PARTNERSHIP_CERTIFICATE_TRANSLATION,
            needsToSendDocuments && isMissingTranslatedMarriageCertificate && !caseData.isDivorce() ? YES : NO);

        templateVars.put(MISSING_NAME_CHANGE_PROOF, needsToSendDocuments && missingDocTypes.contains(NAME_CHANGE_EVIDENCE)
            && !isEmpty(nameChangedHowSet) && !nameChangedHowSet.contains(ChangedNameHow.MARRIAGE_CERTIFICATE) ? YES : NO);

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
