package uk.gov.hmcts.divorce.common.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
public class SubmitClarification implements CCDConfig<CaseData, State, UserRole> {
    public static final String SUBMIT_CLARIFICATION = "submit-clarification";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SUBMIT_CLARIFICATION)
            .forStateTransition(AwaitingClarification, ClarificationSubmitted)
            .name("Submit clarification for CO")
            .description("Submit clarification for conditional order")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, CITIZEN)
            .grant(READ,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR))
            .page("submitClarificationForCO")
            .pageLabel("Submit clarification for conditional order")
            .complex(CaseData::getConditionalOrder)
                .readonly(ConditionalOrder::getRefusalDecision)
                .readonly(ConditionalOrder::getRefusalRejectionReason)
                .readonly(ConditionalOrder::getRefusalClarificationAdditionalInfo)
                .mandatory(ConditionalOrder::getClarificationResponses)
                .mandatory(ConditionalOrder::getCannotUploadClarificationDocuments)
                .optional(ConditionalOrder::getClarificationUploadDocuments)
            .done();
    }

}
