package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.notification.FinalOrderGrantedNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrderAuthorisation;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonList;
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
public class CaseworkerGrantFinalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_GRANT_FINAL_ORDER = "caseworker-grant-final-order";
    private static final String ALWAYS_HIDE = "generalOrderDocumentNames=\"ALWAYS_HIDE\"";
    public static final String ERROR_NO_CO_GRANTED_DATE = "No Conditional Order Granted Date found.  Unable to continue.";
    public static final String ERROR_NO_GENERAL_ORDER = "No general order documents found.  Unable to continue.";
    public static final String ERROR_CASE_NOT_ELIGIBLE = "Case is not yet eligible for Final Order";

    private final Clock clock;
    private final DocumentGenerator documentGenerator;
    private final FinalOrderGrantedNotification finalOrderGrantedNotification;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_GRANT_FINAL_ORDER)
            .forStates(FinalOrderRequested, FinalOrderPending, GeneralConsiderationComplete)
            .name("Grant Final order")
            .description("Grant Final order")
            .showSummary()
            .showEventNotes()
            .endButtonLabel("Submit")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("expediteFinalOrder")
            .showCondition("isFinalOrderOverdue=\"Yes\"")
            .pageLabel("Expedite Final Order")
            .complex(CaseData::getFinalOrder)
                .readonlyNoSummary(FinalOrder::getIsFinalOrderOverdue, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getGeneralOrderDocumentNames)
            .done()
            .complex(CaseData::getFinalOrder)
                .complex(FinalOrder::getOverdueFinalOrderAuthorisation)
                    .mandatory(FinalOrderAuthorisation::getFinalOrderJudgeName)
                .done()
            .done()
            .page("grantFinalOrder")
            .pageLabel("Grant Final Order")
            .complex(CaseData::getFinalOrder)
                .mandatory(FinalOrder::getGranted)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_GRANT_FINAL_ORDER, details.getId());

        var caseData = details.getData();

        if (caseData.getConditionalOrder().getGrantedDate() == null) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList(ERROR_NO_CO_GRANTED_DATE))
                .build();
        }

        if (!YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
        }

        if (CollectionUtils.isEmpty(caseData.getGeneralOrders())) {
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

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_GRANT_FINAL_ORDER, details.getId());

        CaseData caseData = details.getData();

        LocalDate dateFinalOrderEligibleFrom = caseData.getFinalOrder().getDateFinalOrderEligibleFrom();

        LocalDateTime currentDateTime = LocalDateTime.now(clock);

        if (dateFinalOrderEligibleFrom != null
            && dateFinalOrderEligibleFrom.isAfter(currentDateTime.toLocalDate())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList(ERROR_CASE_NOT_ELIGIBLE))
                .build();
        }

        caseData.getFinalOrder().setGrantedDate(currentDateTime);

        if (YesOrNo.YES.equals(caseData.getFinalOrder().getIsFinalOrderOverdue())) {
            final String foGrantingGeneralOrderName = caseData.getDocuments()
                .getGeneralOrderDocumentNames().getValue().getLabel();

            final List<ListValue<DivorceGeneralOrder>> generalOrderList = caseData.getGeneralOrders();
            ListValue<DivorceGeneralOrder> generalOrderToGrantFinalOrder = generalOrderList.stream()
                .filter(g -> g.getValue().getGeneralOrderDocument().getDocumentFileName().equals(foGrantingGeneralOrderName))
                .findFirst()
                .orElseThrow();

            caseData.getFinalOrder().getOverdueFinalOrderAuthorisation()
                .setFinalOrderGeneralOrder(generalOrderToGrantFinalOrder.getValue());
        }

        documentGenerator.generateAndStoreCaseDocument(
            FINAL_ORDER_GRANTED,
            FINAL_ORDER_TEMPLATE_ID,
            FINAL_ORDER_DOCUMENT_NAME,
            caseData,
            details.getId()
        );

        notificationDispatcher.send(finalOrderGrantedNotification, caseData, details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(FinalOrderComplete)
            .build();
    }
}
