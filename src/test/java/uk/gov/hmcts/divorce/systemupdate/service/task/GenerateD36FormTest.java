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
import static uk.gov.hmcts.divorce.document.model.DocumentType.D36;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class GenerateD36FormTest {

    @Mock
    private GenerateFormHelper generateFormHelper;

    @InjectMocks
    private GenerateD36Form generateD36Form;

    @Test
    void shouldGenerateD36DocumentAndAddToListOfDocumentsGenerated() throws IOException {
        final CaseData caseData = CaseData.builder().build();
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        final Document document = Document.builder().build();
        document.links = new Document.Links();
        document.links.self = new Document.Link();
        document.links.binary = new Document.Link();
        document.links.self.href = "/";
        document.links.binary.href = "/binary";
        document.originalDocumentName = "D36";

        generateD36Form.generateD36Document(caseData);
        verify(generateFormHelper).addFormToGeneratedDocuments(
            caseData,
            D36,
            "D36",
            "D36.pdf",
            "/D36.pdf");
    }

    @Test
    void shouldNotGenerateD36DocumentIfOneHasAlreadyBeenGenerated() {
        final ListValue<DivorceDocument> d36Document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D36)
                .documentFileName("D36.pdf")
                .documentLink(
                    new uk.gov.hmcts.ccd.sdk.type.Document(
                        "/",
                        "D36.pdf",
                        "/binary"
                    )
                )
                .build())
            .build();

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getDocuments().setDocumentsGenerated(singletonList(d36Document));

        generateD36Form.generateD36Document(caseData);
        verifyNoInteractions(generateFormHelper);
    }
}
