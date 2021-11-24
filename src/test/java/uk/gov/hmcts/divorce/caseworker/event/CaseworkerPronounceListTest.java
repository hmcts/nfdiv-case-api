package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.service.CasePronouncementService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPronounceList.CASEWORKER_PRONOUNCE_LIST;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class CaseworkerPronounceListTest {

    @Mock
    private CasePronouncementService casePronouncementService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Clock clock;

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
    void shouldPopulateBulkActionCaseDataFieldsIfHearingDateIsBeforeCurrentDateForAboutToSubmitCallback() {

        setMockClock(clock);
        final LocalDateTime dateAndTimeOfHearing = getExpectedLocalDateTime().minusMinutes(1L);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getPronouncedDate()).isEqualTo(dateAndTimeOfHearing.toLocalDate());
        assertThat(result.getData().getDateFinalOrderEligibleFrom())
            .isEqualTo(result.getData().getDateFinalOrderEligibleFrom(dateAndTimeOfHearing));
    }

    @Test
    void shouldPopulateBulkActionCaseDataFieldsIfHearingDateEqualsCurrentDateForAboutToSubmitCallback() {

        setMockClock(clock);
        final LocalDateTime dateAndTimeOfHearing = getExpectedLocalDateTime();

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getPronouncedDate()).isEqualTo(dateAndTimeOfHearing.toLocalDate());
        assertThat(result.getData().getDateFinalOrderEligibleFrom())
            .isEqualTo(result.getData().getDateFinalOrderEligibleFrom(dateAndTimeOfHearing));
    }

    @Test
    void shouldReturnErrorIfHearingDateIsAfterCurrentDateForAboutToSubmitCallback() {

        setMockClock(clock);
        final LocalDateTime dateAndTimeOfHearing = getExpectedLocalDateTime().plusMinutes(1L);

        final CaseDetails<BulkActionCaseData, BulkActionState> caseDetails = new CaseDetails<>();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .build();
        caseDetails.setData(bulkActionCaseData);

        final var result = caseworkerPronounceList.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getPronouncedDate()).isNull();
        assertThat(result.getData().getDateFinalOrderEligibleFrom()).isNull();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0)).isEqualTo("You cannot pronounce a case before the hearing date");
    }

    @Test
    void shouldUpdateBulkCaseAfterBulkTriggerForSubmittedCallback() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(1L);

        when(request.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        doNothing().when(casePronouncementService).pronounceCases(details, CASEWORKER_AUTH_TOKEN);

        SubmittedCallbackResponse submittedCallbackResponse = caseworkerPronounceList.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(casePronouncementService).pronounceCases(details, CASEWORKER_AUTH_TOKEN);
    }
}
