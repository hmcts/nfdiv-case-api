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
import uk.gov.hmcts.reform.document.domain.Document;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class GenerateD10FormTest {

    @Mock
    private GenerateFormHelper generateFormHelper;

    @InjectMocks
    private GenerateD10Form generateD10Form;

    @Test
    void shouldGenerateD10DocumentAndAddToListOfDocumentsGenerated() throws IOException {
        final CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getDocuments().setDocumentsGenerated(new ArrayList<>());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Document document = new Document();
        document.links = new Document.Links();
        document.links.self = new Document.Link();
        document.links.binary = new Document.Link();
        document.links.self.href = "/";
        document.links.binary.href = "/binary";
        document.originalDocumentName = "D10";

        generateD10Form.apply(caseDetails);
        verify(generateFormHelper).addFormToGeneratedDocuments(
            caseData,
            D10,
            "D10",
            "D10.pdf",
            "/D10.pdf");
    }

    @Test
    void shouldNotGenerateD10DocumentIfSolicitorServiceMethodHasNotBeenSelected() {
        final CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(COURT_SERVICE);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = generateD10Form.apply(caseDetails);

        verifyNoInteractions(generateFormHelper);
        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldNotGenerateD10DocumentIfOneHasAlreadyBeenGenerated() {
        final ListValue<DivorceDocument> d10Document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(D10)
                .documentFileName("D10.pdf")
                .documentLink(
                    new uk.gov.hmcts.ccd.sdk.type.Document(
                        "/",
                        "D10.pdf",
                        "/binary"
                    )
                )
                .build())
            .build();

        final CaseData caseData = validApplicant1CaseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        caseData.getApplicant2().setEmail(TEST_USER_EMAIL);
        caseData.getDocuments().setDocumentsGenerated(singletonList(d10Document));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = generateD10Form.apply(caseDetails);

        verifyNoInteractions(generateFormHelper);
        assertThat(result.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldNotGenerateD10DocumentIfJointApplication() {
        CaseData caseData = CaseData.builder().build();
        caseData.setApplicationType(JOINT_APPLICATION);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = generateD10Form.apply(caseDetails);

        verifyNoInteractions(generateFormHelper);
        assertThat(result.getData()).isEqualTo(caseData);
    }
}
