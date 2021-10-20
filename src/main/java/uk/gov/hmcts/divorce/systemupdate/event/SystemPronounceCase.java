package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDate;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SystemPronounceCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String SYSTEM_PRONOUNCE_CASE = "system-pronounce-case";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        new PageBuilder(configBuilder
            .event(SYSTEM_PRONOUNCE_CASE)
            .forStateTransition(AwaitingPronouncement, ConditionalOrderPronounced)
            .name("System pronounce case")
            .description("System pronounce case")
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grant(READ, SOLICITOR, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();

        data.getConditionalOrder().setGrantedDate(data.getConditionalOrder().getDateAndTimeOfHearing().toLocalDate());
        data.getFinalOrder().setDateFinalOrderEligibleFrom(data.getConditionalOrder().getDateAndTimeOfHearing().toLocalDate().plusWeeks(6).plusDays(1));
        data.getConditionalOrder().setOutcomeCase(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(ConditionalOrderPronounced)
            .build();
    }
}
