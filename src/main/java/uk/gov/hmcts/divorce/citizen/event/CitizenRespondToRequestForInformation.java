package uk.gov.hmcts.divorce.citizen.event;

import jakarta.servlet.http.HttpServletRequest;
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
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationSoleParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingRequestedInformation;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CitizenRespondToRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String CITIZEN_RESPOND_TO_REQUEST_FOR_INFORMATION = "citizen-respond-request-for-information";
    public static final String CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR = "Unable to apply response. Applicant ";
    public static final String CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR = "not valid for Party (";
    public static final String CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR = ") on latest RFI for Case Id: ";
    public static final String REQUEST_FOR_INFORMATION_RESPONSE_NOTIFICATION_FAILED_ERROR
        = "Request for Information Response Notification for Case Id {} failed with message: {}";
    public static final String REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_FAILED_ERROR
        = "Request for Information Response Partner Notification for Case Id {} failed with message: {}";

    private final CcdAccessService ccdAccessService;
    private final HttpServletRequest request;
    private final NotificationDispatcher notificationDispatcher;
    private final CitizenRequestForInformationResponseNotification citizenRequestForInformationResponseNotification;
    private final CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CITIZEN_RESPOND_TO_REQUEST_FOR_INFORMATION)
            .forState(InformationRequested)
            .name("Submit response for rfi")
            .description("Submit response for RFI")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("respondRequestForInfo")
            .pageLabel("Submit response for RFI");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", CITIZEN_RESPOND_TO_REQUEST_FOR_INFORMATION, details.getId());

        final RequestForInformationList requestForInformationList = details.getData().getRequestForInformationList();
        final RequestForInformationResponse response = new RequestForInformationResponse();
        RequestForInformationResponseParties responseParty = isApplicant1(details.getId()) ? APPLICANT1 : APPLICANT2;

        if (applicantCanRespondToRequestForInformation(details.getData(), responseParty)) {
            response.setValues(details.getData(), responseParty);
            clearDraft(details.getData(), responseParty);
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(buildErrorString(details, responseParty)))
                .build();
        }

        requestForInformationList.getLatestRequest().addResponseToList(response);

        final State state =
            YES.equals(requestForInformationList.getLatestRequest().getLatestResponse().getRequestForInformationResponseCannotUploadDocs())
            ? AwaitingRequestedInformation
            : RequestedInformationSubmitted;

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(state)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", CITIZEN_RESPOND_TO_REQUEST_FOR_INFORMATION, details.getId());

        try {
            notificationDispatcher.sendRequestForInformationResponseNotification(
                citizenRequestForInformationResponseNotification,
                details.getData(),
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

        if (!details.getData().getApplicationType().isSole()) {
            try {
                notificationDispatcher.sendRequestForInformationResponsePartnerNotification(
                    citizenRequestForInformationResponsePartnerNotification,
                    details.getData(),
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

        return SubmittedCallbackResponse.builder().build();
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private boolean applicantCanRespondToRequestForInformation(CaseData data, RequestForInformationResponseParties party) {
        boolean isJoint = !data.getApplicationType().isSole();
        RequestForInformationSoleParties soleParties =
            data.getRequestForInformationList().getLatestRequest().getRequestForInformationSoleParties();
        RequestForInformationJointParties jointParties =
            data.getRequestForInformationList().getLatestRequest().getRequestForInformationJointParties();
        boolean solePartyApplicant = RequestForInformationSoleParties.APPLICANT.equals(soleParties);
        boolean jointPartyApplicant1 = RequestForInformationJointParties.APPLICANT1.equals(jointParties);
        boolean jointPartyApplicant2 = RequestForInformationJointParties.APPLICANT2.equals(jointParties);
        boolean jointPartyBoth = BOTH.equals(jointParties);

        if (APPLICANT1.equals(party)) {
            return isJoint ? jointPartyApplicant1 || jointPartyBoth : solePartyApplicant;
        } else {
            return isJoint && (jointPartyApplicant2 || jointPartyBoth);
        }
    }

    private void clearDraft(CaseData data, RequestForInformationResponseParties party) {
        RequestForInformationList requestForInformationList = data.getRequestForInformationList();

        if (APPLICANT1.equals(party)) {
            requestForInformationList.setRequestForInformationResponseApplicant1(new RequestForInformationResponseDraft());
        } else {
            requestForInformationList.setRequestForInformationResponseApplicant2(new RequestForInformationResponseDraft());
        }
    }

    private String buildErrorString(CaseDetails<CaseData, State> details, RequestForInformationResponseParties party) {
        final boolean isSole = details.getData().getApplicationType().isSole();
        final boolean isApplicant1 = APPLICANT1.equals(party);
        final RequestForInformation latestRequest = details.getData().getRequestForInformationList().getLatestRequest();
        String requestParty = isSole
            ? latestRequest.getRequestForInformationSoleParties().toString()
            : latestRequest.getRequestForInformationJointParties().toString();
        String applicant = isApplicant1 ? "1 " : "2 ";

        return CITIZEN_NOT_VALID_FOR_PARTY_START_ERROR
            + (isSole && isApplicant1 ? "" : applicant)
            + CITIZEN_NOT_VALID_FOR_PARTY_MID_ERROR
            + requestParty
            + CITIZEN_NOT_VALID_FOR_PARTY_END_ERROR
            + details.getId();
    }
}
