package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.GeneralApplicationDraftJourneyService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorDraftGeneralApplicationApplicant1.SOLICITOR_DRAFT_GEN_APP_APPLICANT1;
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
class SolicitorDraftGeneralApplicationApplicant1IT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<CaseDetails<CaseData, State>> caseDetailsCaptor;

    @Captor
    private ArgumentCaptor<Applicant> applicantCaptor;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    @Test
    void shouldSubmitDraftFromInterimOptions() throws Exception {
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .interimApplicationOptions(InterimApplicationOptions.builder().build())
                .build())
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                    callbackRequest(caseData, SOLICITOR_DRAFT_GEN_APP_APPLICANT1)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(generalApplicationDraftJourneyService)
            .prepareAboutToSubmit(caseDetailsCaptor.capture(), applicantCaptor.capture());

        assertThat(caseDetailsCaptor.getValue().getId()).isEqualTo(TEST_CASE_ID);
    }
}
