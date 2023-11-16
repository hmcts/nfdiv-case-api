package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class GenerateD84FormTest {

    @Mock
    private GenerateFormHelper generateFormHelper;

    @InjectMocks
    private GenerateD84Form generateD84Form;

    @Test
    void shouldGenerateD84DocumentAndAddToListOfDocumentsGenerated() throws IOException {
        final CaseData caseData = CaseData.builder().build();
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        final Document document = Document.builder().build();
        document.links = new Document.Links();
        document.links.self = new Document.Link();
        document.links.binary = new Document.Link();
        document.links.self.href = "/";
        document.links.binary.href = "/binary";
        document.originalDocumentName = "D84";

        generateD84Form.generateD84Document(caseData, TEST_CASE_ID);
        verify(generateFormHelper).addFormToGeneratedDocuments(
            caseData,
            D84,
            "D84",
            "D84.pdf",
            "/D84.pdf");
    }

    @Test
    void shouldNotGenerateD84DocumentIfOneHasAlreadyBeenGenerated() {
        final ListValue<DivorceDocument> d84Document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D84)
                .documentFileName("D84.pdf")
                .documentLink(
                    new uk.gov.hmcts.ccd.sdk.type.Document(
                        "/",
                        "D84.pdf",
                        "/binary"
                    )
                )
                .build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getDocuments().setDocumentsGenerated(singletonList(d84Document));

        generateD84Form.generateD84Document(caseData, TEST_CASE_ID);
        verifyNoInteractions(generateFormHelper);
    }
}
