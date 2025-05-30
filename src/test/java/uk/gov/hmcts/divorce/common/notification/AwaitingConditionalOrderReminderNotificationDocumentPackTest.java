package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.print.documentpack.AwaitingConditionalOrderReminderNotificationDocumentPack;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_DISPLAY_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_FILENAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_FILE_LOCATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class AwaitingConditionalOrderReminderNotificationDocumentPackTest {

    @Mock
    private GenerateFormHelper generateFormHelper;

    @InjectMocks
    private AwaitingConditionalOrderReminderNotificationDocumentPack awaitingConditionalOrderReminderNotificationDocumentPack;

    @Test
    void shouldGenerateD84IfNotAlreadyGenerated() throws IOException {
        CaseData caseData = caseData();

        awaitingConditionalOrderReminderNotificationDocumentPack.getDocumentPack(caseData, caseData.getApplicant1());

        verify(generateFormHelper).addFormToGeneratedDocuments(caseData, D84, D84_DISPLAY_NAME, D84_FILENAME, D84_FILE_LOCATION);
    }

    @Test
    void shouldReturnLetterId() {
        String result = awaitingConditionalOrderReminderNotificationDocumentPack.getLetterId();
        Assertions.assertEquals("conditional-order-reminder-pack", result);
    }
}
