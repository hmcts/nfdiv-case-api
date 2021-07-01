package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUploadDocumentsAndSubmit.CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_START_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CaseworkerUploadDocumentsAndSubmitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    void givenValidCaseDataWhenAboutToStartDocumentUploadCompleteFieldIsReset() throws Exception {

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setDocumentUploadComplete(YES);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_START_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(expectedResponse("classpath:caseworker-document-upload-about-to-start-response.json"))
            );
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitAndDocumentUploadCompleteFieldIsNoThenStateIsAwaitingDocuments() throws Exception {

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setDocumentUploadComplete(NO);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT,
                AwaitingDocuments.getName())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(content().json(responseWithStateOf(AwaitingDocuments)));
    }

    @Test
    void givenValidCaseDataWhenAboutToSubmitAndDocumentUploadCompleteFieldIsYesThenStateIsSubmitted() throws Exception {

        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setDocumentUploadComplete(YES);

        mockMvc.perform(MockMvcRequestBuilders.post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(callbackRequest(
                caseData,
                CASEWORKER_UPLOAD_DOCUMENTS_AND_SUBMIT,
                AwaitingDocuments.getName())))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(content().json(responseWithStateOf(Submitted)));
    }

    private String responseWithStateOf(final State state) throws IOException {
        return expectedResponse("classpath:caseworker-document-upload-about-to-submit-response.json")
            .replace("<state>", state.getName());
    }
}
