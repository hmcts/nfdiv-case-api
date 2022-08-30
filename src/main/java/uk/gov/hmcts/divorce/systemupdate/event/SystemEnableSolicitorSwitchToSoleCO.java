package uk.gov.hmcts.divorce.systemupdate.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class SystemEnableSolicitorSwitchToSoleCO implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_ENABLE_SWITCH_TO_SOLE_CO = "system-enable-switch-to-sole-co";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(SYSTEM_ENABLE_SWITCH_TO_SOLE_CO)
            .forState(ConditionalOrderPending)
            .name("Enable Solicitor Switch to Sole CO")
            .description("Enable Solicitor Switch to Sole CO")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData caseData = details.getData();
        final ConditionalOrder conditionalOrder = caseData.getConditionalOrder();

        if (conditionalOrder.shouldEnableSwitchToSoleCoForApplicant1Solicitor()) {
            caseData.getConditionalOrder().getConditionalOrderApplicant1Questions().setEnableSolicitorSwitchToSoleCo(YES);
        } else if (conditionalOrder.shouldEnableSwitchToSoleCoForApplicant2Solicitor()) {
            caseData.getConditionalOrder().getConditionalOrderApplicant2Questions().setEnableSolicitorSwitchToSoleCo(YES);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
