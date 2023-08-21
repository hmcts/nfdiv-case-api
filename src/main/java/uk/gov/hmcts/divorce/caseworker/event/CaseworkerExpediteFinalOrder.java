package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrderCoverLetter;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderAuthorisation;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CaseworkerExpediteFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_EXPEDITE_FINAL_ORDER = "caseworker-expedite-final-order";

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Autowired
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Autowired
    private GenerateFinalOrder generateFinalOrder;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_EXPEDITE_FINAL_ORDER)
            .forStates(FinalOrderRequested, FinalOrderPending, GeneralConsiderationComplete)
            .name("Expedite Final order")
            .description("Expedite Final order")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Submit")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("expediteFinalOrder")
            .pageLabel("Expedite Final Order")
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getGeneralOrderDocumentNames)
            .done()
            .complex(CaseData::getFinalOrder)
                .complex(FinalOrder::getExpeditedFinalOrderAuthorisation)
                    .mandatory(FinalOrderAuthorisation::getExpeditedFinalOrderJudgeName)
                .done()
                .mandatory(FinalOrder::getGranted)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_EXPEDITE_FINAL_ORDER, details.getId());
        var caseData = details.getData();

        if (caseData.getGeneralOrders() == null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList("No general order documents found.  Unable to continue."))
                .build();
        }

        final var generalOrderDocuments = caseData.getGeneralOrders()
            .stream()
            .map(doc -> doc.getValue().getGeneralOrderDocument())
            .toList();

        List<DynamicListElement> generalOrderDocumentNames =
            generalOrderDocuments
                .stream()
                .map(generalOrderDocument ->
                    DynamicListElement
                        .builder()
                        .label(generalOrderDocument.getDocumentFileName())
                        .code(UUID.randomUUID()).build()
                )
                .collect(toList());

        DynamicList generalOrderDocumentNamesDynamicList = DynamicList
            .builder()
            .listItems(generalOrderDocumentNames)
            .build();

        caseData.getDocuments().setGeneralOrderDocumentNames(generalOrderDocumentNamesDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_EXPEDITE_FINAL_ORDER, details.getId());

        CaseData caseData = details.getData();

        caseData.getFinalOrder().setDateFinalOrderEligibleFrom(LocalDate.now(clock));
        caseData.getFinalOrder().setGrantedDate(LocalDateTime.now(clock));
        final String expeditedFinalOrderGeneralOrderDocumentName = caseData.getDocuments()
            .getGeneralOrderDocumentNames().getValue().getLabel();

        final List<ListValue<DivorceGeneralOrder>> generalOrderList = caseData.getGeneralOrders();
        Optional<ListValue<DivorceGeneralOrder>> generalOrderToExpediteFinancialOrder = generalOrderList.stream()
            .filter(g -> g.getValue().getGeneralOrderDocument().getDocumentFileName().equals(expeditedFinalOrderGeneralOrderDocumentName))
            .findFirst();

        caseData.getFinalOrder().getExpeditedFinalOrderAuthorisation()
            .setExpeditedFinalOrderGeneralOrder(generalOrderToExpediteFinancialOrder.get().getValue());

        generateFinalOrderCoverLetter.apply(details);
        generateFinalOrder.apply(details);

        caseData.getDocuments().setGeneralOrderDocumentNames(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(FinalOrderComplete)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        final CaseData caseData = details.getData();

        log.info("{} submitted callback invoked for case id: {}", CASEWORKER_EXPEDITE_FINAL_ORDER, details.getId());

        notificationDispatcher.send(finalOrderGrantedNotification, caseData, caseId);

        return SubmittedCallbackResponse.builder().build();
    }
}
