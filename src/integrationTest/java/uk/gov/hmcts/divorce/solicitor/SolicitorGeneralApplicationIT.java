package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.testutil.DocManagementStoreWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import java.time.LocalDate;
import java.util.ArrayList;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.json;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorGeneralApplication.SOLICITOR_GENERAL_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    DocManagementStoreWireMock.PropertiesInitializer.class
})
public class SolicitorGeneralApplicationIT {

    private static final String SOLICITOR_GENERAL_APPLICATION_RESPONSE =
        "classpath:solicitor-general-application-response.json";

    private static final String SOLICITOR_GENERAL_APPLICATION_ERRORS_RESPONSE =
        "classpath:solicitor-general-application-errors-response.json";


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @BeforeAll
    static void setUp() {
        DocManagementStoreWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        DocManagementStoreWireMock.stopAndReset();
    }

    @Test
    public void shouldAddDocumentToDocumentsUploadedWhenGeneralApplicationSubmitted() throws Exception {

        final DivorceDocument document = DivorceDocument.builder()
            .documentDateAdded(LocalDate.now())
            .documentLink(
                Document
                    .builder()
                    .url("http://localhost:4200/assets/d11")
                    .filename("GA.pdf")
                    .binaryUrl("GA.pdf/binary")
                    .build()
            )
            .documentType(DocumentType.D11)
            .build();

        final CaseData caseData = CaseData.builder()
            .documents(CaseDocuments.builder().documentsUploaded(new ArrayList<>()).build())
            .generalApplication(GeneralApplication.builder()
                .generalApplicationDocument(document)
                .build()
            )
            .build();

        CallbackRequest request = callbackRequest(caseData, SOLICITOR_GENERAL_APPLICATION);
        request.getCaseDetails().setState(AwaitingAos.getName());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(request))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .isEqualTo(json(expectedResponse(SOLICITOR_GENERAL_APPLICATION_RESPONSE)));
    }

    @Test
    public void shouldReturnErrorsIfCaseIsPartOfActiveBulkCase() throws Exception {

        final CaseData caseData = CaseData.builder()
            .bulkListCaseReference("1234")
            .build();

        CallbackRequest request = callbackRequest(caseData, SOLICITOR_GENERAL_APPLICATION);
        request.getCaseDetails().setState(AwaitingPronouncement.getName());

        String actualResponse = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(request))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(actualResponse)
            .isEqualTo(json(expectedResponse(SOLICITOR_GENERAL_APPLICATION_ERRORS_RESPONSE)));
    }
}
