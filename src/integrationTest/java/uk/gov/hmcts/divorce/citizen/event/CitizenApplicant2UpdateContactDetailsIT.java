package uk.gov.hmcts.divorce.citizen.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateApplication;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2UpdateContactDetails.CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTH_HEADER_VALUE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequestBeforeAndAfter;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithOrderSummary;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CitizenApplicant2UpdateContactDetailsIT {

    private static final AddressGlobalUK ADDRESS1 = AddressGlobalUK.builder()
        .addressLine1("100 The Street")
        .postTown("The town")
        .county("County Durham")
        .country("England")
        .postCode("XXXXXX")
        .build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CcdAccessService ccdAccessService;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @MockBean
    private DivorceApplicationRemover divorceApplicationRemover;

    @MockBean
    private GenerateApplication generateApplication;

    @BeforeEach
    public void setUp() {
        when(ccdAccessService.isApplicant1(anyString(), anyLong())).thenReturn(false);
    }

    @Test
    public void shouldUpdateApplicant2AddressAndRegenerateDivorceApplicationBeforeSubmittingAoS() throws Exception {
        CaseData updatedData = caseDataWithOrderSummary();
        updatedData.getApplicant2().setAddress(ADDRESS1);
        updatedData.getApplicant2().setPhoneNumber("123456789");

        CallbackRequest callbackRequest =
            callbackRequestBeforeAndAfter(caseDataWithOrderSummary(), updatedData, CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS);

        callbackRequest.getCaseDetails().setState(State.AwaitingAos.name());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2PhoneNumber").value("123456789"))
            .andExpect(jsonPath("$.data.applicant2Address.AddressLine1").value("100 The Street"))
            .andExpect(jsonPath("$.data.applicant2Address.PostTown").value("The town"))
            .andExpect(jsonPath("$.data.applicant2Address.County").value("County Durham"))
            .andExpect(jsonPath("$.data.applicant2Address.PostCode").value("XXXXXX"))
            .andExpect(jsonPath("$.data.applicant2Address.Country").value("England"));

        verify(divorceApplicationRemover).apply(any());
        verify(generateApplication).apply(any());
    }

    @Test
    public void shouldNotRegenerateDivorceApplicationAfterSubmittingAoS() throws Exception {
        CaseData updatedData = caseDataWithOrderSummary();
        updatedData.getApplicant2().setAddress(ADDRESS1);

        CallbackRequest callbackRequest =
            callbackRequestBeforeAndAfter(caseDataWithOrderSummary(), updatedData, CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS);

        callbackRequest.getCaseDetails().setState(State.Holding.name());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2Address.AddressLine1").value("100 The Street"))
            .andExpect(jsonPath("$.data.applicant2Address.PostTown").value("The town"))
            .andExpect(jsonPath("$.data.applicant2Address.County").value("County Durham"))
            .andExpect(jsonPath("$.data.applicant2Address.PostCode").value("XXXXXX"))
            .andExpect(jsonPath("$.data.applicant2Address.Country").value("England"));

        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    @Test
    public void shouldUpdateApplicant2PhoneNumberAndNotRegenerateDivorceApplication() throws Exception {
        CaseData updatedData = caseDataWithOrderSummary();
        updatedData.getApplicant2().setPhoneNumber("123456789");

        CallbackRequest callbackRequest =
            callbackRequestBeforeAndAfter(caseDataWithOrderSummary(), updatedData, CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS);

        callbackRequest.getCaseDetails().setState(State.AwaitingAos.name());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2PhoneNumber").value("123456789"));

        verifyNoInteractions(divorceApplicationRemover);
        verifyNoInteractions(generateApplication);
    }

    @Test
    public void shouldRegenerateDivorceApplicationWhenContactPrivacyChangedBeforeSubmittingAoS() throws Exception {
        CaseData updatedData = caseDataWithOrderSummary();
        updatedData.getApplicant2().setContactDetailsType(ContactDetailsType.PRIVATE);

        CallbackRequest callbackRequest =
            callbackRequestBeforeAndAfter(caseDataWithOrderSummary(), updatedData, CITIZEN_APPLICANT2_UPDATE_CONTACT_DETAILS);

        callbackRequest.getCaseDetails().setState(State.AwaitingAos.name());

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, AUTH_HEADER_VALUE)
                .header(AUTHORIZATION, AUTH_HEADER_VALUE)
                .content(objectMapper.writeValueAsString(callbackRequest))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicant2ContactDetailsType").value("private"));

        verify(divorceApplicationRemover).apply(any());
        verify(generateApplication).apply(any());
    }
}
