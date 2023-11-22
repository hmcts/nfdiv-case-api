package uk.gov.hmcts.divorce.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class DraftApplicationRemovalServiceTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private DraftApplicationRemovalService draftApplicationRemovalService;

    @Test
    public void shouldRemoveDraftApplicationDocumentFromCaseDataAndDeleteApplicationDocumentFromDocManagement() {

        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(APPLICATION);
        final ListValue<DivorceDocument> coDocumentListValue = documentWithType(GENERAL_APPLICATION);
        final List<ListValue<DivorceDocument>> docs = List.of(divorceDocumentListValue, coDocumentListValue);

        final List<ListValue<DivorceDocument>> actualDocumentsList = draftApplicationRemovalService.removeDraftApplicationDocument(
            docs,
            TEST_CASE_ID
        );

        verify(documentRemovalService).deleteDocument(List.of(divorceDocumentListValue));
        assertThat(actualDocumentsList).size().isEqualTo(1);
    }

    @Test
    public void shouldNotInvokeDocManagementWhenApplicationDocumentDoesNotExistInGenerateDocuments() {
        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(OTHER);

        final List<ListValue<DivorceDocument>> actualDocumentsList = draftApplicationRemovalService.removeDraftApplicationDocument(
            singletonList(divorceDocumentListValue),
            TEST_CASE_ID
        );

        assertThat(actualDocumentsList).containsExactlyInAnyOrder(divorceDocumentListValue);

        verifyNoInteractions(documentRemovalService);
    }

    @Test
    public void shouldNotInvokeDocManagementWhenGeneratedDocumentsListIsEmpty() {

        final List<ListValue<DivorceDocument>> actualDocumentsList = draftApplicationRemovalService.removeDraftApplicationDocument(
            emptyList(),
            TEST_CASE_ID
        );

        assertThat(actualDocumentsList).isEmpty();

        verifyNoInteractions(documentRemovalService);
    }
}
