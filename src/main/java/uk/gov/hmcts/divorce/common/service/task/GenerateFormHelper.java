package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDocumentAccessManagement;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

@Component
@Slf4j
public class GenerateFormHelper {

    private static final int FIRST = 0;

    @Autowired
    private CaseDocumentAccessManagement documentUploadClientApi;

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

        List<ListValue<DivorceDocument>> documentsGenerated = Optional.ofNullable(caseData.getDocuments().getDocumentsGenerated())
            .orElse(newArrayList());
        documentsGenerated.add(FIRST, generatedForm);
        caseData.getDocuments().setDocumentsGenerated(documentsGenerated);
    }

    private Document uploadFormToDocumentStore(String formDisplayName,
                                               String formFileName,
                                               String formFileLocation) throws IOException {
        final String authToken = authTokenGenerator.generate();
        final String userAuth =  idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        final UploadResponse uploadResponse = documentUploadClientApi.upload(
            userAuth,
            authToken,
            formDisplayName,
            formFileName,
            formFileLocation
        );

        return uploadResponse
            .getDocuments()
            .get(FIRST);
    }
}
