package uk.gov.hmcts.divorce.common.service.task;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.io.IOException;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GenerateFormHelperTest {

    @Mock
    private DocumentUploadClientApi documentUploadClientApi;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private GenerateFormHelper generateFormHelper;

    @Test
    void shouldGenerateDocumentAndAddToListOfDocumentsGenerated() throws IOException {
        final CaseData caseData = CaseData.builder().build();
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

        generateFormHelper.addFormToGeneratedDocuments(
            caseData,
            D10,
            "D10",
            "D10.pdf",
            "/D10.pdf");

        verify(documentUploadClientApi).upload(
            eq(CASEWORKER_AUTH_TOKEN),
            eq(SERVICE_AUTHORIZATION),
            eq("caseworker_id"),
            anyList()
        );
        assertThat(caseData.getDocuments().getDocumentsGenerated()).hasSize(1);
    }
}
