package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerRemoveGeneralOrder implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private DocumentRemovalService documentRemovalService;

    @Autowired
    private DocumentIdProvider documentIdProvider;

    public static final String CASEWORKER_REMOVE_GO = "caseworker-remove-general-order";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_GO)
            .forStates(POST_SUBMISSION_STATES)
            .name("Remove General Order")
            .description("Remove General Order")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(CASE_WORKER,SUPER_USER))
            .page("removeGeneralOrders")
            .pageLabel("Remove General Orders")
            .optional(CaseData::getGeneralOrders)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_REMOVE_GO, details.getId());

        final var beforeCaseData = beforeDetails.getData();
        final var currentCaseData = details.getData();

        handleDeletionOfGeneralOrderDocuments(beforeCaseData, currentCaseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(currentCaseData)
            .build();
    }

    private void handleDeletionOfGeneralOrderDocuments(CaseData beforeCaseData, CaseData currentCaseData) {
        List<DivorceDocument> documentsToRemove = findGeneralOrderDocumentsForRemoval(
            beforeCaseData.getGeneralOrders(),
            currentCaseData.getGeneralOrders()
        );

        if (!documentsToRemove.isEmpty()) {
            List<ListValue<DivorceDocument>> documents = documentsToRemove.stream()
                .map(divorceDocument -> ListValue.<DivorceDocument>builder()
                    .id(documentIdProvider.documentId())
                    .value(divorceDocument).build()).toList();
            documentRemovalService.deleteDocument(documents);
        }
    }

    private List<DivorceDocument> findGeneralOrderDocumentsForRemoval(final List<ListValue<DivorceGeneralOrder>> beforeGeneralOrders,
                                                                     final List<ListValue<DivorceGeneralOrder>> currentGeneralOrders) {

        List<DivorceDocument> documentsToRemove = new ArrayList<>();

        if (beforeGeneralOrders != null && currentGeneralOrders != null) {
            beforeGeneralOrders.forEach(generalOrder -> {
                if (!currentGeneralOrders.contains(generalOrder)) {
                    documentsToRemove.add(generalOrder.getValue().getGeneralOrderDocument());
                }
            });
        } else if (beforeGeneralOrders != null && currentGeneralOrders == null) {
            beforeGeneralOrders.forEach(gOrder ->
                    documentsToRemove.add(gOrder.getValue().getGeneralOrderDocument()));
        }

        return documentsToRemove;
    }
}
