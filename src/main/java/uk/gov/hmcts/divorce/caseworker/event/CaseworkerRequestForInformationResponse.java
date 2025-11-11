package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.CitizenRequestForInformationResponseNotification;
import uk.gov.hmcts.divorce.citizen.notification.CitizenRequestForInformationResponsePartnerNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationOfflineResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingResponseToHWFDecision;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerRequestForInformationResponse implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE = "caseworker-request-for-information-response";
    private static final String ALWAYS_HIDE = "rfiOfflineSoleResponseParties=\"ALWAYS_HIDE\"";
    public static final String NO_REQUEST_FOR_INFORMATION_ERROR =
        "There is no Request for Information on the case.";
    public static final String REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_FAILED_ERROR
        = "Request for Information Response Notification for Case Id {} failed with message: {}";
    public static final String REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_FAILED_ERROR
        = "Request for Information Response Partner Notification for Case Id {} failed with message: {}";
    private final NotificationDispatcher notificationDispatcher;
    private final CitizenRequestForInformationResponseNotification citizenRequestForInformationResponseNotification;
    private final CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE)
            .forStates(AwaitingHWFDecision,
                AwaitingResponseToHWFDecision,
                AwaitingHWFEvidence,
                AwaitingHWFPartPayment,
                AwaitingPayment,
                AwaitingDocuments,
                InformationRequested,
                AwaitingRequestedInformation,
                RequestedInformationSubmitted,
                Submitted)
            .name("Add RFI Response")
            .description("Add RFI Response")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .showEventNotes()
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER_BULK_SCAN, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, JUDGE))
            .page("addTextRfiResponse")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .complex(CaseData::getRequestForInformationList)
                .complex(RequestForInformationList::getRequestForInformationOfflineResponseDraft)
                    .mandatoryWithoutDefaultValue(
                        RequestForInformationOfflineResponseDraft::getRfiOfflineSoleResponseParties,
                        "applicationType=\"soleApplication\"",
                        "Select sender of response"
                    )
                    .mandatoryWithoutDefaultValue(
                        RequestForInformationOfflineResponseDraft::getRfiOfflineJointResponseParties,
                        "applicationType=\"jointApplication\"",
                        "Select sender of response"
                    )
                    .mandatory(
                        RequestForInformationOfflineResponseDraft::getRfiOfflineResponseOtherName,
                        "rfiOfflineSoleResponseParties=\"other\" OR rfiOfflineJointResponseParties=\"other\""
                    )
                    .optional(
                        RequestForInformationOfflineResponseDraft::getRfiOfflineResponseOtherEmail,
                        "rfiOfflineSoleResponseParties=\"other\" OR rfiOfflineJointResponseParties=\"other\""
                    )
                    .mandatory(RequestForInformationOfflineResponseDraft::getRfiOfflineAllDocumentsUploaded)
                    .mandatory(RequestForInformationOfflineResponseDraft::getRfiOfflineDraftResponseDetails)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE, details.getId());
        if (details.getData().getRequestForInformationList().getRequestsForInformation() == null
            || details.getData().getRequestForInformationList().getRequestsForInformation().isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(NO_REQUEST_FOR_INFORMATION_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE, details.getId());
        CaseData caseData = details.getData();

        final boolean allDocumentsUploaded = YES.equals(
            caseData.getRequestForInformationList().getRequestForInformationOfflineResponseDraft().getRfiOfflineAllDocumentsUploaded()
        );
        final State state = allDocumentsUploaded ? RequestedInformationSubmitted : AwaitingRequestedInformation;

        final RequestForInformationResponse response = new RequestForInformationResponse();
        final RequestForInformationOfflineResponseDraft offlineDraft =
            caseData.getRequestForInformationList().getRequestForInformationOfflineResponseDraft();
        response.setValues(caseData, offlineDraft);
        caseData.getRequestForInformationList().getLatestRequest().addResponseToList(response);

        caseData.getRequestForInformationList().setRequestForInformationOfflineResponseDraft(
            new RequestForInformationOfflineResponseDraft()
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CASEWORKER_REQUEST_FOR_INFORMATION_RESPONSE, details.getId());

        final CaseData caseData = details.getData();
        final RequestForInformation latestRequest = caseData.getRequestForInformationList().getLatestRequest();
        final RequestForInformationSoleParties soleParties = latestRequest.getRequestForInformationSoleParties();
        final RequestForInformationJointParties jointParties = latestRequest.getRequestForInformationJointParties();

        if ((caseData.getApplicationType().isSole() && !RequestForInformationSoleParties.OTHER.equals(soleParties))
            || (!caseData.getApplicationType().isSole() && !OTHER.equals(jointParties))
        ) {
            try {
                notificationDispatcher.sendRequestForInformationResponseNotification(
                    citizenRequestForInformationResponseNotification,
                    caseData,
                    details.getId()
                );
            } catch (final NotificationTemplateException e) {
                log.error(
                    REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_FAILED_ERROR,
                    details.getId(),
                    e.getMessage(),
                    e
                );
            }

            if (!caseData.getApplicationType().isSole()
                && BOTH.equals(caseData.getRequestForInformationList().getLatestRequest().getRequestForInformationJointParties())) {
                try {
                    notificationDispatcher.sendRequestForInformationResponsePartnerNotification(
                        citizenRequestForInformationResponsePartnerNotification,
                        caseData,
                        details.getId()
                    );
                } catch (final NotificationTemplateException e) {
                    log.error(
                        REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_FAILED_ERROR,
                        details.getId(),
                        e.getMessage(),
                        e
                    );
                }
            }
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
