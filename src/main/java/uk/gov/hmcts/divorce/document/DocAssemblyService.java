package uk.gov.hmcts.divorce.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.document.model.DocAssemblyRequest;
import uk.gov.hmcts.divorce.document.model.DocAssemblyResponse;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static java.lang.String.format;

@Service
@Slf4j
public class DocAssemblyService {

    private static final String DOCUMENT_FILENAME_FMT = "%s%s";

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private DocAssemblyClient docAssemblyClient;

    @Autowired
    private ObjectMapper objectMapper;

    public DocumentInfo renderDocument(
        final Map<String, Object> templateData,
        final Long caseId,
        final String authorisation,
        final String templateName,
        final String documentName
    ) {

        DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(templateName)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(templateData))
                .build();

        log.info("Sending document request for template : {} case id: {}", templateName, caseId);

        DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateAndStoreDraftApplication(
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
            format(DOCUMENT_FILENAME_FMT, documentName, caseId) + ".pdf",
            docAssemblyResponse.getBinaryFilePath()
        );
    }
}
