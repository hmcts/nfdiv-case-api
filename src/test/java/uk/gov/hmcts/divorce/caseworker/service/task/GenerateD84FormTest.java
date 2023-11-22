package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class GenerateD84FormTest {

    @Mock
    private GenerateFormHelper generateFormHelper;

    @InjectMocks
    private GenerateD84Form generateD84Form;

    @Test
    void shouldGenerateD84DocumentAndAddToListOfDocumentsGenerated() throws IOException {
        final CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Document document = Document.builder().build();
        document.links = new Document.Links();
        document.links.self = new Document.Link();
        document.links.binary = new Document.Link();
        document.links.self.href = "/";
        document.links.binary.href = "/binary";
        document.originalDocumentName = "D84";

        generateD84Form.apply(caseDetails);
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

        final CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getDocuments().setDocumentsGenerated(singletonList(d84Document));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = generateD84Form.apply(caseDetails);

        verifyNoInteractions(generateFormHelper);
        assertThat(result.getData()).isEqualTo(caseData);
    }
}
