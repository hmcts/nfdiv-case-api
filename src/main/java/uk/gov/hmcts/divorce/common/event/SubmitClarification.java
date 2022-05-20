package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
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

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class SubmitClarification implements CCDConfig<CaseData, State, UserRole> {

    public static final String SUBMIT_CLARIFICATION = "submit-clarification";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private PostInformationToCourtNotification postInformationToCourtNotification;

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
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, APPLICANT_1_SOLICITOR, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
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

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();
        caseData.getConditionalOrder().resetClarificationFields();
        return AboutToStartOrSubmitResponse.<CaseData, State>builder().data(caseData).build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Submit Clarification about to submit callback invoked for case id: {}", details.getId());

        final CaseData data = details.getData();
        final ConditionalOrder conditionalOrder = data.getConditionalOrder();
        final boolean cannotUploadDocuments = data.getConditionalOrder().cannotUploadClarificationDocumentsBoolean();

        if (cannotUploadDocuments) {
            notificationDispatcher.send(postInformationToCourtNotification, data, details.getId());
        }

        if (isNotEmpty(data.getConditionalOrder().getClarificationUploadDocuments())) {
            data.getConditionalOrder().getClarificationUploadDocuments()
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

        data.getConditionalOrder().setClarificationResponsesSubmitted(
            data.getConditionalOrder().addAuditRecord(
                conditionalOrder.getClarificationResponsesSubmitted(),
                clarificationResponse
            )
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(ClarificationSubmitted)
            .build();
    }

}
