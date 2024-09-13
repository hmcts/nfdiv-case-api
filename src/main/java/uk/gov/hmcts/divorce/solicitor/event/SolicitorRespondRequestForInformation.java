package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorRespondRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION = "solicitor-respond-request-info";
    public static final String MUST_ADD_DOCS_OR_DESCRIPTION_ERROR = "You must upload a document or write a response";
    public static final String UNABLE_TO_SUBMIT_RESPONSE_ERROR = "Unable to submit response for Case Id: ";

    private final CcdAccessService ccdAccessService;
    private final HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
                .event(SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION)
                .forStates(InformationRequested, RequestedInformationSubmitted)
                .name("Submit Response")
                .description("Submit response")
                .showSummary()
                .showEventNotes()
                .endButtonLabel("Submit")
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE, SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
                .page("requestForInformationResponse", this::midEvent)
                .pageLabel("Submit Response")
                .complex(CaseData::getRequestForInformationList)
                    .complex(RequestForInformationList::getRequestForInformationResponse)
                        .optional(RequestForInformationResponse::getRequestForInformationResponseDocs)
                        .optional(RequestForInformationResponse::getRequestForInformationResponseDetails)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        log.info("{} midEvent callback invoked for Case Id: {}", SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION, details.getId());

        RequestForInformationResponse response = details.getData().getRequestForInformationList().getRequestForInformationResponse();
        List<ListValue<DivorceDocument>> responseDocs = response.getRequestForInformationResponseDocs();
        String responseDetails = response.getRequestForInformationResponseDetails();

        if ((responseDocs == null || responseDocs.isEmpty()) && (responseDetails == null || responseDetails.isEmpty())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(MUST_ADD_DOCS_OR_DESCRIPTION_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} about to submit callback invoked for Case Id: {}", SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION, details.getId());

        CaseData data = details.getData();
        RequestForInformationResponse requestForInformationResponse =
            data.getRequestForInformationList().getRequestForInformationResponse();

        if (isApplicant1Solicitor(details.getId())) {
            requestForInformationResponse.setValues(data.getApplicant1(), APPLICANT1SOLICITOR);
        } else if (isApplicant2Solicitor(details.getId())) {
            requestForInformationResponse.setValues(data.getApplicant2(), APPLICANT2SOLICITOR);
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(UNABLE_TO_SUBMIT_RESPONSE_ERROR + details.getId()))
                .build();
        }

        data.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        //Prevent pre-populating fields for new requests
        data.getRequestForInformationList().setRequestForInformationResponse(new RequestForInformationResponse());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(RequestedInformationSubmitted)
            .build();
    }

    private boolean isApplicant1Solicitor(Long caseId) {
        return ccdAccessService.isApplicant1(request.getHeader(AUTHORIZATION), caseId);
    }

    private boolean isApplicant2Solicitor(Long caseId) {
        return ccdAccessService.isApplicant2(request.getHeader(AUTHORIZATION), caseId);
    }
}
