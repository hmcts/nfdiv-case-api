package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ClarificationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.PostInformationToCourtNotification;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
@RequiredArgsConstructor
public class RespondToRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String RESPOND_TO_REQUEST_FOR_INFORMATION = "respond-request-for-info";
    private static final String NEVER_SHOW = "coRefusalDecision=\"NEVER_SHOW\"";

    private final CcdAccessService ccdAccessService;
    private final HttpServletRequest request;


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(RESPOND_TO_REQUEST_FOR_INFORMATION)
            .forState(InformationRequested)
            .name("Submit response for rfi")
            .description("Submit response for RFI")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE_DELETE, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("respondRequestForInfo")
            .pageLabel("Submit response for RFI")
            .complex(CaseData::getRequestForInformationList)
                .complex(RequestForInformationList::getRequestForInformationResponse)
                    .readonly(RequestForInformationResponse::getRequestForInformationResponseDetails)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("{} about to submit callback invoked for Case Id: {}", RESPOND_TO_REQUEST_FOR_INFORMATION, details.getId());

        RequestForInformationResponse response = details.getData().getRequestForInformationList().getRequestForInformationResponse();

        if (isApplicant1(details.getId())) {
            response.setValues(details.getData(), APPLICANT1);
        } else if (isApplicant2(details.getId())) {
            response.setValues(details.getData(), APPLICANT2);
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList("Unable to determine citizen for Case Id: " + details.getId().toString()))
                .build();
        }
        details.getData().getRequestForInformationList().getLatestRequest().addResponseToList(response);
        details.getData().getRequestForInformationList().setRequestForInformationResponse(new RequestForInformationResponse());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(details.getState()) // Don't change state whilst wip. Do this later.
            .build();
    }

    private boolean isApplicant1(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private boolean isApplicant2(Long caseId) {
        return ccdAccessService.isApplicant2(request.getHeader(AUTHORIZATION), caseId);
    }

}
