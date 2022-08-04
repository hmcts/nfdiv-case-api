package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderReminderContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REMINDER;

@Component
@Slf4j
public class GenerateConditionalOrderReminderDocument {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private ConditionalOrderReminderContent conditionalOrderReminderContent;

    @Autowired
    private Clock clock;

    public void generateConditionalOrderReminder(final CaseData caseData,
                                                 final Long caseId,
                                                 final Applicant applicant,
                                                 final Applicant partner) {

        log.info("Generating conditional order reminder pdf for CaseID: {}", caseId);

        final Document conditionalOrderReminder = caseDataDocumentService.renderDocument(
            conditionalOrderReminderContent.apply(caseData, caseId, applicant, partner),
            caseId,
            CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME, LocalDateTime.now(clock))
        );

        final DivorceDocument corDocument = DivorceDocument
            .builder()
            .documentLink(conditionalOrderReminder)
            .documentFileName(conditionalOrderReminder.getFilename())
            .documentType(CONDITIONAL_ORDER_REMINDER)
            .build();

        caseData.getDocuments().setDocumentsGenerated(addDocumentToTop(
            caseData.getDocuments().getDocumentsGenerated(),
            corDocument
        ));

        log.info("Completed generating conditional order reminder pdf for CaseID: {}", caseId);
    }
}
