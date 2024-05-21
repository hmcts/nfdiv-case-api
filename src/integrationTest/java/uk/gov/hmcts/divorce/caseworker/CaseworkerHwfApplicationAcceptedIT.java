package uk.gov.hmcts.divorce.caseworker;

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
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.config.WebMvcConfig;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDateTime;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerHwfApplicationAccepted.CASEWORKER_HWF_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getFormattedExpectedDateTime;
import static uk.gov.hmcts.divorce.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.callbackRequest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
public class CaseworkerHwfApplicationAcceptedIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WebMvcConfig webMvcConfig;

    @Test
    public void shouldSetCaseStatusToAwaitingDocumentsWhenSoleCaseAndApplicantWantToServeByAlternativeMeansToRespondent() throws Exception {

        final CaseData caseData = CaseData
                .builder()
                .applicationType(ApplicationType.SOLE_APPLICATION)
                .application(
                        Application
                                .builder()
                                .applicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES)
                                .applicant1KnowsApplicant2Address(YesOrNo.NO)
                                .build()
                )
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(
                        callbackRequest(
                            caseData,
                            CASEWORKER_HWF_APPLICATION_ACCEPTED)
                    )
                )
                .accept(APPLICATION_JSON))
            .andDo(print())
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.state").value(AwaitingDocuments.name()));
    }

    @Test
    public void shouldSetCaseStatusToSubmittedWhenSoleCaseAndRespondentAddressIsKnown() throws Exception {

        final CaseData caseData = CaseData
                .builder()
                .applicationType(ApplicationType.SOLE_APPLICATION)
                .applicant2(Applicant.builder()
                        .address(AddressGlobalUK.builder()
                                .addressLine1("line1")
                                .country("UK")
                                .build())
                        .build())
                .application(
                        Application
                                .builder()
                                .applicant1WantsToHavePapersServedAnotherWay(YesOrNo.NO)
                                .build()
                )
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                        callbackRequest(
                                                caseData,
                                                CASEWORKER_HWF_APPLICATION_ACCEPTED)
                                )
                        )
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.state").value(Submitted.name()));
    }

    @Test
    public void shouldSetCaseStatusToSubmittedWhenJointCase() throws Exception {

        final CaseData caseData = CaseData
                .builder()
                .applicationType(ApplicationType.JOINT_APPLICATION)
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                        callbackRequest(
                                                caseData,
                                                CASEWORKER_HWF_APPLICATION_ACCEPTED)
                                )
                        )
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.state").value(Submitted.name()));
    }

    @Test
    public void shouldNotSetDateSubmittedIfDateSubmittedAlreadySetButSetDueDateIfNull() throws Exception {
        final LocalDateTime submittedDateTime = getExpectedLocalDateTime();

        final CaseData caseData = CaseData
                .builder()
                .applicationType(ApplicationType.JOINT_APPLICATION)
                .application(
                        Application
                                .builder()
                                .dateSubmitted(submittedDateTime)
                                .build()
                )
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                        callbackRequest(
                                                caseData,
                                                CASEWORKER_HWF_APPLICATION_ACCEPTED)
                                )
                        )
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.state").value(Submitted.name()))
                .andExpect(
                        jsonPath("$.data.dateSubmitted").value(getFormattedExpectedDateTime())
                )
                .andExpect(
                        jsonPath("$.data.dueDate").value(getExpectedLocalDate().plusDays(28).toString())
                );
    }

    @Test
    public void shouldNotSetDueDateIfDueDateAlreadySetButSetDateSubmittedIfNull() throws Exception {
        final CaseData caseData = CaseData
                .builder()
                .applicationType(ApplicationType.JOINT_APPLICATION)
                .application(
                        Application
                                .builder()
                                .build()
                )
                .dueDate(getExpectedLocalDate())
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .contentType(APPLICATION_JSON)
                        .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                        .content(objectMapper.writeValueAsString(
                                        callbackRequest(
                                                caseData,
                                                CASEWORKER_HWF_APPLICATION_ACCEPTED)
                                )
                        )
                        .accept(APPLICATION_JSON))
                .andDo(print())
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.state").value(Submitted.name()))
                .andExpect(
                        jsonPath("$.data.dateSubmitted").exists()
                )
                .andExpect(
                        jsonPath("$.data.dateSubmitted").isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.data.dueDate").value(getExpectedLocalDate().toString())
                );
    }
}
