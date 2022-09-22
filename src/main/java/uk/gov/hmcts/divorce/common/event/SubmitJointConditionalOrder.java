package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant2AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.GenerateConditionalOrderAnswersDocument;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.notification.SolicitorAppliedForConditionalOrderNotification;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDateTime;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SubmitJointConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_JOINT_CONDITIONAL_ORDER = "submit-joint-conditional-order";

    @Autowired
    private Clock clock;

    @Autowired
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private SolicitorAppliedForConditionalOrderNotification solicitorAppliedForConditionalOrderNotification;

    @Autowired
    private Applicant2AppliedForConditionalOrderNotification app2AppliedForConditionalOrderNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SUBMIT_JOINT_CONDITIONAL_ORDER)
            .forStates(ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Submit Conditional Order")
            .description("Submit Conditional Order")
            .endButtonLabel("Save Conditional Order")
            .showCondition("applicationType=\"jointApplication\" AND coApplicant2IsDrafted=\"Yes\" AND coApplicant2IsSubmitted=\"No\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR))
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

        log.info("Submit joint conditional order about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        data.getConditionalOrder().getConditionalOrderApplicant2Questions().setSubmittedDate(LocalDateTime.now(clock));
        data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsSubmitted(YES);

        var state = ConditionalOrderDrafted.equals(beforeDetails.getState())
            ? ConditionalOrderPending
            : AwaitingLegalAdvisorReferral;

        if (AwaitingLegalAdvisorReferral.equals(state)) {
            generateConditionalOrderAnswersDocument.apply(details, data.getApplicant2().getLanguagePreference());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        var data = details.getData();
        var caseId = details.getId();

        log.info("Submit Joint Conditional Order Submitted callback invoked for case id {} ", caseId);

        if (AwaitingLegalAdvisorReferral.equals(details.getState())) {
            notificationDispatcher.send(solicitorAppliedForConditionalOrderNotification, data, caseId);
        } else {
            notificationDispatcher.send(app2AppliedForConditionalOrderNotification, data, caseId);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
