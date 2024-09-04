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
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformation;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationList;
import uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.RequestForInformationResponseParties.APPLICANT1SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolicitorRespondRequestForInformation implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION = "solicitor-respond-request-info";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
                .event(SOLICITOR_RESPOND_REQUEST_FOR_INFORMATION)
                .forAllStates()
                .name("Request For Info Response")
                .description("Request for information response")
                .showSummary()
                .showEventNotes()
                .endButtonLabel("Submit")
                .aboutToSubmitCallback(this::aboutToSubmit)
                .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
                .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
                .page("requestForInformationResponse")
                .pageLabel("Request For Information Response")
                .complex(CaseData::getRequestForInformationList)
                    .complex(RequestForInformationList::getRequestForInformationResponse)
                        .mandatory(RequestForInformationResponse::getRequestForInformationResponseDetails)
                        .optional(RequestForInformationResponse::getRequestForInformationResponseDocs)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {
        log.info("Solicitor request for information response about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();
        RequestForInformationResponse requestForInformationResponse =
            data.getRequestForInformationList().getRequestForInformationResponse();

        requestForInformationResponse.setRequestForInformationResponseParties(APPLICANT1SOLICITOR);
        requestForInformationResponse.setRequestForInformationResponseName(data.getApplicant1().getSolicitor().getName());
        requestForInformationResponse.setRequestForInformationResponseEmailAddress(data.getApplicant1().getSolicitor().getEmail());
        requestForInformationResponse.setRequestForInformationResponseDateTime(LocalDateTime.now());

        final ListValue<RequestForInformationResponse> newResponse = new ListValue<>();
        newResponse.setValue(requestForInformationResponse);

        final RequestForInformation latestRequestForInformation = data.getRequestForInformationList().getLatestRequest();
        if (isEmpty(latestRequestForInformation.getRequestForInformationResponses())) {
            List<ListValue<RequestForInformationResponse>> responses = new ArrayList<>();
            responses.add(newResponse);
            latestRequestForInformation.setRequestForInformationResponses(responses);
        } else {
            latestRequestForInformation.getRequestForInformationResponses().add(0, newResponse);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
