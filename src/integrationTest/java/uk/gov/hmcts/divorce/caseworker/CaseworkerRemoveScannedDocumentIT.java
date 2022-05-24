package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType.FORM;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRemoveScannedDocument.CASEWORKER_REMOVE_SCANNED_DOCUMENT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequestBeforeAndAfter;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.scannedDocumentWithType;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
public class CaseworkerRemoveScannedDocumentIT {

    private static final String SCANNED_DOCUMENT_RESPONSE_JSON = "classpath:caseworker-remove-scanned-document-response.json";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    void shouldDeleteScannedDocument() throws Exception {

        final CaseData beforeCaseData = caseData();
        setScannedDocument(beforeCaseData);

        final CaseData afterCaseData = caseData();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequestBeforeAndAfter(
                        beforeCaseData,
                        afterCaseData,
                        CASEWORKER_REMOVE_SCANNED_DOCUMENT)))
                .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                content().json(expectedResponse(SCANNED_DOCUMENT_RESPONSE_JSON))
            );
    }

    private String setScannedDocument(final CaseData caseData) throws Exception {
        final String documentId = UUID.randomUUID().toString();
        final var documentListValue = scannedDocumentWithType(FORM, documentId);

        caseData.getDocuments().setScannedDocuments(singletonList(documentListValue));

        return documentId;
    }
}
