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
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.Applicant1AppliedForConditionalOrderNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.common.service.SubmitAosService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleCo.SWITCH_TO_SOLE_CO;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrderOffline.SWITCH_TO_SOLE_FO_OFFLINE;
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
import static uk.gov.hmcts.divorce.divorcecase.model.State.RespondentFinalOrderRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerOfflineDocumentVerified implements CCDConfig<CaseData, State, UserRole> {

    private final SubmitAosService submitAosService;
    private final HoldingPeriodService holdingPeriodService;
    private final NotificationDispatcher notificationDispatcher;
    private final Applicant1AppliedForConditionalOrderNotification app1AppliedForConditionalOrderNotification;
    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final Clock clock;
    private final GeneralReferralService generalReferralService;

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
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, JUDGE))
            .page("documentTypeReceived")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)

            .complex(CaseData::getDocuments)
            .readonlyNoSummary(CaseDocuments::getScannedSubtypeReceived, ALWAYS_HIDE)
                .mandatory(CaseDocuments::getTypeOfDocumentAttached, "scannedSubtypeReceived!=\"*\"", true)
            .done()
            .complex(CaseData::getAcknowledgementOfService)
            .label("scannedAosLabel", "Acknowledgement Of Service", "scannedSubtypeReceived=\"D10\"")
            .mandatory(AcknowledgementOfService::getHowToRespondApplication,
                "typeOfDocumentAttached=\"D10\" OR scannedSubtypeReceived=\"D10\"")
            .done()
            .complex(CaseData::getDocuments)
            .mandatory(CaseDocuments::getScannedDocumentNames,
                    "scannedSubtypeReceived!=\"*\" "
                        + "AND (typeOfDocumentAttached=\"D10\" OR typeOfDocumentAttached=\"D84\" OR typeOfDocumentAttached=\"D36\")")
            .done()
            .complex(CaseData::getConditionalOrder)
            .label("scannedCoLabel", "Conditional Order", "scannedSubtypeReceived=\"D84\"")
            .mandatory(ConditionalOrder::getD84ApplicationType,
                "typeOfDocumentAttached=\"D84\" OR scannedSubtypeReceived=\"D84\"")
            .mandatory(ConditionalOrder::getD84WhoApplying, "coD84ApplicationType=\"switchToSole\"")
            .done()
            .complex(CaseData::getFinalOrder)
            .readonlyNoSummary(FinalOrder::getFinalOrderReminderSentApplicant2, ALWAYS_HIDE)
            .label("scannedFoLabel", "Final Order", "scannedSubtypeReceived=\"D36\"")
            .mandatory(FinalOrder::getD36ApplicationType,
                "typeOfDocumentAttached=\"D36\" OR scannedSubtypeReceived=\"D36\"")
            .mandatory(FinalOrder::getD36WhoApplying, "d36ApplicationType=\"switchToSole\" "
                + "OR (d36ApplicationType=\"sole\" AND finalOrderReminderSentApplicant2=\"Yes\")")
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
        CaseDocuments.ScannedDocumentSubtypes scannedSubtypeReceived = caseData.getDocuments().getScannedSubtypeReceived();

        if (D10.equals(scannedSubtypeReceived)) {
            caseData.getDocuments().setTypeOfDocumentAttached(AOS_D10);
        } else if (D84.equals(scannedSubtypeReceived)) {
            caseData.getDocuments().setTypeOfDocumentAttached(CO_D84);
        } else if (D36.equals(scannedSubtypeReceived)) {
            caseData.getDocuments().setTypeOfDocumentAttached(FO_D36);
        }

        if (isEmpty(caseData.getDocuments().getScannedSubtypeReceived())) {
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
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_OFFLINE_DOCUMENT_VERIFIED, details.getId());
        CaseData caseData = details.getData();
        log.info("Scanned subtype received is {} for case {}", caseData.getDocuments().getScannedSubtypeReceived(), details.getId());
        log.info("Type of document attached is {} for case {}", caseData.getDocuments().getTypeOfDocumentAttached(), details.getId());

        if (AOS_D10.equals(caseData.getDocuments().getTypeOfDocumentAttached())) {
            return processD10AndSendNotifications(details);

        } else if (CO_D84.equals(caseData.getDocuments().getTypeOfDocumentAttached())) {
            return processD84AndSendNotifications(details);

        } else if (FO_D36.equals(caseData.getDocuments().getTypeOfDocumentAttached())) {
            return processD36AndSendNotifications(details);

        } else {
            State state = caseData.getApplication().getStateToTransitionApplicationTo();

            // TODO: NFDIV-3869 - InBulkActionCase is on hold until we figure out how to properly allow caseworkers to edit
            //  cases in bulk lists. If chosen, we override it to be AwaitPronouncement. This test should be removed once new logic is
            //  added to allow use of state.
            if (State.InBulkActionCase.equals(state)) {
                state = State.AwaitingPronouncement;
            }

            log.info("User selected other document type received, transitioning to state {} for case {}", state, details.getId());

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

    private AboutToStartOrSubmitResponse<CaseData, State> processD36AndSendNotifications(CaseDetails<CaseData, State> details) {
        log.info("Verifying FO D36 for case {}", details.getId());
        CaseData caseData = details.getData();


        reclassifyScannedDocumentToChosenDocumentType(caseData, FINAL_ORDER_APPLICATION);

        if (!SWITCH_TO_SOLE.equals(caseData.getFinalOrder().getD36ApplicationType())) {
            //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
            caseData.getDocuments().setScannedSubtypeReceived(null);
        }

        final boolean respondentRequested = OfflineWhoApplying.APPLICANT_2.equals(caseData.getFinalOrder().getD36WhoApplying());

        if (caseData.getApplicationType().isSole()) {
            if (respondentRequested) {
                caseData.getApplicant2().setOffline(YES);
            } else {
                caseData.getApplicant1().setOffline(YES);
            }
        } else {
            caseData.getApplicant1().setOffline(YES);
            caseData.getApplicant2().setOffline(YES);
        }

        // Should only hit RespondentFinalOrderRequested if Sole, eligible for FO, and from respondent.
        final State state = caseData.getApplicationType().isSole()
            && respondentRequested ? RespondentFinalOrderRequested : FinalOrderRequested;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> processD84AndSendNotifications(CaseDetails<CaseData, State> details) {
        log.info("Verifying CO D84 for case {}", details.getId());
        CaseData caseData = details.getData();

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

        if (!caseData.isJudicialSeparationCase()) {
            log.info(
                "CaseworkerOfflineDocumentVerified about to submit callback triggering app1 applied for co notifications: {}",
                details.getId());

            notificationDispatcher.send(app1AppliedForConditionalOrderNotification, caseData, details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> processD10AndSendNotifications(CaseDetails<CaseData, State> details) {
        log.info("Verifying AOS D10 for case {}", details.getId());
        var caseData = details.getData();

        reclassifyScannedDocumentToChosenDocumentType(caseData, RESPONDENT_ANSWERS);

        // setting the status as drafted as AOS answers has been received and is being classified by caseworker
        details.setState(AosDrafted);

        final CaseDetails<CaseData, State> response = submitAosService.submitOfflineAos(details);
        response.getData().getApplicant2().setOffline(YES);
        response.getData().getAcknowledgementOfService().setStatementOfTruth(YES);
        //setting ScannedSubtypeReceived to null as only scanned docs that have not been actioned should be filtered in case list
        response.getData().getDocuments().setScannedSubtypeReceived(null);

        log.info("CaseworkerOfflineDocumentVerified aos processed, sending aos notifications for case: {}", details.getId());

        submitAosService.submitAosNotifications(details);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(response.getData())
            .state(response.getState())
            .build();
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
            && SWITCH_TO_SOLE.equals(caseData.getConditionalOrder().getD84ApplicationType())) {
            log.info(
                "CaseworkerOfflineDocumentVerified submitted callback triggering SwitchedToSoleCO event for case id: {}",
                details.getId()
            );

            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();
            ccdUpdateService.submitEvent(details.getId(), SWITCH_TO_SOLE_CO, user, serviceAuth);

        } else if (FO_D36.equals(caseData.getDocuments().getTypeOfDocumentAttached())) {
            if (SWITCH_TO_SOLE.equals(caseData.getFinalOrder().getD36ApplicationType())) {
                log.info(
                    "CaseworkerOfflineDocumentVerified submitted callback triggering SwitchedToSoleFoOffline event for case id: {}",
                    details.getId());

                final User user = idamService.retrieveSystemUpdateUserDetails();
                final String serviceAuth = authTokenGenerator.generate();

                ccdUpdateService.submitEvent(details.getId(), SWITCH_TO_SOLE_FO_OFFLINE, user, serviceAuth);
            } else if (!(caseData.getApplicationType().isSole()
                && OfflineWhoApplying.APPLICANT_2.equals(caseData.getFinalOrder().getD36WhoApplying()))) {
                generalReferralService.caseWorkerGeneralReferral(details);
            } else {
                log.info("CaseID {} is Sole and Respondent Requested FO.  Skipping general referral check.", details.getId());
            }
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
