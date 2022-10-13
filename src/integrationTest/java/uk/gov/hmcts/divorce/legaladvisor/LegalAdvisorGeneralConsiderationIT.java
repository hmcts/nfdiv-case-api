package uk.gov.hmcts.divorce.legaladvisor;

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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralReferralDecision.APPROVE;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorGeneralConsideration.LEGAL_ADVISOR_GENERAL_CONSIDERATION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
public class LegalAdvisorGeneralConsiderationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Clock clock;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;


    @Test
    public void shouldSetGeneralReferralDecisionDateAndCreateGeneralReferralsAndRemoveGeneralReferralFieldsWhenAboutToSubmitIsInvoked()
        throws Exception {

        setMockClock(clock);

        final CaseData caseData = caseData();
        caseData.getGeneralReferral().setGeneralReferralDecision(APPROVE);
        caseData.getGeneralReferral().setGeneralReferralDecisionReason("approved");

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            LEGAL_ADVISOR_GENERAL_CONSIDERATION)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpectAll(
                jsonPath("$.data.generalReferrals[0].value.generalReferralDecisionDate").value(getExpectedLocalDate().toString()),
                jsonPath("$.data.generalReferrals[0].value.generalReferralDecision").value("approve"),
                jsonPath("$.data.generalReferrals[0].value.generalReferralDecisionReason").value("approved"),
                jsonPath("$.data.generalReferralDecision").doesNotExist(),
                jsonPath("$.data.generalReferralDecisionReason").doesNotExist(),
                jsonPath("$.data.generalReferralDecisionDate").doesNotExist()
            );
    }
}
