package uk.gov.hmcts.divorce.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateProvider;
import uk.gov.hmcts.divorce.document.model.DocAssemblyRequest;
import uk.gov.hmcts.divorce.document.model.DocAssemblyResponse;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;

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
    private DocmosisTemplateProvider docmosisTemplateProvider;


    public DocumentInfo renderDocument(final Map<String, Object> templateContent,
                                       final Long caseId,
                                       final String authorisation,
                                       final String templateId,
                                       final LanguagePreference languagePreference,
                                       final String filename) {

        final String templateName = docmosisTemplateProvider.templateNameFor(templateId, languagePreference);

        final DocAssemblyRequest docAssemblyRequest = getDocAssemblyRequest(templateName, templateContent);

        log.info("Sending document request for template : {} case id: {}", templateName, caseId);

        final DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateAndStoreDraftApplication(
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
            filename + ".pdf",
            docAssemblyResponse.getBinaryFilePath()
        );
    }

    private DocAssemblyRequest getDocAssemblyRequest(final String templateName, final Map<String, Object> templateContent) {

        return DocAssemblyRequest
            .builder()
            .templateId(templateName)
            .outputType("PDF")
            .formPayload(objectMapper.valueToTree(templateContent))
            .secureDocStoreEnabled(true)
            .caseTypeId(getCaseType())
            .jurisdictionId(JURISDICTION)
            .build();
    }
}
