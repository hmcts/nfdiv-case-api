package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ChangedNameHow;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.service.PaymentService;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE_TRANSLATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NAME_CHANGE_EVIDENCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.DIVORCE_WELSH;
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

    public static final String PAPERS_SERVED_ANOTHER_WAY = "papersServedAnotherWay";
    public static final String DIVORCE_SERVED_ANOTHER_WAY = "divorceServedAnotherWay";
    public static final String DISSOLUTION_SERVED_ANOTHER_WAY = "dissolutionServedAnotherWay";

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
        StringBuilder sb = new StringBuilder();
        templateVars.put("outstandingOrAddressMissing", populateServeAnotherWayOrAddressMissing(caseData, id, sb));

        return templateVars;
    }

    private Map<String, String> applicant2TemplateVars(final CaseData caseData, final Long id) {
        Map<String, String> templateVars = commonContent.mainTemplateVars(caseData, id, caseData.getApplicant2(), caseData.getApplicant1());
        StringBuilder sb = new StringBuilder();
        templateVars.put("outstandingOrAddressMissing", populateServeAnotherWayOrAddressMissing(caseData, id, sb));

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

    String populateServeAnotherWayOrAddressMissing(CaseData caseData, Long id, StringBuilder sb) {

        boolean isDivorce = caseData.isDivorce();
        boolean isJoint = !caseData.getApplicationType().isSole();
        boolean divorceServedAnotherWay = isDivorce && !isJoint && caseData.getApplication().applicant1WantsToHavePapersServedAnotherWay();

        String applicationType = isDivorce ? "divorce application" : "application to end a civil partnership";

        boolean servingAnotherWay = !isEmpty(caseData.getApplication().getMissingDocumentTypes());

        boolean papersServedAnotherWay = caseData.getApplicationType().isSole()
            && caseData.getApplication().applicant1WantsToHavePapersServedAnotherWay();

        String partner = servingAnotherWay ? commonContent.getPartner(caseData, caseData.getApplicant2(),
            caseData.getApplicant1().getLanguagePreference()) : "";

        Application application = caseData.getApplication();

        boolean alreadyPrinted = false;
        String feeAmount = formatAmount(paymentService.getServiceCost(SERVICE_OTHER, EVENT_GENERAL,KEYWORD_WITHOUT_NOTICE));

        boolean isAddressProvided = application.isAddressProvidedOrServeAnotherWay();

        if (!isAddressProvided) {
            sb.append(MessageFormat.format("""
                You have submitted your {0}.

                You have not yet provided your {1}’s postal address. You need to do one of the following tasks before your application can progress.

                # Provide a postal address

                [Update your {1}’s postal address](https://ucd-divorce-prototype.herokuapp.com/no-response/no-resp-address-postcode-entry-2). \
                We can then send them your application at no additional cost.

                # Apply to progress your application without an address

                If you cannot find your {1}’s postal address, you can \
                [apply to progress your application another way](https://ucd-divorce-prototype.herokuapp.com/no-response/no-resp-address-options). \
                An application will cost {2}, but you may be able to \
                [get help paying this fee](https://www.gov.uk/get-help-with-court-fees).


                """, applicationType, partner, feeAmount));

            alreadyPrinted = true;
        }

        if (servingAnotherWay) {
            boolean hasCertifiedTranslation = Optional.ofNullable(
                caseData.getApplication().getMarriageDetails().getCertifiedTranslation()).orElse(YesOrNo.NO).toBoolean();
            Set<DocumentType> missingDocTypes = caseData.getApplication().getMissingDocumentTypes();

            boolean ukMarriage = caseData.getApplication().getMarriageDetails().getMarriedInUk().toBoolean();
            Set<ChangedNameHow> nameChangedHowSet = getNameChangedHowSet(caseData);

            boolean isMissingMarriageCertificateNameChangeEvidence = missingDocTypes.contains(NAME_CHANGE_EVIDENCE)
                && nameChangedHowSet.contains(ChangedNameHow.MARRIAGE_CERTIFICATE);

            boolean isMissingMarriageCertificate = missingDocTypes.contains(MARRIAGE_CERTIFICATE)
                || isMissingMarriageCertificateNameChangeEvidence && !hasCertifiedTranslation;

            boolean isMissingTranslatedMarriageCertificate = missingDocTypes.contains(MARRIAGE_CERTIFICATE_TRANSLATION)
                || isMissingMarriageCertificateNameChangeEvidence && hasCertifiedTranslation;

            boolean marriageCertificate = isMissingMarriageCertificate && ukMarriage && caseData.isDivorce();
            boolean civilPartnershipCertificate = isMissingMarriageCertificate && ukMarriage && !caseData.isDivorce();
            boolean foreignMarriageCertificate = isMissingMarriageCertificate && !ukMarriage && caseData.isDivorce();
            boolean foreignCivilPartnershipCertificate = isMissingMarriageCertificate && !ukMarriage && !caseData.isDivorce();
            boolean marriageCertificateTranslation = isMissingTranslatedMarriageCertificate && caseData.isDivorce();
            boolean civilPartnershipCertificateTranslation = isMissingTranslatedMarriageCertificate && !caseData.isDivorce();
            boolean nameChangeProof = missingDocTypes.contains(NAME_CHANGE_EVIDENCE) && !isEmpty(nameChangedHowSet)
                && !nameChangedHowSet.contains(ChangedNameHow.MARRIAGE_CERTIFICATE);

            String isDivorceText = isDivorce ? "divorce" : "";
            String divorceOrDissolutionServedAnotherWay = String.format("# Apply to serve the %s papers another way", isDivorceText);
            String isJointText = isJoint ? "You or your partner can " : "You can ";
            String referenceNumber = id != null ? formatId(id) : null;

            sb.append(MessageFormat.format("""

                {0} need to do the following to progress your {1}.


                ^ Your application will not be checked and issued until you have done the following:


                """, isAddressProvided ? "You" : "## You also", applicationType))
            .append(!alreadyPrinted && application.applicant1WantsToHavePapersServedAnotherWay() ? MessageFormat.format("""
                {0}

                You need to apply to serve the {1} papers to your {2} another way. This is because you did not provide their postal address in the application. For example you could try to serve them by email, text message or social media.

                You can apply here: https://www.gov.uk/government/publications/form-d11-application-notice


                """,divorceOrDissolutionServedAnotherWay,  isDivorceText, partner) : "")
            .append("""

            #Send your documents to the court


                You need to send the following documents to the court because you did not upload them in your application:


            """)
            .append(marriageCertificate ? "* Your original marriage certificate or a certified copy\n\n" : "")
            .append(civilPartnershipCertificate ? "* Your original civil partnership certificate or a certified copy\n\n" : "")
            .append(foreignMarriageCertificate ? "* Your original foreign marriage certificate\n\n" : "")
            .append(foreignCivilPartnershipCertificate ? "* Your original foreign civil partnership certificate\n\n" : "")
            .append(marriageCertificateTranslation ? "* A certified translation of your foreign marriage certificate\n\n" : "")
            .append(civilPartnershipCertificateTranslation ? "* A certified translation of your foreign civil partnership certificate\n\n" : "")
            .append(nameChangeProof ? "* Proof that you changed your name. For example deed poll or statutory declaration\n\n" : "")
            .append(MessageFormat.format(
            """
                ## Sending your documents using our online form:

                {0} [upload your documents using our online form](https://contact-us-about-a-divorce-application.form.service.justice.gov.uk/).

                ## Sending documents by post:

                1. Write your reference number on each document: {1}

                2. Post the original documents to:

                Courts and Tribunals Service Centre
                HMCTS Divorce and Dissolution service
                PO Box 13226
                Harlow
                CM20 9UG

                If you choose to post your documents to us, you must post original documents or certified copies. Make sure you also include in your response a return address. \
                Any cherished documents you send, such as marriage certificates, birth certificates, passports or deed polls will be returned to you. \
                Other documents will not be returned.))

                """, isJointText, referenceNumber));
        }

        return sb.toString();
    }
}
