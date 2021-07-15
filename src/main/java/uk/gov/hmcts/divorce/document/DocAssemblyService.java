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
import java.util.Objects;
import java.util.function.Supplier;

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

    @Autowired
    private DocmosisTemplateProvider docmosisTemplateProvider;

    public DocumentInfo renderDocument(final Supplier<Map<String, Object>> templateContentSupplier,
                                       final Long caseId,
                                       final String authorisation,
                                       final String templateId,
                                       final String documentName,
                                       final LanguagePreference languagePreference) {

        final String templateName = docmosisTemplateProvider.templateNameFor(templateId, languagePreference);

        final DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(templateName)
                .outputType("PDF")
                .formPayload(objectMapper.valueToTree(templateContentSupplier.get()))
                .build();

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

        String fileName = Objects.isNull(caseId) ? format(DOCUMENT_FILENAME_FMT, documentName, "") + ".pdf" : format(DOCUMENT_FILENAME_FMT, documentName, caseId) + ".pdf";
        return new DocumentInfo(
            docAssemblyResponse.getRenditionOutputLocation(),
            fileName,
            docAssemblyResponse.getBinaryFilePath()
        );
    }
}
