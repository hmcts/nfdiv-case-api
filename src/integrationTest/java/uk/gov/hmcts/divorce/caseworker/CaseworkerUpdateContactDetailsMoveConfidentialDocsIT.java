package uk.gov.hmcts.divorce.caseworker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CaseworkerUpdateContactDetailsMoveConfidentialDocsIT {

    public static final String CASEWORKER_UPDATE_CONTACT_DETAILS = "caseworker-update-contact-details";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    public void shouldMoveSensitiveDocsForApp1ToConfidentialDocsWhenApp1GoesPrivate() throws Exception {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .contactDetailsType(ContactDetailsType.PRIVATE)
                .build())
            .applicant2(Applicant.builder()
                .contactDetailsType(ContactDetailsType.PUBLIC)
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(
                    List.of(
                        ListValue.<DivorceDocument>builder()
                            .id(UUID.randomUUID().toString())
                            .value(DivorceDocument.builder()
                                .documentType(DocumentType.AOS_RESPONSE_LETTER)
                                .build())
                            .build(),
                        ListValue.<DivorceDocument>builder()
                            .id(UUID.randomUUID().toString())
                            .value(
                                DivorceDocument.builder()
                                    .documentType(DocumentType.FINAL_ORDER_CAN_APPLY_APP1)
                                    .build()
                            )
                            .build(),
                        ListValue.<DivorceDocument>builder()
                            .id(UUID.randomUUID().toString())
                            .value(DivorceDocument.builder()
                                .documentType(DocumentType.FINAL_ORDER_CAN_APPLY_APP2)
                                .build())
                            .build()
                    )
                )
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_UPDATE_CONTACT_DETAILS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        AboutToStartOrSubmitCallbackResponse convertedResponse = objectMapper
            .readValue(response, AboutToStartOrSubmitCallbackResponse.class);

        List<ListValue<ConfidentialDivorceDocument>> confidentialDocs = convertDocs(
            convertedResponse,
            "confidentialDocumentsGenerated",
            ConfidentialDivorceDocument.class);

        assertThat(confidentialDocs.stream().map(ListValue::getValue)
            .map(ConfidentialDivorceDocument::getConfidentialDocumentsReceived)
            .collect(Collectors.toSet()))
            .containsAll(List.of(
                ConfidentialDocumentsReceived.AOS_RESPONSE_LETTER,
                ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY_APP1,
                ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY_APP2)
            );

        List<ListValue<DivorceDocument>> regularDocs = convertDocs(convertedResponse,
            "documentsGenerated",
            DivorceDocument.class);
        assertThat(regularDocs).hasSize(0);

    }

    @Test
    public void shouldMoveSensitiveDocsForApp2ToConfidentialDocsWhenApp2GoesPrivate() throws Exception {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .contactDetailsType(ContactDetailsType.PUBLIC)
                .build())
            .applicant2(Applicant.builder()
                .contactDetailsType(ContactDetailsType.PRIVATE)
                .build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(
                    List.of(
                        ListValue.<DivorceDocument>builder()
                            .id(UUID.randomUUID().toString())
                            .value(DivorceDocument.builder()
                                .documentType(DocumentType.NOTICE_OF_PROCEEDINGS_APP_2)
                                .build())
                            .build(),
                        ListValue.<DivorceDocument>builder()
                            .id(UUID.randomUUID().toString())
                            .value(
                                DivorceDocument.builder()
                                    .documentType(DocumentType.FINAL_ORDER_CAN_APPLY_APP1)
                                    .build()
                            )
                            .build(),
                        ListValue.<DivorceDocument>builder()
                            .id(UUID.randomUUID().toString())
                            .value(DivorceDocument.builder()
                                .documentType(DocumentType.FINAL_ORDER_CAN_APPLY_APP2)
                                .build())
                            .build()
                    )
                )
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_UPDATE_CONTACT_DETAILS)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse().getContentAsString();

        AboutToStartOrSubmitCallbackResponse convertedResponse = objectMapper
            .readValue(response, AboutToStartOrSubmitCallbackResponse.class);

        List<ListValue<ConfidentialDivorceDocument>> confidentialDocs = convertDocs(
            convertedResponse,
            "confidentialDocumentsGenerated",
            ConfidentialDivorceDocument.class);

        assertThat(confidentialDocs.stream().map(ListValue::getValue)
            .map(ConfidentialDivorceDocument::getConfidentialDocumentsReceived)
            .collect(Collectors.toSet()))
            .containsAll(List.of(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2,
                ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY_APP2,
                ConfidentialDocumentsReceived.FINAL_ORDER_CAN_APPLY_APP1)
            );

        List<ListValue<DivorceDocument>> regularDocs = convertDocs(
            convertedResponse,
            "documentsGenerated",
            DivorceDocument.class);
        assertThat(regularDocs).hasSize(0);
    }

    private <T> List<ListValue<T>> convertDocs(AboutToStartOrSubmitCallbackResponse response,
                                               String documentFieldName,
                                               Class<T> documentType) {
        return objectMapper.convertValue(response.getData().get(documentFieldName),
            TypeFactory.defaultInstance().constructCollectionType(ArrayList.class,
                TypeFactory.defaultInstance().constructParametricType(ListValue.class, documentType)));
    }

}
