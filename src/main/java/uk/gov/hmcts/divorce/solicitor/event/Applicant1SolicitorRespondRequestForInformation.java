package uk.gov.hmcts.divorce.solicitor.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.citizen.notification.CitizenRequestForInformationResponsePartnerNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationAuthParty;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationAuthParty.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationAuthParty.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationJointParties.BOTH;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class Applicant1SolicitorRespondRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String APP_1_SOLICITOR_RESPOND_REQUEST_INFO = "app1-solicitor-respond-request-info";
    public static final String MUST_ADD_DOCS_OR_DETAILS_ERROR = "You must upload a document or write a response";
    public static final String NOT_AUTHORISED_TO_RESPOND_ERROR = "You are not authorised to respond to this request.";
    public static final String REQUEST_FOR_INFORMATION_RESPONSE_PARTNER_NOTIFICATION_FAILED_ERROR
        = "Request for Information Response Partner Notification for Case Id {} failed with message: {}";

    private final NotificationDispatcher notificationDispatcher;
    private final CitizenRequestForInformationResponsePartnerNotification citizenRequestForInformationResponsePartnerNotification;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
                .event(APP_1_SOLICITOR_RESPOND_REQUEST_INFO)
                .forState(InformationRequested)
                .showCondition("requestForInformationAuthParty=\"applicant1\" OR requestForInformationAuthParty=\"both\"")
                .name("Submit Response")
                .description("Submit response")
                .showSummary()
                .showEventNotes()
                .endButtonLabel("Submit")
                .aboutToStartCallback(this::aboutToStart)
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::submitted)
                .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
                .page("requestForInformationResponse", this::midEvent)
                .pageLabel("Submit Response")
                .complex(CaseData::getRequestForInformationList)
                    .readonly(RequestForInformationList::getLatestRequestForInformationDetails)
                    .complex(RequestForInformationList::getRequestForInformationResponseApplicant1Solicitor)
                        .optional(RequestForInformationResponseDraft::getRfiDraftResponseDetails)
                        .optional(RequestForInformationResponseDraft::getRfiDraftResponseDocs)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(CaseDetails<CaseData, State> details) {

        log.info("{} aboutToStart callback invoked for Case Id: {}", APP_1_SOLICITOR_RESPOND_REQUEST_INFO, details.getId());

        RequestForInformationAuthParty authParty = details.getData().getRequestForInformationList().getRequestForInformationAuthParty();

        if (APPLICANT2.equals(authParty) || OTHER.equals(authParty)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(NOT_AUTHORISED_TO_RESPOND_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        log.info("{} midEvent callback invoked for Case Id: {}", APP_1_SOLICITOR_RESPOND_REQUEST_INFO, details.getId());

        RequestForInformationResponseDraft response =
            details.getData().getRequestForInformationList().getRequestForInformationResponseApplicant1Solicitor();
        List<ListValue<DivorceDocument>> responseDocs = response.getRfiDraftResponseDocs();
        String responseDetails = response.getRfiDraftResponseDetails();

        if ((responseDocs == null || responseDocs.isEmpty()) && (responseDetails == null || responseDetails.isEmpty())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(MUST_ADD_DOCS_OR_DETAILS_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", APP_1_SOLICITOR_RESPOND_REQUEST_INFO, details.getId());

        CaseData data = details.getData();
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(data, APPLICANT1SOLICITOR);

        data.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        //Prevent pre-populating fields for new requests
        data.getRequestForInformationList().setRequestForInformationResponseApplicant1Solicitor(new RequestForInformationResponseDraft());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(RequestedInformationSubmitted)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for Case Id: {}", APP_1_SOLICITOR_RESPOND_REQUEST_INFO, details.getId());

        if (!details.getData().getApplicationType().isSole()
            && BOTH.equals(details.getData().getRequestForInformationList().getLatestRequest().getRequestForInformationJointParties())
        ) {
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
}
