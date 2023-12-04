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
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCase.SYSTEM_UPDATE_BULK_CASE;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.JURISDICTION;
import static uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce.getCaseType;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRemoveBulkCase.SYSTEM_REMOVE_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getCaseLinkListValue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
@ActiveProfiles("test")
public class CaseRemovalServiceIT {

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private IdamService idamService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseRemovalService caseRemovalService;

    @Test
    void shouldSuccessfullyRemoveCasesInBulkList() {

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .bulkListCaseDetails(
                List.of(
                    getBulkListCaseDetailsListValue("5"),
                    getBulkListCaseDetailsListValue("2")
                )
            )
            .casesToBeRemoved(List.of(getBulkListCaseDetailsListValue("5")))
            .casesAcceptedToListForHearing(List.of(getCaseLinkListValue("5")))
            .build();

        final var bulkActionCaseDetails = CaseDetails
            .<BulkActionCaseData, BulkActionState>builder()
            .id(2L)
            .data(bulkActionCaseData)
            .build();

        var userDetails = UserInfo.builder().uid(SYSTEM_USER_USER_ID).build();
        var user = new User(SYSTEM_UPDATE_AUTH_TOKEN, userDetails);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(SYSTEM_UPDATE_BULK_CASE)
            .token("startEventToken")
            .caseDetails(getBulkCaseDetails())
            .build();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                bulkActionCaseDetails.getId().toString(),
                SYSTEM_UPDATE_BULK_CASE))
            .thenReturn(startEventResponse);

        final StartEventResponse startEventResponseNFD = StartEventResponse.builder()
            .eventId(SYSTEM_REMOVE_BULK_CASE)
            .token("startEventToken")
            .caseDetails(getCaseDetails())
            .build();

        when(coreCaseDataApi
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                getCaseType(),
                "5",
                SYSTEM_REMOVE_BULK_CASE))
            .thenReturn(startEventResponseNFD);

        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(SYSTEM_USER_USER_ID),
            eq(JURISDICTION),
            eq(getCaseType()),
            eq("5"),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(getCaseDetails());

        when(coreCaseDataApi.submitEventForCaseWorker(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(SYSTEM_USER_USER_ID),
            eq(JURISDICTION),
            eq(BulkActionCaseTypeConfig.getCaseType()),
            eq(bulkActionCaseDetails.getId().toString()),
            eq(true),
            any(CaseDataContent.class)
        )).thenReturn(getBulkCaseDetails());

        caseRemovalService.removeCases(bulkActionCaseDetails);

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                BulkActionCaseTypeConfig.getCaseType(),
                bulkActionCaseDetails.getId().toString(),
                SYSTEM_UPDATE_BULK_CASE
            );

        verify(coreCaseDataApi)
            .startEventForCaseWorker(
                SYSTEM_UPDATE_AUTH_TOKEN,
                TEST_SERVICE_AUTH_TOKEN,
                SYSTEM_USER_USER_ID,
                JURISDICTION,
                getCaseType(),
                "5",
                SYSTEM_REMOVE_BULK_CASE
            );

        verify(coreCaseDataApi).submitEventForCaseWorker(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(SYSTEM_USER_USER_ID),
            eq(JURISDICTION),
            eq(getCaseType()),
            eq("5"),
            eq(true),
            any(CaseDataContent.class)
        );

        verify(coreCaseDataApi).submitEventForCaseWorker(
            eq(SYSTEM_UPDATE_AUTH_TOKEN),
            eq(TEST_SERVICE_AUTH_TOKEN),
            eq(SYSTEM_USER_USER_ID),
            eq(JURISDICTION),
            eq(BulkActionCaseTypeConfig.getCaseType()),
            eq(bulkActionCaseDetails.getId().toString()),
            eq(true),
            any(CaseDataContent.class)
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
            .data(objectMapper.convertValue(caseData, new TypeReference<>() {
            }))
            .id(5L)
            .build();
    }

    private uk.gov.hmcts.reform.ccd.client.model.CaseDetails getBulkCaseDetails() {

        final var bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2021, 11, 10, 0, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .bulkListCaseDetails(
                List.of(
                    getBulkListCaseDetailsListValue("5"),
                    getBulkListCaseDetailsListValue("2")
                )
            )
            .casesToBeRemoved(List.of(getBulkListCaseDetailsListValue("5")))
            .casesAcceptedToListForHearing(List.of(getCaseLinkListValue("5")))
            .build();

        return uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .data(objectMapper.convertValue(bulkActionCaseData, new TypeReference<>() {
            }))
            .id(2L)
            .build();
    }
}
