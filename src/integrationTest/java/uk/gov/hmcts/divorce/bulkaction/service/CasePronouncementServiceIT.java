package uk.gov.hmcts.divorce.bulkaction.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCaseDataContentProvider;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles("test")
public class CasePronouncementServiceIT {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private CasePronouncementService casePronouncementService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @MockBean
    private IdamService idamService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Test
    void shouldSuccessfullyUpdateCourtHearingDetailsForCasesInBulk() {

        final LocalDateTime dateAndTimeOfHearing = LocalDateTime.of(2021, 11, 10, 0, 0, 0);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .courtName(Court.SERVICE_CENTRE)
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue(TEST_CASE_ID.toString())))
            .build();


        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .data(bulkActionCaseData)
            .build();

        var userDetails = UserDetails.builder().id(CASEWORKER_USER_ID).build();
        var user = new User(CASEWORKER_AUTH_TOKEN, userDetails);
        when(idamService.retrieveUser(CASEWORKER_AUTH_TOKEN)).thenReturn(user);

        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(SYSTEM_PRONOUNCE_CASE)
            .token("startEventToken")
            .caseDetails(getCaseDetails())
            .build();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_PRONOUNCE_CASE))
            .thenReturn(startEventResponse);

        final CaseDataContent caseDataContent = mock(CaseDataContent.class);

        when(ccdCaseDataContentProvider
            .createCaseDataContent(
                eq(startEventResponse),
                eq(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY),
                eq(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION),
                any(CaseData.class)))
            .thenReturn(caseDataContent);

        when(coreCaseDataApi.submitEventForCaseWorker(
            SYSTEM_UPDATE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION,
            SYSTEM_USER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent
        )).thenReturn(getCaseDetails());

        casePronouncementService.pronounceCases(bulkActionCaseDetails, CASEWORKER_AUTH_TOKEN);

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_PRONOUNCE_CASE
            );

        verify(ccdCaseDataContentProvider)
            .createCaseDataContent(
                eq(startEventResponse),
                eq(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY),
                eq(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION),
                any(CaseData.class));

        verify(coreCaseDataApi).submitEventForCaseWorker(
            CASEWORKER_AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            CASEWORKER_USER_ID,
            JURISDICTION,
            CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent
        );
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getCaseDetails() {
        CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(LocalDateTime.of(2021, 10, 26, 10, 0, 0))
                .build())
            .finalOrder(FinalOrder.builder().build())
            .build();

        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .data(objectMapper.convertValue(caseData, new TypeReference<>(){}))
            .build();
    }
}
