package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.DraftServiceApplicationAction;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationDraftSubmissionService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorAmendDeemedServiceApplication.SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SolicitorAmendDeemedServiceApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private ServiceApplicationDraftSubmissionService serviceApplicationBuilderService;

    @Test
    void shouldAmendDeemedServiceApplication() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .interimApplicationOptions(InterimApplicationOptions.builder()
                    .draftServiceApplicationAction(DraftServiceApplicationAction.AMEND).build())
                .build())
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Applicant> applicantCaptor = ArgumentCaptor.forClass(Applicant.class);

        verify(serviceApplicationBuilderService)
            .submitFromInterimOptions(eq(TEST_CASE_ID), caseDataCaptor.capture(), applicantCaptor.capture());
    }

    @Test
    void shouldWithdrawDeemedServiceApplication() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .interimApplicationOptions(InterimApplicationOptions.builder()
                    .draftServiceApplicationAction(DraftServiceApplicationAction.WITHDRAW).build())
                .build())
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_AMEND_DEEMED_SERVICE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        ArgumentCaptor<CaseData> caseDataCaptor = ArgumentCaptor.forClass(CaseData.class);
        ArgumentCaptor<Applicant> applicantCaptor = ArgumentCaptor.forClass(Applicant.class);

        verify(serviceApplicationBuilderService)
            .submitFromInterimOptions(eq(TEST_CASE_ID), caseDataCaptor.capture(), applicantCaptor.capture());
    }
}
