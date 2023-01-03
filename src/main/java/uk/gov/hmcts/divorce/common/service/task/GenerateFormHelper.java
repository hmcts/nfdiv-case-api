package uk.gov.hmcts.divorce.common.service.task;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.io.IOException;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Slf4j
public class GenerateFormHelper {

    private static final int FIRST = 0;

    @Autowired
    private DocumentUploadClientApi documentUploadClientApi;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void addFormToGeneratedDocuments(CaseData caseData,
                                            DocumentType formType,
                                            String formDisplayName,
                                            String formFileName,
                                            String formFileLocation) throws IOException {
        Document document = uploadFormToDocumentStore(formDisplayName, formFileName, formFileLocation);

        final ListValue<DivorceDocument> generatedForm = ListValue.<DivorceDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(DivorceDocument.builder()
                .documentType(formType)
                .documentFileName(formFileName)
                .documentLink(
                    new uk.gov.hmcts.ccd.sdk.type.Document(
                        document.links.self.href,
                        document.originalDocumentName,
                        document.links.binary.href
                    )
                )
                .build())
            .build();

        caseData.getDocuments().getDocumentsGenerated().add(FIRST, generatedForm);
    }

    private Document uploadFormToDocumentStore(String formDisplayName,
                                               String formFileName,
                                               String formFileLocation) throws IOException {
        final MultipartFile file = new InMemoryMultipartFile(
            formDisplayName,
            formFileName,
            MediaType.APPLICATION_PDF_VALUE,
            IOUtils.resourceToByteArray(formFileLocation)
        );

        final String authToken = authTokenGenerator.generate();
        final String userAuth = request.getHeader(AUTHORIZATION);
        final var userDetails = idamService.retrieveUser(userAuth).getUserDetails();

        final UploadResponse uploadResponse = documentUploadClientApi.upload(
            userAuth,
            authToken,
            userDetails.getId(),
            singletonList(file)
        );

        return uploadResponse
            .getEmbedded()
            .getDocuments()
            .get(FIRST);
    }
}
