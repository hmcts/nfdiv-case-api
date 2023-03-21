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
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;

@Component
public class CaseworkerAwaitingDocuments implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_AWAITING_DOCUMENTS = "caseworker-awaiting-documents";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_AWAITING_DOCUMENTS)
            .forStateTransition(EnumSet.of(AwaitingPayment, AwaitingHWFDecision, Submitted), AwaitingDocuments)
            .showEventNotes()
            .name("Awaiting documents")
            .description("Awaiting documents from the applicant")
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("caseworkerAwaitingDocuments")
            .pageLabel("Update Due Date")
            .optional(CaseData::getDueDate);
    }
}
