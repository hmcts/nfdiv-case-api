package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.Optional;

import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_DISPLAY_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_FILENAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_FILE_LOCATION;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REMINDER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;

@Component
@RequiredArgsConstructor
public class AwaitingConditionalOrderReminderNotificationDocumentPack implements DocumentPack {

    private static final String LETTER_TYPE_CONDITIONAL_ORDER_REMINDER_PACK = "conditional-order-reminder-pack";
    private final GenerateFormHelper generateFormHelper;

    private static final DocumentPackInfo CONDITIONAL_ORDER_REMINDER_PACK = new DocumentPackInfo(
        ImmutableMap.of(
            COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
            CONDITIONAL_ORDER_REMINDER, Optional.of(CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID),
            D84, Optional.empty()
        ),
        ImmutableMap.of(
            COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
            CONDITIONAL_ORDER_REMINDER_TEMPLATE_ID, CONDITIONAL_ORDER_REMINDER_DOCUMENT_NAME
        )
    );

    @Override
    public DocumentPackInfo getDocumentPack(CaseData caseData,
                                            Applicant applicant) {
        final boolean d84DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D84);

        if (!d84DocumentAlreadyGenerated) {
            try {
                generateFormHelper.addFormToGeneratedDocuments(caseData, D84, D84_DISPLAY_NAME, D84_FILENAME, D84_FILE_LOCATION);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return CONDITIONAL_ORDER_REMINDER_PACK;
    }

    @Override
    public String getLetterId() {
        return LETTER_TYPE_CONDITIONAL_ORDER_REMINDER_PACK;
    }
}
