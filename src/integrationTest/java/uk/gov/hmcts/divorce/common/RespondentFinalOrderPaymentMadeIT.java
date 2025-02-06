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
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Payment;
import uk.gov.hmcts.divorce.divorcecase.model.PaymentStatus;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.RespondentFinalOrderPaymentMade.RESPONDENT_FINAL_ORDER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.common.service.PaymentValidatorService.ERROR_PAYMENT_INCOMPLETE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER;
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
public class RespondentFinalOrderPaymentMadeIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @MockBean
    private NotificationService notificationService;

    @Test
    void shouldReturnErrorsIfPaymentWasUnsuccessful() throws Exception {
        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithPaymentStatus(PaymentStatus.CANCELLED);

        performRespondentFinalOrderPaymentMadeRequest(caseDetails.getData(), ABOUT_TO_SUBMIT_URL)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").value(ERROR_PAYMENT_INCOMPLETE))
            .andExpect(jsonPath("$.state").value(AwaitingFinalOrderPayment.name()));
    }

    @Test
    void shouldChangeCaseStateToFinalOrderRequestedIfPaymentWasSuccessful() throws Exception {
        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithPaymentStatus(PaymentStatus.SUCCESS);

        performRespondentFinalOrderPaymentMadeRequest(caseDetails.getData(), ABOUT_TO_SUBMIT_URL)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(RespondentFinalOrderRequested.name()))
            .andExpect(jsonPath("$.data.applicant1AppliedForFinalOrderFirst").value("No"))
            .andExpect(jsonPath("$.data.applicant2AppliedForFinalOrderFirst").value("Yes"));
    }

    @Test
    void shouldSendAppliedForFinalOrderNotifications() throws Exception {
        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithPaymentStatus(PaymentStatus.SUCCESS);

        performRespondentFinalOrderPaymentMadeRequest(caseDetails.getData(), SUBMITTED_URL).andExpect(status().isOk());

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong()
        );
        verifyNoMoreInteractions(notificationService);
    }

    private ResultActions performRespondentFinalOrderPaymentMadeRequest(CaseData caseData, String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(caseData, RESPONDENT_FINAL_ORDER_PAYMENT_MADE, "AwaitingFinalOrderPayment"))
            )
            .accept(APPLICATION_JSON));
    }

    private CaseDetails<CaseData, State>  buildTestDataWithPaymentStatus(PaymentStatus status) {
        List<ListValue<Payment>> payments = singletonList(
            new ListValue<>("1", Payment.builder().amount(55000).status(status).build())
        );

        return CaseDetails.<CaseData,State>builder().state(AwaitingFinalOrder)
            .data(
                CaseData.builder()
                    .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
                    .applicationType(SOLE_APPLICATION)
                    .applicant2(Applicant.builder().email(TEST_APPLICANT_2_USER_EMAIL).build())
                    .finalOrder(FinalOrder.builder().finalOrderPayments(payments).build())
                    .build()
            ).build();
    }
}
