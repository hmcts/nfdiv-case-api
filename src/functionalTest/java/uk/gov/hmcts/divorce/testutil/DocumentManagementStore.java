package uk.gov.hmcts.divorce.testutil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.io.IOException;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;

@TestPropertySource("classpath:application.yaml")
@Service
public class DocumentManagementStore {

    private static final int FIRST = 0;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private DocumentUploadClientApi documentUploadClientApi;

    public Document upload(final String displayName,
                           final String fileName,
                           final String filePath) throws IOException {

        final MultipartFile file = new InMemoryMultipartFile(
            displayName,
            fileName,
            MediaType.APPLICATION_PDF_VALUE,
            resourceAsBytes(filePath)
        );

        final String accessToken = idamTokenGenerator.generateIdamTokenForSolicitor();
        final String userId = idamTokenGenerator.getUserDetailsFor(accessToken).getId();

        final UploadResponse uploadResponse = documentUploadClientApi.upload(
            accessToken,
            serviceAuthenticationGenerator.generate("nfdiv_case_api"),
            userId,
            singletonList(file)
        );

        return uploadResponse
            .getEmbedded()
            .getDocuments()
            .get(FIRST);
    }
}
