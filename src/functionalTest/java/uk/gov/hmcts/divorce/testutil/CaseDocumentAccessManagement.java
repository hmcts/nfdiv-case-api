package uk.gov.hmcts.divorce.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.io.IOException;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;
import static uk.gov.hmcts.reform.ccd.document.am.model.Classification.RESTRICTED;

@TestPropertySource("classpath:application.yaml")
@Service
public class CaseDocumentAccessManagement {

    private static final int FIRST = 0;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private CaseDocumentClientApi caseDocumentClientApi;

    public Document upload(final String displayName,
                           final String fileName,
                           final String filePath) throws IOException {

        final MultipartFile file = new InMemoryMultipartFile(
            displayName,
            fileName,
            MediaType.APPLICATION_PDF_VALUE,
            resourceAsBytes(filePath)
        );

        final String accessToken = idamTokenGenerator.generateIdamTokenForSystem();

        DocumentUploadRequest request = new DocumentUploadRequest(
            RESTRICTED.toString(),
            CASE_TYPE,
            JURISDICTION,
            singletonList(file)
        );

        final UploadResponse uploadResponse = caseDocumentClientApi.uploadDocuments(
                accessToken,
                serviceAuthenticationGenerator.generate("nfdiv_case_api"),
                request
            );

        return Objects.requireNonNull(uploadResponse)
            .getDocuments()
            .get(FIRST);
    }
}
