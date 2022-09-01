package uk.gov.hmcts.divorce.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemEnableSolicitorSwitchToSoleCO.SYSTEM_ENABLE_SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class SystemEnableSolicitorSwitchToSoleCOIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    void shouldSetEnableSolicitorSwitchToSoleCoForApplicant1() throws Exception {

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .build()
            )
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.coApplicant1EnableSolicitorSwitchToSoleCo").value(YES.getValue()));
    }

    @Test
    void shouldSetEnableSolicitorSwitchToSoleCoForApplicant2() throws Exception {

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(NO)
                            .build()
                    )
                    .conditionalOrderApplicant2Questions(
                        ConditionalOrderQuestions.builder()
                            .isSubmitted(YES)
                            .submittedDate(LocalDateTime.now().minusDays(15))
                            .build()
                    )
                    .build()
            )
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .content(objectMapper.writeValueAsString(callbackRequest(caseData, SYSTEM_ENABLE_SWITCH_TO_SOLE_CO)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.coApplicant2EnableSolicitorSwitchToSoleCo").value(YES.getValue()));
    }
}
