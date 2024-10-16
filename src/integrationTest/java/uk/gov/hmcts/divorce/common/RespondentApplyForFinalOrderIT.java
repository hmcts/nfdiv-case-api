package uk.gov.hmcts.divorce.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationService;
import uk.gov.hmcts.divorce.testutil.FeesWireMock;
import uk.gov.hmcts.divorce.testutil.PaymentWireMock;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.Clock;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.RespondentApplyForFinalOrder.RESPONDENT_APPLY_FINAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.notification.EmailTemplateName.SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_GENERAL;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_NOTICE;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.FeesWireMock.stubForFeesLookup;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.buildServiceReferenceRequest;
import static uk.gov.hmcts.divorce.testutil.PaymentWireMock.stubCreateServiceRequest;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SUBMITTED_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_2_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {
    PaymentWireMock.PropertiesInitializer.class,
    FeesWireMock.PropertiesInitializer.class
})
public class RespondentApplyForFinalOrderIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private Clock clock;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeAll
    static void setUp() {
        PaymentWireMock.start();
        FeesWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        PaymentWireMock.stopAndReset();
        FeesWireMock.stopAndReset();
    }

    @Test
    void shouldChangeStateToAwaitingFinalOrderPaymentIfRespondentWillMakePayment() throws Exception {
        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.NO);
        final CaseData data = caseDetails.getData();

        stubForFeesLookup(TestDataHelper.getFeeResponseAsJson(), EVENT_GENERAL, SERVICE_OTHER, KEYWORD_NOTICE);
        stubCreateServiceRequest(OK, buildServiceReferenceRequest(data, data.getApplicant2()));

        performRespondentApplyForFinalRequest(caseDetails.getData(), ABOUT_TO_SUBMIT_URL)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(AwaitingFinalOrderPayment.name()))
            .andExpect(jsonPath("$.data.applicant2FinalOrderFeeInPounds").value("10"))
            .andExpect(
                jsonPath("$.data.applicant2FinalOrderFeeServiceRequestReference").value(TEST_SERVICE_REFERENCE)
            );
    }

    @Test
    void shouldNotSendAppliedForFinalOrderNotificationsIfRespondentWillMakePayment() throws Exception {
        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.NO);

        performRespondentApplyForFinalRequest(caseDetails.getData(), SUBMITTED_URL).andExpect(status().isOk());

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldChangeStateToFinalOrderRequestedIfRespondentRequestsHwf() throws Exception {
        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.YES);

        performRespondentApplyForFinalRequest(caseDetails.getData(), ABOUT_TO_SUBMIT_URL)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value(RespondentFinalOrderRequested.name()))
            .andExpect(jsonPath("$.data.applicant1AppliedForFinalOrderFirst").value("No"))
            .andExpect(jsonPath("$.data.applicant2AppliedForFinalOrderFirst").value("Yes"));
    }

    @Test
    void shouldSendAppliedForFinalOrderNotificationsIfRespondentRequestsHwf() throws Exception {
        setMockClock(clock);

        final CaseDetails<CaseData, State> caseDetails = buildTestDataWithHwfAnswer(YesOrNo.YES);

        performRespondentApplyForFinalRequest(caseDetails.getData(), SUBMITTED_URL).andExpect(status().isOk());

        verify(notificationService).sendEmail(
            eq(TEST_APPLICANT_2_USER_EMAIL), eq(SOLE_RESPONDENT_APPLIED_FOR_FINAL_ORDER), anyMap(), eq(ENGLISH), anyLong()
        );
        verifyNoMoreInteractions(notificationService);
    }

    private ResultActions performRespondentApplyForFinalRequest(CaseData caseData, String url) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.post(url)
            .contentType(APPLICATION_JSON)
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
