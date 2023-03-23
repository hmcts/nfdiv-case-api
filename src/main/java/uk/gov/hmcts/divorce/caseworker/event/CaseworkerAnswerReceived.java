package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.OrderSummary;
import uk.gov.hmcts.divorce.caseworker.event.page.AnswerReceivedPaymentConfirmation;
import uk.gov.hmcts.divorce.caseworker.event.page.AnswerReceivedPaymentSummary;
import uk.gov.hmcts.divorce.caseworker.event.page.AnswerReceivedUploadDocument;
import uk.gov.hmcts.divorce.citizen.notification.DisputedApplicationAnswerReceivedNotification;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.payment.PaymentService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAnswer;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.payment.PaymentService.EVENT_ISSUE;
import static uk.gov.hmcts.divorce.payment.PaymentService.KEYWORD_DEF;
import static uk.gov.hmcts.divorce.payment.PaymentService.SERVICE_OTHER;

@Component
@Slf4j
public class CaseworkerAnswerReceived implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ADD_ANSWER = "caseworker-add-answer";

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private DisputedApplicationAnswerReceivedNotification answerReceivedNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            new AnswerReceivedUploadDocument(),
            new AnswerReceivedPaymentConfirmation(),
            new AnswerReceivedPaymentSummary()
        );

        var pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_ADD_ANSWER)
            .forStates(EnumSet.of(Holding, AwaitingAos, AosOverdue, AwaitingConditionalOrder, AwaitingPronouncement, AwaitingAnswer))
            .name("Answer received")
            .description("Answer received")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("CASEWORKER_ADD_ANSWER aboutToStart-callback invoked for case id: {}", details.getId());
        final CaseData caseData = details.getData();

        OrderSummary orderSummary = paymentService.getOrderSummaryByServiceEvent(SERVICE_OTHER, EVENT_ISSUE, KEYWORD_DEF);
        caseData.getAcknowledgementOfService().getDisputingFee().setOrderSummary(orderSummary);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("CASEWORKER_ADD_ANSWER aboutToSubmit-callback invoked for case id: {}", details.getId());

        final CaseData caseData = details.getData();
        caseData.getDocuments().getAnswerReceivedSupportingDocuments()
            .forEach(documentListValue ->
                caseData.getDocuments().setDocumentsUploaded(
                    addDocumentToTop(caseData.getDocuments().getDocumentsUploaded(), documentListValue.getValue())
                ));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getState().equals(AwaitingAnswer) ? AwaitingJsNullity : details.getState())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("CASEWORKER_ADD_ANSWER submitted-callback invoked for case id: {}", details.getId());

        final CaseData caseData = details.getData();

        if (caseData.getAcknowledgementOfService().isDisputed()) {
            notificationDispatcher.send(answerReceivedNotification, details.getData(), details.getId());
        }
        return SubmittedCallbackResponse.builder().build();
    }
}
