package uk.gov.hmcts.divorce.caseworker.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class CaseworkerHwfPartPaymentRequired implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_HWF_PART_PAYMENT_REQUIRED = "caseworker-hwf-part-payment-required";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_HWF_PART_PAYMENT_REQUIRED)
            .forStateTransition(EnumSet.of(AwaitingDocuments, AwaitingHWFEvidence, AwaitingHWFDecision), AwaitingHWFPartPayment)
            .name("HWF part payment required")
            .description("HWF part payment required")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR));
    }
}
