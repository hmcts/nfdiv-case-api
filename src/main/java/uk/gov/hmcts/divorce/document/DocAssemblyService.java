package uk.gov.hmcts.divorce.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.document.content.DraftPetitionTemplateContent;
import uk.gov.hmcts.divorce.document.model.DocAssemblyRequest;
import uk.gov.hmcts.divorce.document.model.DocAssemblyResponse;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

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

    @Autowired
    private DraftPetitionTemplateContent templateContent;

    public static final String DOCUMENT_FILENAME_FMT = "%s%s";

    public static final String DOCUMENT_NAME = "draft-mini-petition-";

    public DocumentInfo renderDocument(
        CaseData caseData,
        Long caseId,
        String authorisation,
        String templateName
    ) {

        Map<String, Object> templateData = templateContent.apply(caseData, caseId);

        DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(templateName)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(templateData))
                .build();

        log.info("Sending document request for template : {} case id: {}", templateName, caseId);

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
