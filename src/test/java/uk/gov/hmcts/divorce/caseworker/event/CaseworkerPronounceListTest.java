package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.BulkTriggerService;
import uk.gov.hmcts.divorce.bulkaction.service.ListValueUtil;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.service.task.PronounceCase;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SystemUpdateCaseErrors.SYSTEM_BULK_CASE_ERRORS;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPronounceList.CASEWORKER_PRONOUNCE_LIST;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseworkerPronounceListTest {

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private PronounceCase pronounceCase;

    @Mock
    private ListValueUtil listValueUtil;

    @InjectMocks
    private CaseworkerPronounceList caseworkerPronounceList;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        caseworkerPronounceList.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PRONOUNCE_LIST);
    }

    @Test
    void shouldReturnWithNoErrorIfHasJudgePronouncedIsYesForMidEventCallback() {

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .hasJudgePronounced(YES)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.midEvent(caseDetails, caseDetails);

        assertThat(result.getErrors()).isNull();
    }

    @Test
    void shouldReturnWithErrorIfHasJudgePronouncedIsNoForMidEventCallback() {

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .hasJudgePronounced(NO)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.midEvent(caseDetails, caseDetails);

        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).isEqualTo("The judge must have pronounced to continue.");
    }

    @Test
    void shouldPopulateBulkActionCaseDataFieldsForAboutToSubmitCallback() {
        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(LocalDateTime.now())
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getState()).isEqualTo(Pronounced);
        assertThat(result.getData().getPronouncedDate()).isEqualTo(LocalDate.now());
        assertThat(result.getData().getDateFinalOrderEligibleFrom()).isEqualTo(LocalDate.now().plusWeeks(6).plusDays(1));
    }

    @Test
    void shouldUpdateBulkCaseAfterBulkTriggerForSubmittedCallback() {
        final BulkListCaseDetails bulkListCaseDetails1 = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("1")
                .build())
            .build();
        final BulkListCaseDetails bulkListCaseDetails2 = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("2")
                .build())
            .build();
        ListValue<BulkListCaseDetails> listValue1 = ListValue.<BulkListCaseDetails>builder()
            .id(UUID.randomUUID().toString())
            .value(bulkListCaseDetails1)
            .build();
        ListValue<BulkListCaseDetails> listValue2 = ListValue.<BulkListCaseDetails>builder()
            .id(UUID.randomUUID().toString())
            .value(bulkListCaseDetails2)
            .build();
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = Arrays.asList(listValue1, listValue2);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var userDetails = UserDetails.builder()
            .email("test@test.com")
            .id("app1")
            .build();
        final var user = new User("token", userDetails);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN))).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(listValueUtil.fromListValueToList(any())).thenReturn(new ArrayList<>());
        when(listValueUtil.fromListToListValue(any())).thenReturn(List.of(ListValue.builder().build()));
        when(bulkTriggerService.bulkTrigger(
            new ArrayList<>(),
            SYSTEM_PRONOUNCE_CASE,
            pronounceCase,
            user,
            TEST_SERVICE_AUTH_TOKEN
        )).thenReturn(List.of(BulkListCaseDetails.builder().build(), BulkListCaseDetails.builder().build()));

        caseworkerPronounceList.submitted(caseDetails, caseDetails);

        verify(bulkTriggerService).bulkTrigger(
            new ArrayList<>(),
            SYSTEM_PRONOUNCE_CASE,
            pronounceCase,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );

        verify(ccdUpdateService).submitBulkActionEvent(
            caseDetails,
            SYSTEM_BULK_CASE_ERRORS,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );
    }

    @Test
    void shouldThrowErrorForSubmittedCallback() {
        final BulkListCaseDetails bulkListCaseDetails1 = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("1")
                .build())
            .build();
        final BulkListCaseDetails bulkListCaseDetails2 = BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference("2")
                .build())
            .build();
        ListValue<BulkListCaseDetails> listValue1 = ListValue.<BulkListCaseDetails>builder()
            .id(UUID.randomUUID().toString())
            .value(bulkListCaseDetails1)
            .build();
        ListValue<BulkListCaseDetails> listValue2 = ListValue.<BulkListCaseDetails>builder()
            .id(UUID.randomUUID().toString())
            .value(bulkListCaseDetails2)
            .build();
        final List<ListValue<BulkListCaseDetails>> bulkListCaseDetails = Arrays.asList(listValue1, listValue2);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(bulkListCaseDetails)
            .erroredCaseDetails(new ArrayList<>())
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var userDetails = UserDetails.builder()
            .email("test@test.com")
            .id("app1")
            .build();
        final var user = new User("token", userDetails);

        when(request.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(idamService.retrieveUser(eq(TEST_AUTHORIZATION_TOKEN))).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(listValueUtil.fromListValueToList(any())).thenReturn(new ArrayList<>());
        when(listValueUtil.fromListToListValue(any())).thenReturn(List.of(ListValue.builder().build()));
        when(bulkTriggerService.bulkTrigger(
            new ArrayList<>(),
            SYSTEM_PRONOUNCE_CASE,
            pronounceCase,
            user,
            TEST_SERVICE_AUTH_TOKEN
        )).thenReturn(List.of(BulkListCaseDetails.builder().build(), BulkListCaseDetails.builder().build()));
        doThrow(CcdManagementException.class)
            .when(ccdUpdateService).submitBulkActionEvent(
                caseDetails,
                SYSTEM_BULK_CASE_ERRORS,
                user,
                TEST_SERVICE_AUTH_TOKEN
            );

        caseworkerPronounceList.submitted(caseDetails, caseDetails);

        verify(bulkTriggerService).bulkTrigger(
            new ArrayList<>(),
            SYSTEM_PRONOUNCE_CASE,
            pronounceCase,
            user,
            TEST_SERVICE_AUTH_TOKEN
        );

        assertThrows(
            CcdManagementException.class,
            () -> ccdUpdateService.submitBulkActionEvent(
                caseDetails,
                SYSTEM_BULK_CASE_ERRORS,
                user,
                TEST_SERVICE_AUTH_TOKEN)
        );
    }
}
