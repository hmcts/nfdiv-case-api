package uk.gov.hmcts.divorce.common.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.RespondentDraftAosStartedTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.notification.ApplicantNotification;
import uk.gov.hmcts.divorce.notification.CommonContent;
import uk.gov.hmcts.divorce.notification.NotificationService;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_DRAFT_AOS_STARTED_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.RESPONDENT_DRAFT_AOS_STARTED;

@Component
@Slf4j
@RequiredArgsConstructor
public class RespondentDraftAosStartedNotification implements ApplicantNotification {

    public static final String RESPONDENT_DRAFTED_AOS = "respondent-drafted-aos";

    private final NotificationService notificationService;
    private final CommonContent commonContent;
    private final RespondentDraftAosStartedTemplateContent templateContent;
    private final CaseDataDocumentService caseDataDocumentService;
    private final BulkPrintService bulkPrintService;

    @Override
    public void sendToApplicant1(final CaseData caseData, final Long id) {
        log.info("Sending Respondent draft aos started notification to applicant 1 for case : {}", id);

        Applicant applicant = caseData.getApplicant1();

        var templateContent = commonContent.mainTemplateVars(caseData, id, applicant, caseData.getApplicant2());
        templateContent.put(NAME, applicant.getFullName());

        notificationService.sendEmail(
                applicant.getEmail(),
                RESPONDENT_DRAFT_AOS_STARTED,
                templateContent,
                applicant.getLanguagePreference(),
                id);
    }

    @Override
    public void sendToApplicant1Offline(final CaseData caseData, final Long id) {
        log.info("Sending Respondent draft aos started letter to applicant 1 for case : {}", id);

        Applicant applicant = caseData.getApplicant1();

        Letter letter = new  Letter(generateDocument(id, applicant, caseData), 1);
        String caseIdString = String.valueOf(id);

        final Print print = new Print(
                List.of(letter),
                caseIdString,
                caseIdString,
                RESPONDENT_DRAFTED_AOS,
                applicant.getFullName(),
                applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, id);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {
        return caseDataDocumentService.renderDocument(templateContent.getTemplateContent(caseData, caseId, applicant),
                caseId,
                RESPONDENT_DRAFT_AOS_STARTED_APPLICATION_TEMPLATE_ID,
                applicant.getLanguagePreference(),
                RESPONDENT_DRAFT_AOS_STARTED_DOCUMENT_NAME);
    }
}
