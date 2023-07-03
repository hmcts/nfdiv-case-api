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
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
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
    private Clock clock;

    @Autowired
    private GenerateFinalOrder generateFinalOrder;

    @Autowired
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Autowired
    private FinalOrderGrantedNotification finalOrderGrantedNotification;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

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
            .complex(CaseData::getFinalOrder)
            .mandatory(FinalOrder::getGranted)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_EXPEDITE_FINAL_ORDER, details.getId());
        var caseData = details.getData();
        final var generalOrderDocs = caseData.getGeneralOrders()
            .stream().map(doc -> doc.getValue().getGeneralOrderDocument()).collect(toList());

        if (!isEmpty(generalOrderDocs)) {
            List<DynamicListElement> scannedDocumentNames =
                emptyIfNull(caseData.getDocuments().getScannedDocuments())
                    .stream()
                    .map(scannedDocListValue ->
                        DynamicListElement
                            .builder()
                            .label(scannedDocListValue.getValue().getFileName())
                            .code(UUID.randomUUID()).build()
                    )
                    .collect(toList());

            DynamicList scannedDocNamesDynamicList = DynamicList
                .builder()
                .value(DynamicListElement.builder().label("scannedDocumentName").code(UUID.randomUUID()).build())
                .listItems(scannedDocumentNames)
                .build();

            caseData.getDocuments().setScannedDocumentNames(scannedDocNamesDynamicList);
        }

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

        final List<ListValue<DivorceGeneralOrder>> generalOrderList = caseData.getGeneralOrders();
        boolean generalOrderToExpediteFO = generalOrderList.stream()
            .anyMatch(g -> g.getValue().getGeneralOrderFastTrackFinalOrder().equals(YES));

        if (!generalOrderToExpediteFO) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList("No general order authorising FO fast track found.  Unable to continue."))
                .build();
        }

        generateFinalOrderCoverLetter.apply(details);
        generateFinalOrder.apply(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(FinalOrderComplete)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final Long caseId = details.getId();
        final CaseData caseData = details.getData();

        log.info("CitizenSaveAndClose submitted callback invoked for case id: {}", details.getId());

        notificationDispatcher.send(finalOrderGrantedNotification, caseData, caseId);

        return SubmittedCallbackResponse.builder().build();
    }
}
