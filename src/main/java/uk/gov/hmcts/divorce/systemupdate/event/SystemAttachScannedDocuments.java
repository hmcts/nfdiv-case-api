package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.BulkScanMetaInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;

@Component
@Slf4j
public class SystemAttachScannedDocuments implements CCDConfig<CaseData, State, UserRole> {

    @Value("${toggle.enable_qr_code_reading}")
    private boolean qrCodeReadingEnabled;

    @Autowired
    private Clock clock;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .attachScannedDocEvent()
            .forStateTransition(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED, OfflineDocumentReceived)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEMUPDATE, CASE_WORKER)
            .grantHistoryOnly(LEGAL_ADVISOR))
            .page("attachScannedDocs")
            .pageLabel("Correspondence")
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getScannedDocuments)
                .done()
            .complex(CaseData::getBulkScanMetaInfo)
                .mandatoryWithLabel(BulkScanMetaInfo::getEvidenceHandled, "Supplementary evidence handled")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        final CaseData caseData = details.getData();

        //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
        caseData.getDocuments().setScannedSubtypeReceived(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails) {

        log.info(
            "[aboutToSubmit]: saving currentState({}) into caseData({}) to retain it post state transition",
            details.getState(), details.getId()
        );

        final CaseData caseData = details.getData();
        final CaseData beforeCaseData = beforeDetails.getData();
        caseData.getApplication().setPreviousState(beforeDetails.getState());

        if (qrCodeReadingEnabled) {
            handleScannedDocument(caseData, beforeCaseData);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    private void handleScannedDocument(CaseData caseData, CaseData beforeCaseData) {
        final List<ListValue<ScannedDocument>> afterScannedDocs = caseData.getDocuments().getScannedDocuments();
        final List<ListValue<ScannedDocument>> beforeScannedDocs =
            isNotEmpty(beforeCaseData.getDocuments().getScannedDocuments())
                ? beforeCaseData.getDocuments().getScannedDocuments()
                : new ArrayList<>();

        Optional<ScannedDocument> mostRecentScannedSubtypeReceived = Stream.ofNullable(afterScannedDocs)
            .flatMap(Collection::stream)
            .filter(element -> !beforeScannedDocs.contains(element))
            .map(ListValue::getValue)
            .filter(scannedDocument ->
                EnumUtils.isValidEnum(
                    CaseDocuments.ScannedDocumentSubtypes.class,
                    scannedDocument.getSubtype().toUpperCase(Locale.ROOT)
                )
            )
            .findFirst();

        if (mostRecentScannedSubtypeReceived.isPresent()) {
            final ScannedDocument scannedDocument = mostRecentScannedSubtypeReceived.get();
            final CaseDocuments.ScannedDocumentSubtypes scannedDocumentSubtype =
                CaseDocuments.ScannedDocumentSubtypes.valueOf(scannedDocument.getSubtype().toUpperCase(Locale.ROOT));
            final DocumentType documentType = getDocumentType(scannedDocumentSubtype);

            if (isNotEmpty(documentType)) {
                caseData.reclassifyScannedDocumentToChosenDocumentType(documentType, clock, scannedDocument);
                caseData.getDocuments().setScannedSubtypeReceived(scannedDocumentSubtype);
            }
        }
    }

    private DocumentType getDocumentType(CaseDocuments.ScannedDocumentSubtypes scannedDocumentSubtype) {

        // TODO: extend once Nullity document types added

        if (D10.equals(scannedDocumentSubtype)) {
            return RESPONDENT_ANSWERS;
        } else if (D84.equals(scannedDocumentSubtype)) {
            return CONDITIONAL_ORDER_APPLICATION;
        } else if (D36.equals(scannedDocumentSubtype)) {
            return FINAL_ORDER_APPLICATION;
        } else {
            return null;
        }
    }
}
