package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosOverdue;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.BailiffRefused;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceAdminRefusal;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerResponseToServiceApplication implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION = "caseworker-response-to-service-application";
    private static final String ALTERNATIVE_SERVICE_TYPE_NULL_ERROR = "Please set the alternative service type before using this event";
    private static final int MAX_ATTACHMENTS_PER_APP = 5;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_RESPONSE_TO_SERVICE_APPLICATION)
            .forStates(AwaitingAos, AosOverdue, BailiffRefused, ServiceAdminRefusal)
            .name("Response to service app")
            .description("Response to service application")
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grant(CREATE_READ_UPDATE_DELETE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, JUDGE))
            .page("uploadDocument", this::midEvent)
            .pageLabel("Upload document")
            .complex(CaseData::getAlternativeService)
            .mandatory(AlternativeService::getAlternativeServiceType)
            .mandatory(AlternativeService::getReceivedServiceApplicationDate)
            .mandatory(AlternativeService::getAlternativeServiceJudgeOrLegalAdvisorDetails)
            .optional(AlternativeService::getServiceApplicationDocuments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Caseworker response to service application about to start callback invoked for Case Id: {}", details.getId());

        List<String> validationErrors = new java.util.ArrayList<>(Collections.emptyList());
        CaseData caseData = details.getData();
        var altServiceTypeList = caseData.getAlternativeServiceOutcomes();

        // We get the alternative service outcome from index 0 as the latest alternative service outcome is always added to index 0 on the
        // alternative outcomes list.
        if (isEmpty(altServiceTypeList) || isNull(altServiceTypeList.get(0).getValue().getAlternativeServiceType())) {
            validationErrors.add(ALTERNATIVE_SERVICE_TYPE_NULL_ERROR);
        }

        if (!validationErrors.isEmpty()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(validationErrors)
                .data(caseData)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Caseworker response to service application midEvent callback invoked for Case Id: {}", details.getId());
        CaseData caseData = details.getData();
        List<String> errors = new ArrayList<>();

        AlternativeService altService = caseData.getAlternativeService();
        if (isNotEmpty(altService.getServiceApplicationDocuments())
            && altService.getServiceApplicationDocuments().size() > MAX_ATTACHMENTS_PER_APP) {
            errors.add(String.format("Maximum supported uploads is %s", MAX_ATTACHMENTS_PER_APP));
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker response to service application about to submit callback invoked for Case Id: {}", details.getId());

        CaseData caseData = details.getData();
        AlternativeService altService = caseData.getAlternativeService();
        AlternativeServiceType altServiceType = altService.getAlternativeServiceType();

        if (isNotEmpty(altService.getServiceApplicationDocuments())) {
            altService.getServiceApplicationDocuments().forEach(divorceDocumentListValue -> {
                caseData.getDocuments().setDocumentsUploaded(
                    addDocumentToTop(caseData.getDocuments().getDocumentsUploaded(), divorceDocumentListValue.getValue()));
            });
        }

        State state;
        if (DEEMED.equals(altServiceType) || DISPENSED.equals(altServiceType)) {
            state = AwaitingServiceConsideration;
        } else {
            state = AwaitingBailiffReferral;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(state)
            .build();
    }
}
