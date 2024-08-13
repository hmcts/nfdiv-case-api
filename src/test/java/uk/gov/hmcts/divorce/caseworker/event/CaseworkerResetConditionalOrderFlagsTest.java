package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.service.task.ResetConditionalOrderFlags;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerResetConditionalOrderFlags.CASEWORKER_RESET_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerResetConditionalOrderFlags.WARNING_RESET_CONDITIONAL_ORDER_FLAGS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerResetConditionalOrderFlagsTest {
    @Mock
    private ResetConditionalOrderFlags resetConditionalOrderFlags;

    @InjectMocks
    private CaseworkerResetConditionalOrderFlags caseworkerResetConditionalOrderFlags;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerResetConditionalOrderFlags.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RESET_CONDITIONAL_ORDER);
    }

    @Test
    void shouldResetConditionalOrderFlagsByDelegatingToCaseTask() {
        final var caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingConditionalOrder);

        when(resetConditionalOrderFlags.apply(caseDetails)).thenReturn(caseDetails);

        caseworkerResetConditionalOrderFlags.aboutToSubmit(caseDetails, caseDetails);

        verify(resetConditionalOrderFlags).apply(caseDetails);
    }

    @Test
    void shouldWarnUserWhenTheyTriggerTheEvent() {
        final var caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingConditionalOrder);

        when(resetConditionalOrderFlags.apply(caseDetails)).thenReturn(caseDetails);

        var response = caseworkerResetConditionalOrderFlags.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getWarnings()).isEqualTo(Collections.singletonList(WARNING_RESET_CONDITIONAL_ORDER_FLAGS));
    }
}
