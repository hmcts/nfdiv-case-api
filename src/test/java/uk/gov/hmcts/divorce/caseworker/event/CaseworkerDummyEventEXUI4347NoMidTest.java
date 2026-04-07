package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerDummyEventEXUI4347.EXUI_ISSUE_ID;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerDummyEventEXUI4347NoMid.CASEWORKER_DUMMY_EVENT_EXUI_4347_NO_MID_EVENT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@Slf4j
@ExtendWith(MockitoExtension.class)
class CaseworkerDummyEventEXUI4347NoMidTest {

    @InjectMocks
    private CaseworkerDummyEventEXUI4347NoMid caseworkerDummyEventEXUI4347NoMid;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerDummyEventEXUI4347NoMid.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_DUMMY_EVENT_EXUI_4347_NO_MID_EVENT);
    }

    @Test
    void shouldSetDummyStringInAboutToStart() {
        final CaseData caseData = caseData();
        caseData.setDummyString(EXUI_ISSUE_ID);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerDummyEventEXUI4347NoMid.aboutToStart(caseDetails);

        assertThat(response.getData().getDummyString()).isEqualTo(EXUI_ISSUE_ID);
    }

    @Test
    void shouldAlwaysThrowErrorToPreventCaseDataAlteration() {
        final CaseData caseData = caseData();
        caseData.setDummyString(EXUI_ISSUE_ID);
        caseData.setDummySetDateAutomatically(YES);
        caseData.setDummyDate(LocalDate.now());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerDummyEventEXUI4347NoMid.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).isEqualTo("Always Throw Error To Preserve Test Case Data");
    }

    @Test
    void shouldThrowErrorsWhenDummyStringNotSet() {
        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerDummyEventEXUI4347NoMid.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors().get(0)).isEqualTo("Always Throw Error To Preserve Test Case Data");
        assertThat(response.getErrors().get(1)).isEqualTo("aboutToSubmit Callback: Dummy String Was Originally: null Dummy String is Now: null Dummy String Should Be: EXUI-3839-4347");
    }
}
