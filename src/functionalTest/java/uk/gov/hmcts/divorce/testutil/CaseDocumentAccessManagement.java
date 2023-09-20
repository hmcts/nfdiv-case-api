package uk.gov.hmcts.divorce.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.io.IOException;
import java.util.Objects;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static io.micrometer.core.instrument.binder.BaseUnits.FILES;
import static java.util.Objects.requireNonNull;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.resourceAsBytes;
import static uk.gov.hmcts.reform.ccd.document.am.model.Classification.RESTRICTED;

@TestPropertySource("classpath:application.yaml")
@Service
public class CaseDocumentAccessManagement {

    private static final String CLASSIFICATION = "classification";
    private static final String CASE_TYPE_ID = "caseTypeId";
    private static final String JURISDICTION_ID = "jurisdictionId";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String NFDIV_CASE_API = "nfdiv_case_api";
    private static final String DOCUMENTS = "documents";
    private static final int FIRST = 0;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    private ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${case_document_am.url}")
    private String caseDocumentAccessManagementUrl;

    public CaseDocumentAMDocument upload(final String displayName,
                                         final String fileName,
                                         final String filePath) throws IOException {

        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        final MultipartFile file = new InMemoryMultipartFile(
            displayName,
            fileName,
            MediaType.APPLICATION_PDF_VALUE,
            resourceAsBytes(filePath)
        );

        final String t = restTemplate.postForObject(caseDocumentAccessManagementUrl + "/cases/documents", httpEntity(file), String.class);
        JsonNode jsonNode =
            Objects.requireNonNull(mapper.readTree(t))
                .get(DOCUMENTS)
                .get(FIRST);

        return mapper.treeToValue(jsonNode, CaseDocumentAMDocument.class);
    }

    private HttpEntity<MultiValueMap<String, Object>> httpEntity(final MultipartFile file) {
        MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
        HttpEntity<Resource> fileResource = new HttpEntity<>(buildByteArrayResource(file), buildPartHeaders(file));
        parameters.add(FILES, fileResource);
        parameters.add(CLASSIFICATION, RESTRICTED.toString());
        parameters.add(CASE_TYPE_ID, getCaseType());
        parameters.add(JURISDICTION_ID, JURISDICTION);

        HttpHeaders headers = new HttpHeaders();
        headers.add(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        headers.add(SERVICE_AUTHORIZATION, serviceAuthenticationGenerator.generate(NFDIV_CASE_API));

        return new HttpEntity<>(parameters, headers);
    }

    private static HttpHeaders buildPartHeaders(MultipartFile file) {
        requireNonNull(file.getContentType());
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(file.getContentType()));
        return headers;
    }

    private static ByteArrayResource buildByteArrayResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException ioException) {
            throw new IllegalStateException(ioException);
        }
    }
}
