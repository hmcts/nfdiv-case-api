package uk.gov.hmcts.divorce.solicitor;

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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorApplyForFinalOrder.SOLICITOR_FINAL_ORDER_REQUESTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SolicitorApplyForFinalOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    void shouldReturnErrorWhenApplyForFinalOrderIsNoAndMidEventIsInvoked() throws Exception {

        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicantWantToApplyForFinalOrder(NO);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=SolicitorApplyForFinalOrder")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_FINAL_ORDER_REQUESTED)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").value("You must select 'Yes' to apply for Final Order"));
    }

    @Test
    void shouldNotReturnErrorWhenApplyForFinalOrderIsYesAndMidEventIsInvoked() throws Exception {

        final CaseData caseData = caseData();
        caseData.getFinalOrder().setDoesApplicantWantToApplyForFinalOrder(YES);

        mockMvc.perform(MockMvcRequestBuilders.post("/callbacks/mid-event?page=SolicitorApplyForFinalOrder")
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_FINAL_ORDER_REQUESTED)))
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk()
            )
            .andExpect(jsonPath("$.errors").doesNotExist());
    }
}
