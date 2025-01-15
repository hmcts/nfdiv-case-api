package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration.NEVER_SHOW;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class SystemSetTimeToLive implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_SET_TTL = "system-set-ttl";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_SET_TTL)
            .forStates(Draft)
            .showCondition(NEVER_SHOW)
            .name("System set TTL")
            .description("System set TTL")
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("System set TTL about to submit callback invoked CaseID: {}", details.getId());

        LocalDate deleteDate = details.getLastModified().plusMonths(6).toLocalDate();

        details.getData().setRetainAndDisposeTimeToLive(
            TTL.builder()
                .overrideTTL(null)
                .suspended(YesOrNo.NO)
                .systemTTL(deleteDate)
                .build()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}

