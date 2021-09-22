package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemApplicant1ApplyForConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER = "system-notify-applicant1-conditional-order";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_NOTIFY_APPLICANT1_CONDITIONAL_ORDER)
            .forState(AwaitingConditionalOrder)
            .name("Applicant 1 Conditional Order")
            .description("Notify Applicant 1 they can apply for a Conditional Order")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();
        data.getApplication().setApplicant1NotifiedCanApplyForConditionalOrder(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
