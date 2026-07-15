package uk.gov.hmcts.divorce.solicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.ServicePaymentMethod;
import uk.gov.hmcts.divorce.solicitor.service.ServiceApplicationSubmitPaymentService;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorSubmitServiceApplication.SOLICITOR_SUBMIT_SERVICE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SolicitorSubmitServiceApplicationIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private ServiceApplicationSubmitPaymentService serviceApplicationSubmitPaymentService;

    @Test
    void shouldReturnErrorWhenSubmitPaymentFails() throws Exception {
        when(serviceApplicationSubmitPaymentService.processSubmitPayment(anyLong(), any()))
            .thenReturn(Optional.of("payment failed"));

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .interimApplicationOptions(InterimApplicationOptions.builder().build())
                .build())
            .alternativeService(AlternativeService.builder().build())
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_SUBMIT_SERVICE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]").value("payment failed"))
            .andExpect(jsonPath("$.state").doesNotExist());
    }

    @Test
    void shouldSetAwaitingServiceConsiderationAndCopyStatementFieldsWhenPaymentMethodIsPba() throws Exception {
        when(serviceApplicationSubmitPaymentService.processSubmitPayment(anyLong(), any()))
            .thenReturn(Optional.empty());

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimAppsStatementOfTruth(YesOrNo.YES)
            .interimAppsSignStatementOfTruth(YesOrNo.YES)
            .interimAppsStatementOfTruthSolsName("Test Solicitor")
            .interimAppsStatementOfTruthSolsFirm("Test Firm")
            .interimAppsStatementOfTruthComments("Additional comments")
            .build();

        AlternativeService alternativeService = AlternativeService.builder().build();
        alternativeService.getServicePaymentFee().setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_ACCOUNT);

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .interimApplicationOptions(options)
                .build())
            .alternativeService(alternativeService)
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_SUBMIT_SERVICE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("AwaitingServiceConsideration"))
            .andExpect(jsonPath("$.data.serviceApplicationStatementOfTruth").value("Yes"))
            .andExpect(jsonPath("$.data.serviceApplicationSignStatementOfTruth").value("Yes"))
            .andExpect(jsonPath("$.data.serviceApplicationStatementOfTruthSolsName").value("Test Solicitor"))
            .andExpect(jsonPath("$.data.serviceApplicationStatementOfTruthSolsFirm").value("Test Firm"))
            .andExpect(jsonPath("$.data.serviceApplicationStatementOfTruthComments").value("Additional comments"));
    }

    @Test
    void shouldSetAwaitingServicePaymentWhenPaymentMethodIsHwf() throws Exception {
        when(serviceApplicationSubmitPaymentService.processSubmitPayment(anyLong(), any()))
            .thenReturn(Optional.empty());

        AlternativeService alternativeService = AlternativeService.builder().build();
        alternativeService.getServicePaymentFee().setPaymentMethod(ServicePaymentMethod.FEE_PAY_BY_HWF);

        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .interimApplicationOptions(InterimApplicationOptions.builder().build())
                .build())
            .alternativeService(alternativeService)
            .build();

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(caseData, SOLICITOR_SUBMIT_SERVICE_APPLICATION)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.state").value("AwaitingServicePayment"));
    }
}
