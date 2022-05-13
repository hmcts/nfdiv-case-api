package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;

@Component
@Slf4j
public class GenerateD10Form implements CaseTask {

    private static final String D10_FILE_LOCATION = "/D10.pdf";
    private static final String D10_FILENAME = "D10.pdf";
    private static final String D10_DISPLAY_NAME = "D10";
    private static final int FIRST = 0;

    @Autowired
    private DocumentUploadClientApi documentUploadClientApi;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();
        final boolean d10DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D10);

        var app2 = caseData.getApplicant2();
        var app2Offline = app2.isRepresented() && app2.getSolicitor() != null
            ? !app2.getSolicitor().hasOrgId()
            : StringUtils.isEmpty(caseData.getApplicant2().getEmail()) || caseData.getApplicant2().isOffline();

        var d10Needed = !caseData.getApplication().isCourtServiceMethod() || app2Offline;

        if (d10Needed && !d10DocumentAlreadyGenerated) {
            try {
                log.info("Adding D10 to list of generated documents for case id: {}", caseId);
                addD10FormToGeneratedDocuments(caseData);
            } catch (Exception e) {
                log.error("Error encountered whilst adding D10 document to list of generated documents for case id: {}", caseId);
            }
        }

        return caseDetails;
    }

    private void addD10FormToGeneratedDocuments(CaseData caseData) throws IOException {
        Document document = uploadD10ToDocumentStore();

        final ListValue<DivorceDocument> d10Document = ListValue.<DivorceDocument>builder()
            .id(UUID.randomUUID().toString())
            .value(DivorceDocument.builder()
                .documentType(D10)
                .documentFileName(D10_FILENAME)
                .documentLink(
                    new uk.gov.hmcts.ccd.sdk.type.Document(
                        document.links.self.href,
                        document.originalDocumentName,
                        document.links.binary.href
                    )
                )
                .build())
            .build();

        caseData.getDocuments().getDocumentsGenerated().add(FIRST, d10Document);
    }

    private Document uploadD10ToDocumentStore() throws IOException {
        final MultipartFile file = new InMemoryMultipartFile(
            D10_DISPLAY_NAME,
            D10_FILENAME,
            MediaType.APPLICATION_PDF_VALUE,
            IOUtils.resourceToByteArray(D10_FILE_LOCATION)
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
