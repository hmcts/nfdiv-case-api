package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent.URL_TO_LINK_CASE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ACCESS_CODE;
import static uk.gov.hmcts.divorce.notification.CommonContent.CREATE_ACCOUNT_LINK;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_TO_SOLS_EMAIL_SOL_REMOVED_SELF_AS_REPRESENTATIVE;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOL_STOPPED_REP_INVITE_CITIZEN;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;

@RequiredArgsConstructor
@Component
@Slf4j
public class NocSolRemovedSelfAsRepresentativeNotification implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final DocmosisCommonContent docmosisCommonContent;
    private final EmailTemplatesConfig config;
    private final CaseDataDocumentService caseDataDocumentService;
    private final BulkPrintService bulkPrintService;

    public static final String LETTER_TYPE_INVITE_CITIZEN = "invite-citizen";
    private static final String RECIPIENT_ADDRESS = "address";
    public static final String RESPONDENT_SIGN_IN_DIVORCE_URL = "respondentSignInDivorceUrl";
    public static final String RESPONDENT_SIGN_IN_DISSOLUTION_URL = "respondentSignInDissolutionUrl";
    private static final String SIGN_IN_DIVORCE_URL = "signInDivorceUrl";
    private static final String SIGN_IN_DISSOLUTION_URL = "signInDissolutionUrl";

    @Override
    public void sendToApplicant1OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC solicitor removed self from case notification to app1OldSolicitor: {}", id);

        sendToOldSolicitor(oldCaseData, true, id);
    }

    @Override
    public void sendToApplicant2OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC solicitor removed self from case notification to app2OldSolicitor: {}", id);

        sendToOldSolicitor(oldCaseData, false, id);
    }

    private void sendToOldSolicitor(final CaseData data, boolean isApplicant1, final Long id) {
        Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        Optional.ofNullable(applicant)
            .map(Applicant::getSolicitor)
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotEmpty)
            .ifPresent(email -> notificationService.sendEmail(
                email,
                NOC_TO_SOLS_EMAIL_SOL_REMOVED_SELF_AS_REPRESENTATIVE,
                commonContent.nocOldSolsTemplateVars(id, data, isApplicant1),
                applicant.getLanguagePreference(),
                id
            ));
    }

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending email invite to applicant/applicant1 : {}", id);

        Map<String, String> templateVars = commonContent.nocCitizenTemplateVars(id, caseData.getApplicant1());
        templateVars.put(ACCESS_CODE, caseData.getCaseInviteApp1().accessCodeApplicant1());
        templateVars.put(CREATE_ACCOUNT_LINK,
            config.getTemplateVars().get(caseData.isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));

        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            SOL_STOPPED_REP_INVITE_CITIZEN,
            templateVars,
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending email invite to respondent : {}", id);

        Map<String, String> templateVars = commonContent.nocCitizenTemplateVars(id, caseData.getApplicant2());
        templateVars.put(ACCESS_CODE, caseData.getCaseInvite().accessCode());
        if (caseData.getApplicationType() == ApplicationType.SOLE_APPLICATION) {
            templateVars.put(CREATE_ACCOUNT_LINK,
                config.getTemplateVars().get(caseData.isDivorce() ? RESPONDENT_SIGN_IN_DIVORCE_URL : RESPONDENT_SIGN_IN_DISSOLUTION_URL));

            notificationService.sendEmail(
                caseData.getApplicant2().getEmail(),
                SOL_STOPPED_REP_INVITE_CITIZEN,
                templateVars,
                caseData.getApplicant2().getLanguagePreference(),
                id
            );
        }
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, Long id) {
        log.info("Sending letter invite to applicant/applicant1 : {}", id);
        generateNoCNotificationLetterAndSend(caseData, id, caseData.getApplicant1(), true);
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, Long id) {
        if (caseData.getApplicationType() == ApplicationType.SOLE_APPLICATION) {
            log.info("Sending letter invite to respondent : {}", id);
            generateNoCNotificationLetterAndSend(caseData, id, caseData.getApplicant2(), false);
        }
    }


    private void generateNoCNotificationLetterAndSend(CaseData caseData, Long caseId, Applicant applicant, boolean isApplicant1) {

        Document generatedDocument = generateDocument(caseId, applicant, caseData, isApplicant1);

        Letter letter = new  Letter(generatedDocument, 1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
            List.of(letter),
            caseIdString,
            caseIdString,
            LETTER_TYPE_INVITE_CITIZEN,
            applicant.getFullName(),
            applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData,
                                      final boolean isApplicant1) {

        return caseDataDocumentService.renderDocument(getTemplateContent(caseData, caseId, applicant, isApplicant1),
            caseId,
            NFD_SOL_STOPPED_REP_APP_INVITE_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            NFD_NOTICE_OF_CHANGE_APP_INVITE_DOCUMENT_NAME);
    }

    private Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant, boolean isApplicant1) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());
        templateContent.put(FIRST_NAME, applicant.getFirstName());
        templateContent.put(LAST_NAME, applicant.getLastName());
        templateContent.put(RECIPIENT_ADDRESS, applicant.getAddress());
        templateContent.put(CASE_REFERENCE, formatId(caseId));

        if (isApplicant1) {
            templateContent.put(DocmosisTemplateConstants.ACCESS_CODE, caseData.getCaseInviteApp1().accessCodeApplicant1());
            templateContent.put(URL_TO_LINK_CASE,
                config.getTemplateVars().get(caseData.isDivorce() ? SIGN_IN_DIVORCE_URL : SIGN_IN_DISSOLUTION_URL));
        } else {
            templateContent.put(DocmosisTemplateConstants.ACCESS_CODE, caseData.getCaseInvite().accessCode());
            templateContent.put(URL_TO_LINK_CASE,
                config.getTemplateVars().get(caseData.isDivorce() ? RESPONDENT_SIGN_IN_DIVORCE_URL
                    : RESPONDENT_SIGN_IN_DISSOLUTION_URL));
        }
        return templateContent;
    }
}
