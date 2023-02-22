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
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrder.SWITCH_TO_SOLE_FO;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.AOS_D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.CO_D84;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.FO_D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D10;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D36;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.ScannedDocumentSubtypes.D84;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingLegalAdvisorReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.FinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.JSAwaitingLA;
import static uk.gov.hmcts.divorce.divorcecase.model.State.OfflineDocumentReceived;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;

@Component
@Slf4j
public class CaseworkerOfflineDocumentVerified implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private SubmitAosService submitAosService;

    @Autowired
    private HoldingPeriodService holdingPeriodService;

    @Autowired
    private NotificationDispatcher notificationDispatcher;

    @Autowired
    private Applicant1AppliedForConditionalOrderNotification app1AppliedForConditionalOrderNotification;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

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
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR))
            .page("documentTypeReceived")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)

            .complex(CaseData::getDocuments)
                .readonlyNoSummary(CaseDocuments::getScannedSubtypeReceived, ALWAYS_HIDE)
                .mandatory(CaseDocuments::getTypeOfDocumentAttached, "scannedSubtypeReceived!=\"*\"")
            .done()
            .complex(CaseData::getAcknowledgementOfService)
                .label("scannedAosLabel", "Acknowledgement Of Service", "scannedSubtypeReceived=\"D10\"")
                .mandatory(AcknowledgementOfService::getHowToRespondApplication,
                    "typeOfDocumentAttached=\"D10\" OR scannedSubtypeReceived=\"D10\"")
            .done()
            .complex(CaseData::getDocuments)
                .mandatory(CaseDocuments::getScannedDocumentNames,
                    "typeOfDocumentAttached=\"D10\" OR typeOfDocumentAttached=\"D84\" OR typeOfDocumentAttached=\"D36\"")
            .done()
            .complex(CaseData::getConditionalOrder)
                .label("scannedCoLabel", "Conditional Order", "scannedSubtypeReceived=\"D84\"")
                .mandatory(ConditionalOrder::getD84ApplicationType,
                    "typeOfDocumentAttached=\"D84\" OR scannedSubtypeReceived=\"D84\"")
                .mandatory(ConditionalOrder::getD84WhoApplying, "coD84ApplicationType=\"switchToSole\"")
            .done()
            .complex(CaseData::getFinalOrder)
                .label("scannedFoLabel", "Final Order", "scannedSubtypeReceived=\"D36\"")
                .mandatory(FinalOrder::getD36ApplicationType,
                    "typeOfDocumentAttached=\"D36\" OR scannedSubtypeReceived=\"D36\"")
                .mandatory(FinalOrder::getD36WhoApplying, "d36ApplicationType=\"switchToSole\"")
            .done()
            .page("stateToTransitionToOtherDoc")
            .showCondition("applicationType=\"soleApplication\" AND typeOfDocumentAttached=\"Other\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getStateToTransitionApplicationTo)
            .done()


            .page("stateToTransitionToJoint")
            .showCondition("applicationType=\"jointApplication\" AND typeOfDocumentAttached!=\"D84\" OR scannedSubtypeReceived!=\"D84\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getStateToTransitionApplicationTo)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, details.getId());
        var caseData = details.getData();

        if (isEmpty(caseData.getDocuments().getScannedSubtypeReceived())) {
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

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, details.getId());
        var caseData = details.getData();

        if (AOS_D10.equals(caseData.getDocuments().getTypeOfDocumentAttached())
            || D10.equals(caseData.getDocuments().getScannedSubtypeReceived())) {

            reclassifyScannedDocumentToChosenDocumentType(caseData, RESPONDENT_ANSWERS);

            // setting the status as drafted as AOS answers has been received and is being classified by caseworker
            details.setState(AosDrafted);

            final CaseDetails<CaseData, State> response = submitAosService.submitOfflineAos(details);
            response.getData().getApplicant2().setOffline(YES);
            response.getData().getAcknowledgementOfService().setStatementOfTruth(YES);
            //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
            response.getData().getDocuments().setScannedSubtypeReceived(null);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(response.getData())
                .state(response.getState())
                .build();

        } else if (CO_D84.equals(caseData.getDocuments().getTypeOfDocumentAttached())
            || D84.equals(caseData.getDocuments().getScannedSubtypeReceived())) {

            reclassifyScannedDocumentToChosenDocumentType(caseData, CONDITIONAL_ORDER_APPLICATION);

            if (!SWITCH_TO_SOLE.equals(caseData.getConditionalOrder().getD84ApplicationType())) {
                //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
                caseData.getDocuments().setScannedSubtypeReceived(null);
            }

            if (caseData.getApplicationType().isSole()) {
                caseData.getApplicant1().setOffline(YES);
            } else {
                caseData.getApplicant1().setOffline(YES);
                caseData.getApplicant2().setOffline(YES);
            }

            var state = caseData.isJudicialSeparationCase()
                ? JSAwaitingLA
                : AwaitingLegalAdvisorReferral;

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(state)
                .build();

        } else if (FO_D36.equals(caseData.getDocuments().getTypeOfDocumentAttached())
            || D36.equals(caseData.getDocuments().getScannedSubtypeReceived())) {

            reclassifyScannedDocumentToChosenDocumentType(caseData, FINAL_ORDER_APPLICATION);

            if (!SWITCH_TO_SOLE.equals(caseData.getFinalOrder().getD36ApplicationType())) {
                //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
                caseData.getDocuments().setScannedSubtypeReceived(null);
            }

            if (caseData.getApplicationType().isSole()) {
                caseData.getApplicant1().setOffline(YES);
            } else {
                caseData.getApplicant1().setOffline(YES);
                caseData.getApplicant2().setOffline(YES);
            }

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(FinalOrderRequested)
                .build();

        } else {
            final State state = caseData.getApplication().getStateToTransitionApplicationTo();

            if (Holding.equals(state)) {
                log.info("Setting due date(Issue date + 20 weeks + 1 day) as state selected is Holding for case id {}",
                    details.getId()
                );
                details.getData().setDueDate(holdingPeriodService.getDueDateFor(caseData.getApplication().getIssueDate()));
            }

            //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
            details.getData().getDocuments().setScannedSubtypeReceived(null);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .state(state)
                .build();
        }
    }

    private void reclassifyScannedDocumentToChosenDocumentType(CaseData caseData, DocumentType documentType) {
        if (isEmpty(caseData.getDocuments().getScannedSubtypeReceived())) {
            String filename = caseData.getDocuments().getScannedDocumentNames().getValueLabel();

            log.info("Reclassifying scanned doc {} to {} doc type", filename, documentType);

            caseData.reclassifyScannedDocumentToChosenDocumentType(documentType, clock, filename);
        }
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();

        if (CO_D84.equals(caseData.getDocuments().getTypeOfDocumentAttached())
            || D84.equals(caseData.getDocuments().getScannedSubtypeReceived())) {

            notificationDispatcher.send(app1AppliedForConditionalOrderNotification, caseData, details.getId());

            if (SWITCH_TO_SOLE.equals(caseData.getConditionalOrder().getD84ApplicationType())) {

                log.info(
                    "CaseworkerOfflineDocumentVerified submitted callback triggering SwitchedToSoleCO event for case id: {}",
                    details.getId());

                //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
                caseData.getDocuments().setScannedSubtypeReceived(null);

                final User user = idamService.retrieveSystemUpdateUserDetails();
                final String serviceAuth = authTokenGenerator.generate();
                ccdUpdateService.submitEvent(details, SWITCH_TO_SOLE_CO, user, serviceAuth);
            }

        } else if ((FO_D36.equals(caseData.getDocuments().getTypeOfDocumentAttached())
            || D36.equals(caseData.getDocuments().getScannedSubtypeReceived()))
            && SWITCH_TO_SOLE.equals(caseData.getFinalOrder().getD36ApplicationType())) {

            log.info(
                "CaseworkerOfflineDocumentVerified submitted callback triggering Switched To Sole FO event for case id: {}",
                details.getId());

            //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
            caseData.getDocuments().setScannedSubtypeReceived(null);

            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();
            ccdUpdateService.submitEvent(details, SWITCH_TO_SOLE_FO, user, serviceAuth);
        } else if (AOS_D10.equals(caseData.getDocuments().getTypeOfDocumentAttached())) {
            log.info(
                "CaseworkerOfflineDocumentVerified submitted callback triggering submit aos notifications: {}",
                details.getId());

            submitAosService.submitAosNotifications(details);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
