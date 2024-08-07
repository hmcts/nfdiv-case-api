package uk.gov.hmcts.divorce.citizen.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.LitigantGrantOfRepresentationConfirmationTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.LETTER_TYPE_GRANT_OF_REPRESENTATION;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_TO_SOLS_EMAIL_NEW_SOL;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.NOC_TO_SOLS_EMAIL_OLD_SOL;

@RequiredArgsConstructor
@Component
@Slf4j
public class NocCitizenToSolsNotifications implements ApplicantNotification {

    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final CaseDataDocumentService caseDataDocumentService;
    private final LitigantGrantOfRepresentationConfirmationTemplateContent templateContent;
    private final BulkPrintService bulkPrintService;


    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app1 : {}", id);
        notificationService.sendEmail(
            caseData.getApplicant1().getEmail(),
            NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN,
            commonContent.nocCitizenTemplateVars(id, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant2(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app2 : {}", id);
        notificationService.sendEmail(
            caseData.getApplicant2().getEmail(),
            NOC_CITIZEN_TO_SOL_EMAIL_CITIZEN,
            commonContent.nocCitizenTemplateVars(id, caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, Long id) {
        generateNoCNotificationLetterAndSend(caseData, id, caseData.getApplicant1());
    }

    @Override
    public void sendToApplicant2Offline(final CaseData caseData, Long id) {
        generateNoCNotificationLetterAndSend(caseData, id, caseData.getApplicant2());
    }

    @Override
    public void sendToApplicant1Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app1Solicitor : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant1().getSolicitor().getEmail(),
            NOC_TO_SOLS_EMAIL_NEW_SOL,
            commonContent.nocSolsTemplateVars(id, caseData.getApplicant1()),
            caseData.getApplicant1().getLanguagePreference(),
            id
        );
    }


    @Override
    public void sendToApplicant2Solicitor(final CaseData caseData, final Long id) {
        log.info("Sending NOC notification to app2Solicitor : {}", id);

        notificationService.sendEmail(
            caseData.getApplicant2().getSolicitor().getEmail(),
            NOC_TO_SOLS_EMAIL_NEW_SOL,
            commonContent.nocSolsTemplateVars(id, caseData.getApplicant2()),
            caseData.getApplicant2().getLanguagePreference(),
            id
        );
    }

    @Override
    public void sendToApplicant1OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC notification to app1OldSolicitor : {}", id);
        Optional.ofNullable(oldCaseData)
            .map(CaseData::getApplicant1)
            .map(Applicant::getSolicitor)
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotEmpty)
            .ifPresent(email -> notificationService.sendEmail(
                email,
                NOC_TO_SOLS_EMAIL_OLD_SOL,
                commonContent.nocOldSolsTemplateVars(id, oldCaseData.getApplicant1()),
                oldCaseData.getApplicant1().getLanguagePreference(),
                id
            ));
    }

    @Override
    public void sendToApplicant2OldSolicitor(final CaseData oldCaseData, final Long id) {
        log.info("Sending NOC notification to app2OldSolicitor : {}", id);

        Optional.ofNullable(oldCaseData)
            .map(CaseData::getApplicant2)
            .map(Applicant::getSolicitor)
            .map(Solicitor::getEmail)
            .filter(StringUtils::isNotEmpty)
            .ifPresent(email -> notificationService.sendEmail(
                email,
                NOC_TO_SOLS_EMAIL_OLD_SOL,
                commonContent.nocOldSolsTemplateVars(id, oldCaseData.getApplicant2()),
                oldCaseData.getApplicant2().getLanguagePreference(),
                id
            ));
    }

    private void generateNoCNotificationLetterAndSend(CaseData caseData, Long caseId, Applicant applicant) {

        Document generatedDocument = generateDocument(caseId, applicant, caseData);

        Letter letter = new  Letter(generatedDocument, 1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
                List.of(letter),
                caseIdString,
                caseIdString,
                LETTER_TYPE_GRANT_OF_REPRESENTATION,
                applicant.getFullName(),
                applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {

        return caseDataDocumentService.renderDocument(templateContent.getTemplateContent(caseData, caseId, applicant),
                caseId,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                applicant.getLanguagePreference(),
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME);
    }
}
