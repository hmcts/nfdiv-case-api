package uk.gov.hmcts.divorce.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralOrderTemplateContent;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static com.microsoft.applicationinsights.core.dependencies.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_ORDER;

@Slf4j
@Component
public class CreateGeneralOrder implements CcdPageConfiguration {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String NO_DETAILS_OR_SELECTED_DOCUMENT_ERROR = "You must either enter details or select a scanned document.";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private GeneralOrderTemplateContent generalOrderTemplateContent;

    @Autowired
    private Clock clock;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("CreateGeneralOrder", this::midEvent)
            .complex(CaseData::getGeneralOrder)
                .mandatory(GeneralOrder::getGeneralOrderDate)
                .mandatory(GeneralOrder::getGeneralOrderDivorceParties)
                .optional(GeneralOrder::getGeneralOrderRecitals)
                .optional(GeneralOrder::getGeneralOrderJudgeOrLegalAdvisorType)
                .optional(GeneralOrder::getGeneralOrderJudgeOrLegalAdvisorName)
                .optional(GeneralOrder::getGeneralOrderJudgeOrLegalAdvisorVenue)
                .optional(GeneralOrder::getGeneralOrderDetails)
            .done()
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getScannedDocumentNames)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for CreateGeneralOrder");

        final CaseData caseData = details.getData();
        final GeneralOrder generalOrder = caseData.getGeneralOrder();
        final DynamicList scannedDocNames = caseData.getDocuments().getScannedDocumentNames();
        final String selectedScannedDoc = scannedDocNames == null ? null : scannedDocNames.getValue().getLabel();

        if (isNullOrEmpty(generalOrder.getGeneralOrderDetails()) && isNullOrEmpty(selectedScannedDoc)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(NO_DETAILS_OR_SELECTED_DOCUMENT_ERROR))
                .build();
        }

        final Long caseId = details.getId();

        log.info("Generating general order document for templateId : {} case caseId: {}",
            DIVORCE_GENERAL_ORDER, caseId);

        if (selectedScannedDoc != null) {
            ListValue<ScannedDocument> scannedDoc = caseData.getDocuments().getScannedDocuments().stream()
                .filter(g -> g.getValue().getFileName().equals(selectedScannedDoc))
                .findFirst()
                .orElseThrow();
            generalOrder.setGeneralOrderScannedDraft(scannedDoc.getValue());
            generalOrder.setGeneralOrderUseScannedDraft(YES);
        } else {
            Document generalOrderDocument = caseDataDocumentService.renderDocument(
                generalOrderTemplateContent.apply(caseData, caseId),
                caseId,
                DIVORCE_GENERAL_ORDER,
                caseData.getApplicant1().getLanguagePreference(),
                GENERAL_ORDER + LocalDateTime.now(clock).format(formatter)
            );
            generalOrder.setGeneralOrderDraft(generalOrderDocument);
            generalOrder.setGeneralOrderUseScannedDraft(NO);
        }

        caseData.setGeneralOrder(generalOrder);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
