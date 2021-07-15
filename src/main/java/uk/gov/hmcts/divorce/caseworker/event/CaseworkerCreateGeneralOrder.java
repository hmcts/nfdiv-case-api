package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.event.page.CreateGeneralOrder;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralOrderDraft;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceGeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_CTSC;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_COURTADMIN_RDU;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASEWORKER_SUPERUSER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerCreateGeneralOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_CREATE_GENERAL_ORDER = "caseworker-create-general-order";

    @Autowired
    private CreateGeneralOrder createGeneralOrder;

    @Autowired
    private DocumentIdProvider documentIdProvider;

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
            .forAllStates()
            .name("Create general order")
            .description("Create general order")
            .explicitGrants()
            .showSummary()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASEWORKER_COURTADMIN_CTSC)
            .grant(READ, CASEWORKER_COURTADMIN_RDU, CASEWORKER_SUPERUSER, CASEWORKER_LEGAL_ADVISOR, SOLICITOR, CITIZEN));
    }

    private AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {

        log.info("Caseworker create general order about to submit callback invoked");

        var caseDataCopy = details.getData().toBuilder().build();
        var generalOrder = caseDataCopy.getGeneralOrder();

        DivorceDocument generalOrderDocument = DivorceDocument
            .builder()
            .documentFileName(generalOrder.getGeneralOrderDraft().getFilename())
            .documentType(DocumentType.GENERAL_ORDER)
            .documentLink(generalOrder.getGeneralOrderDraft())
            .build();

        DivorceGeneralOrder divorceGeneralOrder = DivorceGeneralOrder
            .builder()
            .generalOrderDocument(generalOrderDocument)
            .generalOrderDivorceParties(generalOrder.getGeneralOrderDivorceParties())
            .build();

        ListValue<DivorceGeneralOrder> divorceGeneralOrderListValue =
            ListValue
                .<DivorceGeneralOrder>builder()
                .id(documentIdProvider.documentId())
                .value(divorceGeneralOrder)
                .build();

        if (isEmpty(caseDataCopy.getGeneralOrders())) {
            caseDataCopy.setGeneralOrders(singletonList(divorceGeneralOrderListValue));

        } else {
            caseDataCopy.getGeneralOrders().add(divorceGeneralOrderListValue);
        }

        //clear general order field so that on next general order old data is not shown
        caseDataCopy.setGeneralOrder(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseDataCopy)
            .build();
    }
}
