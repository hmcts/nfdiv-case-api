package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ExpeditedFinalOrderAuthorisation;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

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
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;

@Slf4j
@RequiredArgsConstructor
@Component
public class CaseworkerExpediteFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_EXPEDITE_FINAL_ORDER = "caseworker-expedite-final-order";
    public static final String ERROR_NO_CO_GRANTED_DATE = "No Conditional Order Granted Date found.  Unable to continue.";
    public static final String ERROR_NO_GENERAL_ORDER = "No general order documents found.  Unable to continue.";

    private final NotificationDispatcher notificationDispatcher;
    private final FinalOrderGrantedNotification finalOrderGrantedNotification;
    private final DocumentGenerator documentGenerator;
    private final Clock clock;

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
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("expediteFinalOrder")
            .pageLabel("Expedite Final Order")
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getGeneralOrderDocumentNames)
            .done()
            .complex(CaseData::getFinalOrder)
                .complex(FinalOrder::getExpeditedFinalOrderAuthorisation)
                    .mandatory(ExpeditedFinalOrderAuthorisation::getExpeditedFinalOrderJudgeName)
                .done()
                .mandatory(FinalOrder::getGranted)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_EXPEDITE_FINAL_ORDER, details.getId());
        var caseData = details.getData();

        if (caseData.getConditionalOrder().getGrantedDate() == null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList(ERROR_NO_CO_GRANTED_DATE))
                .build();
        }

        if (caseData.getGeneralOrders() == null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList(ERROR_NO_GENERAL_ORDER))
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

        documentGenerator.generateAndStoreCaseDocument(
            FINAL_ORDER_GRANTED,
            FINAL_ORDER_TEMPLATE_ID,
            FINAL_ORDER_DOCUMENT_NAME,
            caseData,
            details.getId()
        );

        caseData.getDocuments().setGeneralOrderDocumentNames(null);

        notificationDispatcher.send(finalOrderGrantedNotification, caseData, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(FinalOrderComplete)
            .build();
    }
}
