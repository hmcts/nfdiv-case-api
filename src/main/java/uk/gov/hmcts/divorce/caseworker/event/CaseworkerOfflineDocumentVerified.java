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
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ScannedDocument;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerOfflineDocumentVerified implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private SubmitAosService submitAosService;

    @Autowired
    private Clock clock;

    public static final String CASEWORKER_OFFLINE_DOCUMENT_VERIFIED = "caseworker-offline-document-verified";
    private static final String ALWAYS_HIDE = "typeOfDocumentAttached=\"ALWAYS_HIDE\"";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_OFFLINE_DOCUMENT_VERIFIED)
            .forState(OfflineDocumentReceived)
            .name("Offline Document Verified")
            .description("Offline Document Verified")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SUPER_USER))
            .page("documentTypeReceived")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .showCondition("applicationType=\"soleApplication\"")
            .complex(CaseData::getAcknowledgementOfService)
                .mandatory(AcknowledgementOfService::getTypeOfDocumentAttached)
                .mandatory(AcknowledgementOfService::getHowToRespondApplication, "typeOfDocumentAttached=\"D10\"")
            .done()
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getScannedDocumentNames, "typeOfDocumentAttached=\"D10\"")
            .done()
            .page("stateToTransitionTo")
            .showCondition("applicationType=\"jointApplication\" OR typeOfDocumentAttached=\"Other\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getStateToTransitionApplicationTo)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        var caseData = details.getData();
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

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        var caseData = details.getData();

        if (AOS_D10.equals(caseData.getAcknowledgementOfService().getTypeOfDocumentAttached())) {

            reclassifyAosScannedDocumentToRespondentAnswers(caseData);

            final CaseDetails<CaseData, State> response = submitAosService.submitOfflineAos(details);
            response.getData().getApplicant2().setOffline(YES);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(response.getData())
                .state(response.getState())
                .build();
        } else {
            final State state = caseData.getApplication().getStateToTransitionApplicationTo();

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(state)
                .build();
        }
    }

    private void reclassifyAosScannedDocumentToRespondentAnswers(CaseData caseData) {
        String aosFileName = caseData.getDocuments().getScannedDocumentNames().getValueLabel();

        log.info("Reclassifying scanned doc {} to respondent answers doc type", aosFileName);

        ListValue<ScannedDocument> aosScannedDocument =
            emptyIfNull(caseData.getDocuments().getScannedDocuments())
                .stream()
                .filter(scannedDoc -> scannedDoc.getValue().getFileName().equals(aosFileName))
                .findFirst()
                .get();

        List<ListValue<DivorceDocument>> updatedDocumentsUploaded = caseData.getDocuments().addDocumentToTop(
            caseData.getDocuments().getDocumentsUploaded(),
            mapScannedDocumentToDivorceDocument(aosScannedDocument.getValue())
        );

        caseData.getDocuments().setDocumentsUploaded(updatedDocumentsUploaded);
    }

    private DivorceDocument mapScannedDocumentToDivorceDocument(final ScannedDocument scannedDocument) {
        return DivorceDocument.builder()
            .documentLink(scannedDocument.getUrl())
            .documentFileName(scannedDocument.getFileName())
            .documentDateAdded(LocalDate.now(clock))
            .documentType(DocumentType.RESPONDENT_ANSWERS)
            .documentComment("Reclassified scanned document")
            .build();
    }
}
