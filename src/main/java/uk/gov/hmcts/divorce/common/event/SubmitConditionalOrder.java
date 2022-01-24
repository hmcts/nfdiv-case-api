package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class SubmitConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_CONDITIONAL_ORDER = "submit-conditional-order";

    @Autowired
    private AppliedForConditionalOrderNotification appliedForConditionalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private Clock clock;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IdamService idamService;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SUBMIT_CONDITIONAL_ORDER)
            .forStates(ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Submit Conditional Order")
            .description("Submit Conditional Order")
            .endButtonLabel("Save Conditional Order")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, CREATOR, APPLICANT_2)
            .grant(READ, CASE_WORKER, SUPER_USER, LEGAL_ADVISOR))
            .page("ConditionalOrderSoT")
            .pageLabel("Statement of Truth - submit conditional order")
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                .mandatory(ConditionalOrderQuestions::getStatementOfTruth)
                .done()
            .done()
            .complex(CaseData::getConditionalOrder)
                .mandatory(ConditionalOrder::getSolicitorName)
                .mandatory(ConditionalOrder::getSolicitorFirm)
                .optional(ConditionalOrder::getSolicitorAdditionalComments)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit conditional order about to submit callback invoked for case id: {}", details.getId());
        CaseData data = details.getData();
        setSubmittedDate(data.getConditionalOrder());
        var state = details.getData().getApplicationType().isSole()
            ? AwaitingLegalAdvisorReferral
            : beforeDetails.getState() == ConditionalOrderDrafted ? ConditionalOrderPending : AwaitingLegalAdvisorReferral;

        User user = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        appliedForConditionalOrderNotification.setSubmittingUserId(user.getUserDetails().getId());
        notificationDispatcher.send(appliedForConditionalOrderNotification, data, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    private void setSubmittedDate(ConditionalOrder conditionalOrder) {
        ConditionalOrderQuestions app1Questions = conditionalOrder.getConditionalOrderApplicant1Questions();
        ConditionalOrderQuestions app2Questions = conditionalOrder.getConditionalOrderApplicant2Questions();
        if (Objects.nonNull(app1Questions.getStatementOfTruth()) && app1Questions.getStatementOfTruth().toBoolean()
            && Objects.isNull(app1Questions.getSubmittedDate())) {
            app1Questions.setSubmittedDate(LocalDateTime.now(clock));
        }
        if (Objects.nonNull(app2Questions.getStatementOfTruth()) && app2Questions.getStatementOfTruth().toBoolean()
            && Objects.isNull(app2Questions.getSubmittedDate())) {
            app2Questions.setSubmittedDate(LocalDateTime.now(clock));
        }
    }
}
