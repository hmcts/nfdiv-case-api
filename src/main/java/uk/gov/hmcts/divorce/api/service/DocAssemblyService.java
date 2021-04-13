package uk.gov.hmcts.divorce.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.clients.DocAssemblyClient;
import uk.gov.hmcts.divorce.api.model.DocumentInfo;
import uk.gov.hmcts.divorce.api.model.docassembly.DocAssemblyRequest;
import uk.gov.hmcts.divorce.api.model.docassembly.DocAssemblyResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.lang.String.format;

@Service
@Slf4j
public class DocAssemblyService {
    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private DocAssemblyClient docAssemblyClient;

    @Autowired
    private ObjectMapper objectMapper;

    public static final String DOCUMENT_FILENAME_FMT = "%s%s";

    public static final String DOCUMENT_NAME = "draft-mini-petition-";

    public DocumentInfo generateAndStoreDraftPetition(
        CaseData caseData,
        Long caseId,
        String authorisation,
        String templateId
    ) {

        DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(templateId)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(caseData))
                .build();

        log.info("Sending document request for template: {} case id: {}", templateId, caseId);

        DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateAndStoreDraftPetition(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );

        log.info("Document successfully generated and stored for case Id {} with document location {}",
            caseId,
            docAssemblyResponse.getRenditionOutputLocation()
        );

        return new DocumentInfo(
            docAssemblyResponse.getRenditionOutputLocation(),
            format(DOCUMENT_FILENAME_FMT, DOCUMENT_NAME, caseId) + ".pdf",
            docAssemblyResponse.getBinaryFilePath()
        );
    }
}
