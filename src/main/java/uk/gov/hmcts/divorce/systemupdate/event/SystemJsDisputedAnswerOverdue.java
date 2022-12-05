package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemJsDisputedAnswerOverdue implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_JS_DISPUTED_ANSWER_OVERDUE = "system-js-disputed-answer-overdue";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_JS_DISPUTED_ANSWER_OVERDUE)
            .forStateTransition(AwaitingAnswer, AwaitingJsNullity)
            .name("System JS Disputed Answer Overdue")
            .description("System JS Disputed Answer Overdue")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120);
    }
}
