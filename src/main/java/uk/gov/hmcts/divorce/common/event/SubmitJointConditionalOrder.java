package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SubmitJointConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_JOINT_CONDITIONAL_ORDER = "submit-joint-conditional-order";

    @Autowired
    private Clock clock;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SUBMIT_JOINT_CONDITIONAL_ORDER)
            .forStates(ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Submit Conditional Order")
            .description("Submit Conditional Order")
            .endButtonLabel("Save Conditional Order")
            .showCondition("applicationType=\"jointApplication\" AND coApplicant2IsSubmitted=\"No\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR)
            .grant(READ, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR))
            .page("JointConditionalOrderSoT")
            .pageLabel("Statement of Truth - submit joint conditional order")
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant2Questions)
                .mandatory(ConditionalOrderQuestions::getStatementOfTruth)
                .mandatory(ConditionalOrderQuestions::getSolicitorName)
                .mandatory(ConditionalOrderQuestions::getSolicitorFirm)
                .optional(ConditionalOrderQuestions::getSolicitorAdditionalComments)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit joint conditional order about to submit callback invoked for case id: {}", details.getId());

        final CaseData data = details.getData();
        data.getConditionalOrder().getConditionalOrderApplicant2Questions().setSubmittedDate(LocalDateTime.now(clock));
        data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsSubmitted(YES);

        var state = beforeDetails.getState() == ConditionalOrderDrafted
            ? ConditionalOrderPending
            : AwaitingLegalAdvisorReferral;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }
}
