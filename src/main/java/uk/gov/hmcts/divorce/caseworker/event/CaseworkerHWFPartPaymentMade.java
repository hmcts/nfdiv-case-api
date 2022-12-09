package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerHWFPartPaymentMade implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_HWF_PART_PAYMENT_MADE = "caseworker-hwf-part-payment-made";
    public static final String EVENT_NAME_AND_DESCRIPTION = "HWF part payment made";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_HWF_PART_PAYMENT_MADE)
            .forStateTransition(EnumSet.of(AwaitingHWFPartPayment, AwaitingHWFEvidence), Submitted)
            .name(EVENT_NAME_AND_DESCRIPTION)
            .description(EVENT_NAME_AND_DESCRIPTION)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(SOLICITOR, LEGAL_ADVISOR, CITIZEN));
    }
}
