package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Hearing;
import uk.gov.hmcts.divorce.divorcecase.model.HearingAttendance;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerListForHearing.CASEWORKER_LIST_FOR_HEARING;
import static uk.gov.hmcts.divorce.divorcecase.model.State.PendingHearingDate;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerListForHearingTest {
    @InjectMocks
    private CaseworkerListForHearing caseworkerListForHearing;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerListForHearing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_LIST_FOR_HEARING);
    }

    @Test
    void shouldResetGeneralApplicationWhenAboutToStartCallbackTriggered() {
        final CaseData caseData = caseData();
        caseData.setHearing(Hearing.builder()
            .venueOfHearing("Test")
            .dateOfHearing(LOCAL_DATE_TIME)
            .hearingAttendance(Set.of(HearingAttendance.IN_PERSON))
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setState(PendingHearingDate);
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerListForHearing.aboutToStart(details);

        assertThat(response.getData().getHearing().getDateOfHearing()).isNull();
        assertThat(response.getData().getHearing().getVenueOfHearing()).isNull();
        assertThat(response.getData().getHearing().getHearingAttendance()).isNull();
    }
}
