package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.payment.PaymentService;

import java.time.Clock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.RespondentApplyForFinalOrder.RESPONDENT_APPLY_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class RespondentApplyForFinalOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldChangeStateToAwaitingFinalOrderPaymentIfRespondentWillPayWithoutHwf() throws Exception {
        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.NO);
        OrderSummary orderSummary = OrderSummary.builder().paymentTotal("55000").build();

        when(paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_GENERAL, KEYWORD_NOTICE))
            .thenReturn(orderSummary);

        performRespondentApplyForFinalRequest(caseDetails.getData(), ABOUT_TO_SUBMIT_URL)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(AwaitingFinalOrderPayment.name()))
            .andExpect(jsonPath("$.data.applicant2FinalOrderFeeInPounds").value("550"));
    }

    @Test
    void shouldChangeStateToFinalOrderRequestedIfRespondentWillPayWithHwf() throws Exception {
        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.YES);

        performRespondentApplyForFinalRequest(caseDetails.getData(), ABOUT_TO_SUBMIT_URL)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(RespondentFinalOrderRequested.name()))
            .andExpect(jsonPath("$.data.applicant1AppliedForFinalOrderFirst").value("No"))
            .andExpect(jsonPath("$.data.applicant2AppliedForFinalOrderFirst").value("Yes"));
    }

    @Test
    void shouldNotSendSoleAppliedForFinalOrderNotificationsIfWillPayWithoutHwf() throws Exception {
        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.NO);

        performRespondentApplyForFinalRequest(caseDetails.getData(), SUBMITTED_URL).andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldSendSoleAppliedForFinalOrderNotificationsIfHwfRequested() throws Exception {
        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.YES);

        performRespondentApplyForFinalRequest(caseDetails.getData(), SUBMITTED_URL).andExpect(status().isOk());

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong()
        );
        verifyNoMoreInteractions(notificationService);
    }

    private ResultActions performRespondentApplyForFinalRequest(CaseData caseData, String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(caseData, RESPONDENT_APPLY_FINAL_ORDER, "AwaitingFinalOrder"))
            )
            .accept(APPLICATION_JSON));
    }

    private CaseDetails<CaseData, State> buildTestDataWithHwfAnswer(YesOrNo respondentNeedsHelpWithFees) {
        var hwf = HelpWithFees.builder().needHelp(respondentNeedsHelpWithFees).build();

        return CaseDetails.<CaseData,State>builder().state(AwaitingFinalOrder)
            .data(
                CaseData.builder()
                    .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
                    .applicationType(SOLE_APPLICATION)
                    .applicant2(Applicant.builder().email(TEST_APPLICANT_2_USER_EMAIL).build())
                    .finalOrder(FinalOrder.builder().applicant2FinalOrderHelpWithFees(hwf).build())
                    .build()
            ).build();
    }
}
