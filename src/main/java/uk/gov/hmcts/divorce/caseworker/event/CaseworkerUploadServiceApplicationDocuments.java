package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.hasAddedDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServicePayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerUploadServiceApplicationDocuments implements CCDConfig<CaseData, State, UserRole> {
    private static final String NEVER_SHOW = "alternativeServiceJudgeOrLegalAdvisorDetails=\"NEVER_SHOW\"";
    public static final String UPLOAD_SERVICE_APPLICATION_DOCS = "cw-upload-service-app-docs";
    private static final String UPLOAD_SERVICE_APPLICATION_DOCS_NAME = "Upload service app docs";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(UPLOAD_SERVICE_APPLICATION_DOCS)
            .forStates(
                AwaitingDocuments
            )
            .name(UPLOAD_SERVICE_APPLICATION_DOCS_NAME)
            .description(UPLOAD_SERVICE_APPLICATION_DOCS_NAME)
            .showCondition("serviceApplicationDocsUploadedPreSubmission=\"No\"")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, LEGAL_ADVISOR, JUDGE, SOLICITOR))
            .page("cwUploadServiceAppDocs", this::midEvent)
            .pageLabel(UPLOAD_SERVICE_APPLICATION_DOCS_NAME)
            .complex(CaseData::getAlternativeService)
            .readonlyNoSummary(AlternativeService::getServiceApplicationDocsUploadedPreSubmission, NEVER_SHOW)
            .optional(AlternativeService::getServiceApplicationDocuments)
            .optional(AlternativeService::getAlternativeServiceJudgeOrLegalAdvisorDetails)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("{} midEvent callback invoked for Case Id: {}", UPLOAD_SERVICE_APPLICATION_DOCS_NAME, details.getId());

        CaseData caseData = details.getData();
        AlternativeService alternativeService = caseData.getAlternativeService();
        AlternativeService beforeAlternativeService = detailsBefore.getData().getAlternativeService();

        List<String> validationErrors = new ArrayList<>();

        boolean newDocumentsAdded = hasAddedDocuments(alternativeService.getServiceApplicationDocuments(),
            beforeAlternativeService.getServiceApplicationDocuments());

        if (!newDocumentsAdded && StringUtils.isEmpty(alternativeService.getAlternativeServiceJudgeOrLegalAdvisorDetails())) {
            validationErrors.add("Please upload supporting documents and/or provide further details for Judge or Legal Advisor.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(validationErrors)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}", UPLOAD_SERVICE_APPLICATION_DOCS_NAME, details.getId());

        var caseData = details.getData();
        AlternativeService alternativeService = caseData.getAlternativeService();

        State endState = AwaitingServiceConsideration;

        if (StringUtils.isNotEmpty(alternativeService.getServicePaymentFee().getHelpWithFeesReferenceNumber())) {
            endState = AwaitingServicePayment;
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(endState)
            .build();
    }
}

