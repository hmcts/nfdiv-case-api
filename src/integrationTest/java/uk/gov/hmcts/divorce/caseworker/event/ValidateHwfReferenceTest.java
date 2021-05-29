package uk.gov.hmcts.divorce.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.ValidateHwfReference.VALIDATE_HWF_CODE;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestResourceUtil.expectedResponse;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ValidateHwfReferenceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    public void shouldMoveCaseStateToAwaitingPaymentWhenHwfCodeIsNotValidForFullAmount() throws Exception {
        var caseData = caseData();
        caseData.setHwfCodeValidForFullAmount(NO);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    VALIDATE_HWF_CODE,
                    AwaitingHWFDecision.name())
                )
            )
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(
                    expectedResponse("classpath:caseworker/validate-hwf-code-awaiting-pay-response.json")
                )
            );
    }

    @Test
    public void shouldMoveCaseStateToSubmittedWhenHwfCodeIsValidForFullAmountAndDocumentsAreUploaded() throws Exception {
        final var documentListValue = documentWithType(MARRIAGE_CERTIFICATE);

        String documentUuid = FilenameUtils.getName(documentListValue.getValue().getDocumentLink().getUrl());

        var caseData = caseData();
        caseData.setHwfCodeValidForFullAmount(YES);
        caseData.setDocumentsUploaded(singletonList(documentListValue));

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    VALIDATE_HWF_CODE,
                    AwaitingHWFDecision.name())
                )
            )
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(
                    expectedResponse("classpath:caseworker/validate-hwf-code-submitted-response.json")
                        .replace("<documentUuid>", documentUuid)
                )
            );
    }

    @Test
    public void shouldMoveCaseStateToAwaitingDocumentsWhenHwfCodeIsValidForFullAmountAndDocumentsAreNotUploaded() throws Exception {
        var caseData = caseData();
        caseData.setHwfCodeValidForFullAmount(YES);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData,
                    VALIDATE_HWF_CODE,
                    AwaitingHWFDecision.name())
                )
            )
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(
                    expectedResponse("classpath:caseworker/validate-hwf-code-awaiting-docs.json")
                )
            );
    }

    @Test
    public void shouldReturnErrorWhenCaseStateIsNotAwaitingHwfDecision() throws Exception {
        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(
                    caseData(),
                    VALIDATE_HWF_CODE,
                    AwaitingPayment.name())
                )
            )
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk()
            )
            .andExpect(
                content().json(
                    expectedResponse("classpath:caseworker/validate-hwf-code-error.json")
                )
            );
    }
}
