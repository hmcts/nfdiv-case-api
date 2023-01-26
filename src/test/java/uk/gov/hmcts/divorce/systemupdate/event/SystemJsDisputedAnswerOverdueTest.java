package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemJsDisputedAnswerOverdue.SYSTEM_JS_DISPUTED_ANSWER_OVERDUE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(SpringExtension.class)
class SystemJsDisputedAnswerOverdueTest {

    @InjectMocks
    private SystemJsDisputedAnswerOverdue systemJsDisputedAnswerOverdue;

    @Test
    public void shouldContainEventConfig() {
        final ConfigBuilder<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemJsDisputedAnswerOverdue.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_JS_DISPUTED_ANSWER_OVERDUE);
    }

}
