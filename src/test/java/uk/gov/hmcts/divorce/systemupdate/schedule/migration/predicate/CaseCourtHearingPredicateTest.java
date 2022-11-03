package uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BIRMINGHAM;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseCourtHearingPredicateTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private CaseCourtHearingPredicate caseCourtHearingPredicate;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldReturnTrueIfCourtHearingDateIsNotTheSameAsBulkCase() {

        final LocalDateTime localDateTime = now();
        final LocalDateTime dateAndTimeOfHearing = now().plusDays(1);
        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(dateAndTimeOfHearing)
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final ListValue<BulkListCaseDetails> listValue = new ListValue<BulkListCaseDetails>("1", BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(TEST_CASE_ID.toString())
                .build())
            .build());

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(localDateTime)
            .build();

        when(coreCaseDataApi.getCase(user.getAuthToken(), SERVICE_AUTHORIZATION, TEST_CASE_ID.toString()))
            .thenReturn(reformCaseDetails);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails)).thenReturn(caseDetails);

        assertThat(caseCourtHearingPredicate
            .caseHearingIsNotSet(bulkActionCaseData, user, SERVICE_AUTHORIZATION)
            .test(listValue))
            .isTrue();
    }

    @Test
    void shouldReturnTrueIfCourtHearingDateIsNull() {

        final LocalDateTime localDateTime = now();
        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final ListValue<BulkListCaseDetails> listValue = new ListValue<BulkListCaseDetails>("1", BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(TEST_CASE_ID.toString())
                .build())
            .build());

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(localDateTime)
            .build();

        when(coreCaseDataApi.getCase(user.getAuthToken(), SERVICE_AUTHORIZATION, TEST_CASE_ID.toString()))
            .thenReturn(reformCaseDetails);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails)).thenReturn(caseDetails);

        assertThat(caseCourtHearingPredicate
            .caseHearingIsNotSet(bulkActionCaseData, user, SERVICE_AUTHORIZATION)
            .test(listValue))
            .isTrue();
    }

    @Test
    void shouldReturnTrueIfCourtIsNotTheSameAsBulkCase() {

        final LocalDateTime localDateTime = now();
        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(localDateTime)
                .court(BIRMINGHAM)
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final ListValue<BulkListCaseDetails> listValue = new ListValue<BulkListCaseDetails>("1", BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(TEST_CASE_ID.toString())
                .build())
            .build());

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(localDateTime)
            .court(BURY_ST_EDMUNDS)
            .build();

        when(coreCaseDataApi.getCase(user.getAuthToken(), SERVICE_AUTHORIZATION, TEST_CASE_ID.toString()))
            .thenReturn(reformCaseDetails);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails)).thenReturn(caseDetails);

        assertThat(caseCourtHearingPredicate
            .caseHearingIsNotSet(bulkActionCaseData, user, SERVICE_AUTHORIZATION)
            .test(listValue))
            .isTrue();
    }

    @Test
    void shouldReturnTrueIfCourtIsNull() {

        final LocalDateTime localDateTime = now();
        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(localDateTime)
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final ListValue<BulkListCaseDetails> listValue = new ListValue<BulkListCaseDetails>("1", BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(TEST_CASE_ID.toString())
                .build())
            .build());

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(localDateTime)
            .court(BIRMINGHAM)
            .build();

        when(coreCaseDataApi.getCase(user.getAuthToken(), SERVICE_AUTHORIZATION, TEST_CASE_ID.toString()))
            .thenReturn(reformCaseDetails);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails)).thenReturn(caseDetails);

        assertThat(caseCourtHearingPredicate
            .caseHearingIsNotSet(bulkActionCaseData, user, SERVICE_AUTHORIZATION)
            .test(listValue))
            .isTrue();
    }

    @Test
    void shouldReturnFalseIfCourtHearingDateAndCourtAreTheSameAsBulkCase() {

        final LocalDateTime localDateTime = now();
        final CaseDetails reformCaseDetails = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(localDateTime)
                .court(BIRMINGHAM)
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final ListValue<BulkListCaseDetails> listValue = new ListValue<BulkListCaseDetails>("1", BulkListCaseDetails.builder()
            .caseReference(CaseLink.builder()
                .caseReference(TEST_CASE_ID.toString())
                .build())
            .build());

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(localDateTime)
            .court(BIRMINGHAM)
            .build();

        when(coreCaseDataApi.getCase(user.getAuthToken(), SERVICE_AUTHORIZATION, TEST_CASE_ID.toString()))
            .thenReturn(reformCaseDetails);
        when(caseDetailsConverter.convertToCaseDetailsFromReformModel(reformCaseDetails)).thenReturn(caseDetails);

        assertThat(caseCourtHearingPredicate
            .caseHearingIsNotSet(bulkActionCaseData, user, SERVICE_AUTHORIZATION)
            .test(listValue))
            .isFalse();
    }
}