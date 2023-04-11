package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ClarificationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.PostInformationToCourtNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SubmitClarification implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_CLARIFICATION = "submit-clarification";
    private static final String NEVER_SHOW = "coRefusalDecision=\"NEVER_SHOW\"";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private PostInformationToCourtNotification postInformationToCourtNotification;

    @Autowired
    private ClarificationSubmittedNotification clarificationSubmittedNotification;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SUBMIT_CLARIFICATION)
            .forStateTransition(AwaitingClarification, ClarificationSubmitted)
            .name("Submit clarification for CO")
            .description("Submit clarification for conditional order")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, APPLICANT_1_SOLICITOR, APPLICANT_2_SOLICITOR, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("submitClarificationForCO")
            .pageLabel("Submit clarification for conditional order")
            .complex(CaseData::getConditionalOrder)
                .readonly(ConditionalOrder::getCannotUploadClarificationDocuments, NEVER_SHOW)
                .readonly(ConditionalOrder::getRefusalDecision)
                .readonly(ConditionalOrder::getRefusalOrderDocument)
                .readonly(ConditionalOrder::getRefusalClarificationAdditionalInfo)
                .mandatory(ConditionalOrder::getClarificationResponses)
                .optional(ConditionalOrder::getClarificationUploadDocuments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit Clarification about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        final ConditionalOrder conditionalOrder = data.getConditionalOrder();
        final boolean cannotUploadDocuments = conditionalOrder.cannotUploadClarificationDocumentsBoolean();
        final boolean clarificationDocumentsUploaded = isNotEmpty(conditionalOrder.getClarificationUploadDocuments());

        if (cannotUploadDocuments) {
            notificationDispatcher.send(postInformationToCourtNotification, data, details.getId());
        }

        if (clarificationDocumentsUploaded) {
            conditionalOrder.getClarificationUploadDocuments()
                .forEach(documentListValue ->
                    data.getDocuments().setDocumentsUploaded(
                        addDocumentToTop(data.getDocuments().getDocumentsUploaded(), documentListValue.getValue())
                    ));
        }

        final var clarificationResponse =
            ClarificationResponse.builder()
                .clarificationDate(LocalDate.now(clock))
                .clarificationResponses(conditionalOrder.getClarificationResponses())
                .clarificationUploadDocuments(conditionalOrder.getClarificationUploadDocuments())
                .cannotUploadClarificationDocuments(conditionalOrder.getCannotUploadClarificationDocuments())
                .build();

        conditionalOrder.setClarificationResponsesSubmitted(
            conditionalOrder.addAuditRecord(
                conditionalOrder.getClarificationResponsesSubmitted(),
                clarificationResponse
            )
        );

        notificationDispatcher.send(clarificationSubmittedNotification, data, details.getId());

        data.getConditionalOrder().resetRefusalFields();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(ClarificationSubmitted)
            .build();
    }

}
