package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1AppliedForConditionalOrderNotification;
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
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.WelshTranslationReview;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SubmitConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_CONDITIONAL_ORDER = "submit-conditional-order";

    @Autowired
    private Applicant1AppliedForConditionalOrderNotification app1AppliedForConditionalOrderNotification;

    @Autowired
    private Applicant2AppliedForConditionalOrderNotification app2AppliedForConditionalOrderNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private Clock clock;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private GenerateConditionalOrderAnswersDocument generateConditionalOrderAnswersDocument;

    @Autowired
    private SolicitorAppliedForConditionalOrderNotification solicitorAppliedForConditionalOrderNotification;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SUBMIT_CONDITIONAL_ORDER)
            .forStates(ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Submit Conditional Order")
            .description("Submit Conditional Order")
            .endButtonLabel("Save Conditional Order")
            .showCondition("coApplicant1IsDrafted=\"Yes\" AND coApplicant1IsSubmitted=\"No\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, CREATOR, APPLICANT_2)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR))
            .page("ConditionalOrderSoT")
            .pageLabel("Statement of Truth - submit conditional order")
            .complex(CaseData::getConditionalOrder)
                .complex(ConditionalOrder::getConditionalOrderApplicant1Questions)
                .mandatory(ConditionalOrderQuestions::getStatementOfTruth)
                .mandatory(ConditionalOrderQuestions::getSolicitorName)
                .mandatory(ConditionalOrderQuestions::getSolicitorFirm)
                .optional(ConditionalOrderQuestions::getSolicitorAdditionalComments)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit conditional order about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        final boolean isSole = data.getApplicationType().isSole();
        ConditionalOrderQuestions app1Questions = data.getConditionalOrder().getConditionalOrderApplicant1Questions();
        ConditionalOrderQuestions app2Questions = data.getConditionalOrder().getConditionalOrderApplicant2Questions();

        final List<String> validationErrors = validate(app1Questions, app2Questions, isSole);

        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(details.getData())
                .errors(validationErrors)
                .build();
        }

        setSubmittedDate(app1Questions, app2Questions);
        setIsSubmitted(app1Questions, app2Questions);

        boolean haveBothApplicantsSubmitted = app1Questions.getStatementOfTruth() == YES && app2Questions.getStatementOfTruth() == YES;

        var state = isSole || haveBothApplicantsSubmitted ? AwaitingLegalAdvisorReferral : ConditionalOrderPending;

        if (AwaitingLegalAdvisorReferral.equals(state)
            && isSole
            && isEmpty(data.getAcknowledgementOfService().getDateAosSubmitted())
            && isNotEmpty(data.getCaseInvite())
            && isNotEmpty(data.getCaseInvite().accessCode())
            && shouldSetApplicant2ToOffline(data)
        ) {
            data.getApplicant2().setOffline(YES);
        }

        final boolean isApplicant1 = ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), details.getId());

        if (isApplicant1) {
            notificationDispatcher.send(app1AppliedForConditionalOrderNotification, data, details.getId());
        } else {
            notificationDispatcher.send(app2AppliedForConditionalOrderNotification, data, details.getId());
        }

        if (AwaitingLegalAdvisorReferral.equals(state)) {
            notificationDispatcher.send(solicitorAppliedForConditionalOrderNotification, data, details.getId());
            generateConditionalOrderAnswersDocument.apply(details,
                isApplicant1 ? data.getApplicant1().getLanguagePreference() : data.getApplicant2().getLanguagePreference());
        }

        if (AwaitingLegalAdvisorReferral.equals(state) && data.isWelshApplication()) {
            data.getApplication().setWelshPreviousState(state);
            state = WelshTranslationReview;
            log.info("State set to WelshTranslationReview, WelshPreviousState set to {}, CaseID {}",
                data.getApplication().getWelshPreviousState(), details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    private List<String> validate(ConditionalOrderQuestions app1Questions, ConditionalOrderQuestions app2Questions, boolean isSole) {
        if (app1Questions.getStatementOfTruth() == null || !app1Questions.getStatementOfTruth().toBoolean()) {
            if (isSole || (app2Questions.getStatementOfTruth() == null || !app2Questions.getStatementOfTruth().toBoolean())) {
                return of("The applicant must agree that the facts stated in the application are true");
            }
        }
        return emptyList();
    }

    private void setSubmittedDate(ConditionalOrderQuestions app1Questions, ConditionalOrderQuestions app2Questions) {
        if (Objects.nonNull(app1Questions.getStatementOfTruth()) && app1Questions.getStatementOfTruth().toBoolean()
            && Objects.isNull(app1Questions.getSubmittedDate())) {
            app1Questions.setSubmittedDate(LocalDateTime.now(clock));
        }
        if (Objects.nonNull(app2Questions.getStatementOfTruth()) && app2Questions.getStatementOfTruth().toBoolean()
            && Objects.isNull(app2Questions.getSubmittedDate())) {
            app2Questions.setSubmittedDate(LocalDateTime.now(clock));
        }
    }

    private void setIsSubmitted(ConditionalOrderQuestions app1Questions, ConditionalOrderQuestions app2Questions) {
        if (Objects.nonNull(app1Questions.getStatementOfTruth()) && app1Questions.getStatementOfTruth().toBoolean()) {
            app1Questions.setIsSubmitted(YES);
        }
        if (Objects.nonNull(app2Questions.getStatementOfTruth()) && app2Questions.getStatementOfTruth().toBoolean()) {
            app2Questions.setIsSubmitted(YES);
        }
    }

    private boolean shouldSetApplicant2ToOffline(CaseData caseData) {
        return caseData.getConditionalOrder().hasServiceBeenConfirmed()
            || caseData.getConditionalOrder().isLastApprovedServiceApplicationBailiffApplication()
            || caseData.getAlternativeService().isApplicationGrantedDeemedOrDispensed();
    }
}
