package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GenerateD10FormTest {

    @Mock
    private DocumentUploadClientApi documentUploadClientApi;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private GenerateD10Form generateD10Form;

    @Test
    void shouldGenerateD10DocumentAndAddToListOfDocumentsGenerated() {
        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolServiceMethod(SOLICITOR_SERVICE);
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

        final UploadResponse uploadResponse = mock(UploadResponse.class);
        final UploadResponse.Embedded embedded = mock(UploadResponse.Embedded.class);

        final User user = new User(CASEWORKER_AUTH_TOKEN, UserDetails.builder().id("caseworker_id").build());

        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(request.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        when(uploadResponse.getEmbedded()).thenReturn(embedded);
        when(embedded.getDocuments()).thenReturn(singletonList(document));
        when(documentUploadClientApi.upload(
            eq(CASEWORKER_AUTH_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("caseworker_id"),
            anyList())
        ).thenReturn(uploadResponse);

        final var result = generateD10Form.apply(caseDetails);

        verify(documentUploadClientApi).upload(
            eq(CASEWORKER_AUTH_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("caseworker_id"),
            anyList()
        );
        assertThat(result.getData().getDocuments().getDocumentsGenerated()).hasSize(1);
    }

    @Test
    void shouldNotGenerateD10DocumentIfSolicitorServiceMethodHasNotBeenSelected() {
        CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = generateD10Form.apply(caseDetails);

        verifyNoInteractions(documentUploadClientApi);
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

        final CaseData caseData = CaseData.builder().build();
        caseData.getApplication().setSolServiceMethod(SOLICITOR_SERVICE);
        caseData.getDocuments().setDocumentsGenerated(singletonList(d10Document));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = generateD10Form.apply(caseDetails);

        verifyNoInteractions(documentUploadClientApi);
        assertThat(result.getData()).isEqualTo(caseData);
    }
}
