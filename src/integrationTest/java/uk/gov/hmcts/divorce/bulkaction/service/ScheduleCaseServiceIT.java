package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CreateBulkList.CREATE_BULK_LIST;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.CASE_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithCourtHearing.SYSTEM_UPDATE_CASE_COURT_HEARING;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemUpdateCaseWithPronouncementJudge.SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE;
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
public class ScheduleCaseServiceIT {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private ScheduleCaseService scheduleCaseService;

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
            .eventId(CREATE_BULK_LIST)
            .token("startEventToken")
            .caseDetails(getCaseCourtHearingDetails())
            .build();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_UPDATE_CASE_COURT_HEARING))
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
        )).thenReturn(getCaseCourtHearingDetails());

        scheduleCaseService.updateCourtHearingDetailsForCasesInBulk(bulkActionCaseDetails, CASEWORKER_AUTH_TOKEN);

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_UPDATE_CASE_COURT_HEARING
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

    @Test
    void shouldSuccessfullyUpdatePronouncementJudgeDetailsForCasesInBulk() {

        final LocalDateTime dateAndTimeOfHearing = LocalDateTime.of(2021, 11, 10, 0, 0, 0);

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .pronouncementJudge("District Judge")
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
            .eventId(CREATE_BULK_LIST)
            .token("startEventToken")
            .caseDetails(getCasePronouncementDetails())
            .build();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE))
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
        )).thenReturn(getCasePronouncementDetails());

        scheduleCaseService.updatePronouncementJudgeDetailsForCasesInBulk(bulkActionCaseDetails, CASEWORKER_AUTH_TOKEN);

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                CASEWORKER_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                CASEWORKER_USER_ID,
                JURISDICTION,
                CASE_TYPE,
                TEST_CASE_ID.toString(),
                SYSTEM_UPDATE_CASE_PRONOUNCEMENT_JUDGE
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


    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getCaseCourtHearingDetails() {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .data(Map.of("dateAndTimeOfHearing", "2021-01-18'T'00:00:00.000",
                "courtName", "serviceCentre"))
            .build();
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getCasePronouncementDetails() {
        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .data(Map.of("pronouncementJudge", "District Judge"))
            .build();
    }
}
