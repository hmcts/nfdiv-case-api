package uk.gov.hmcts.divorce.systemupdate.event;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CFT_TTL_MANAGER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.TTL_PROFILE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SystemUpdateTTL implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_UPDATE_TTL = "system-update-TTL";
    private static final LocalDate CURRENT_DATE_PLUS_SIX_MONTH = LocalDate.now().plusMonths(6);

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SYSTEM_UPDATE_TTL)
            .forStates(Draft, AwaitingPayment)
            .name("Resolve time to live")
            .showCondition(NEVER_SHOW)
            .aboutToStartCallback(this::aboutToStart)
            .description("Resolve time to live")
            .grant(CREATE_READ_UPDATE, TTL_PROFILE));

        configBuilder.caseRoleToAccessProfile(CFT_TTL_MANAGER).accessProfiles("TTL_profile").build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("{} about to start callback invoked for Case Id: {}", SYSTEM_UPDATE_TTL, details.getId());
        final CaseData caseData = details.getData();

        if (caseData.getTimeToLive() == null && details.getState() == Draft) {
            caseData.setTimeToLive(TTL.builder().systemTTL(CURRENT_DATE_PLUS_SIX_MONTH)
                    .suspended(YesOrNo.NO)
                    .build());
        } else {
            caseData.setTimeToLive(null);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }
}
