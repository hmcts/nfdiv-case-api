package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.event.page.CreateGeneralOrder;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralOrderDraft;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@RequiredArgsConstructor
@Component
@Slf4j
public class CaseworkerCreateGeneralOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CREATE_GENERAL_ORDER = "caseworker-create-general-order";

    private final CreateGeneralOrder createGeneralOrder;
    private final DocumentIdProvider documentIdProvider;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        final List<CcdPageConfiguration> pages = asList(
            createGeneralOrder,
            new GeneralOrderDraft()
        );

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(
        final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder
            .event(CASEWORKER_CREATE_GENERAL_ORDER)
            .forStates(POST_SUBMISSION_STATES)
            .name("Create general order")
            .description("Create general order")
            .showSummary()
            .showEventNotes()
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, SOLICITOR, CITIZEN, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();

        List<DynamicListElement> scannedDocumentNames =
            emptyIfNull(caseData.getDocuments().getScannedDocuments())
                .stream()
                .map(scannedDocListValue ->
                    DynamicListElement
                        .builder()
                        .label(scannedDocListValue.getValue().getFileName())
                        .code(UUID.randomUUID()).build()
                ).toList();

        DynamicList scannedDocNamesDynamicList = DynamicList
            .builder()
            .listItems(scannedDocumentNames)
            .build();

        caseData.getDocuments().setScannedDocumentNames(scannedDocNamesDynamicList);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("Caseworker create general order about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        final GeneralOrder generalOrder = data.getGeneralOrder();

        String fileName;
        Document documentLink;
        if (YES.equals(generalOrder.getGeneralOrderUseScannedDraft())) {
            fileName = generalOrder.getGeneralOrderScannedDraft().getFileName();
            documentLink = generalOrder.getGeneralOrderScannedDraft().getUrl();
        } else {
            fileName = generalOrder.getGeneralOrderDraft().getFilename();
            documentLink = generalOrder.getGeneralOrderDraft();
        }

        DivorceDocument generalOrderDocument = DivorceDocument
            .builder()
            .documentFileName(fileName)
            .documentType(DocumentType.GENERAL_ORDER)
            .documentLink(documentLink)
            .build();

        DivorceGeneralOrder divorceGeneralOrder = DivorceGeneralOrder
            .builder()
            .generalOrderDocument(generalOrderDocument)
            .generalOrderDivorceParties(generalOrder.getGeneralOrderDivorceParties())
            .build();

        if (YES.equals(generalOrder.getGeneralOrderUseScannedDraft())) {
            divorceGeneralOrder.setGeneralOrderFromScannedDoc(YES);
        }

        ListValue<DivorceGeneralOrder> divorceGeneralOrderListValue =
            ListValue
                .<DivorceGeneralOrder>builder()
                .id(documentIdProvider.documentId())
                .value(divorceGeneralOrder)
                .build();

        if (isEmpty(data.getGeneralOrders())) {
            data.setGeneralOrders(singletonList(divorceGeneralOrderListValue));
        } else {
            data.getGeneralOrders().add(0, divorceGeneralOrderListValue);
        }

        //clear general order field so that on next general order old data is not shown
        data.setGeneralOrder(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
