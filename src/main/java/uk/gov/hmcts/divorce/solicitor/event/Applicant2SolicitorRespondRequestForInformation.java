package uk.gov.hmcts.divorce.solicitor.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseDraft;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT2SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.State.InformationRequested;
import static uk.gov.hmcts.divorce.divorcecase.model.State.RequestedInformationSubmitted;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class Applicant2SolicitorRespondRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String APP_2_SOLICITOR_RESPOND_REQUEST_INFO = "app2-solicitor-respond-request-info";
    public static final String MUST_ADD_DOCS_OR_DETAILS_ERROR = "You must upload a document or write a response";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
                .event(APP_2_SOLICITOR_RESPOND_REQUEST_INFO)
                .forState(InformationRequested)
                .name("Submit Response")
                .description("Submit response")
                .showSummary()
                .showEventNotes()
                .endButtonLabel("Submit")
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
                .page("requestForInformationResponse", this::midEvent)
                .pageLabel("Submit Response")
                .complex(CaseData::getRequestForInformationList)
                    .complex(RequestForInformationList::getRequestForInformationResponseApplicant2Solicitor)
                        .optional(RequestForInformationResponseDraft::getRfiDraftResponseDetails)
                        .optional(RequestForInformationResponseDraft::getRfiDraftResponseDocs)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        log.info("{} midEvent callback invoked for Case Id: {}", APP_2_SOLICITOR_RESPOND_REQUEST_INFO, details.getId());

        RequestForInformationResponseDraft response =
            details.getData().getRequestForInformationList().getRequestForInformationResponseApplicant2Solicitor();
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
        log.info("{} about to submit callback invoked for Case Id: {}", APP_2_SOLICITOR_RESPOND_REQUEST_INFO, details.getId());

        CaseData data = details.getData();
        RequestForInformationResponse requestForInformationResponse = new RequestForInformationResponse();
        requestForInformationResponse.setValues(data, APPLICANT2SOLICITOR);

        data.getRequestForInformationList().getLatestRequest().addResponseToList(requestForInformationResponse);

        //Prevent pre-populating fields for new requests
        data.getRequestForInformationList().setRequestForInformationResponseApplicant2Solicitor(new RequestForInformationResponseDraft());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(RequestedInformationSubmitted)
            .build();
    }
}
