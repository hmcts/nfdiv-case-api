package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.hasAddedDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

@Component
@Slf4j
public class CaseworkerRemoveScannedDocument implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_REMOVE_SCANNED_DOCUMENT = "caseworker-remove-scanned-document";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_REMOVE_SCANNED_DOCUMENT)
            .forStates(POST_SUBMISSION_STATES)
            .name("Remove scanned document")
            .description("Remove scanned document")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary(false)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER)
            .grantHistoryOnly(SOLICITOR, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("removeScannedDocument")
            .pageLabel("Remove scanned document")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getScannedDocuments)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        final Long caseId = details.getId();
        final var caseData = details.getData();
        final var beforeCaseData = beforeDetails.getData();

        log.info("Callback invoked for: {} case id: {}", CASEWORKER_REMOVE_SCANNED_DOCUMENT, caseId);

        final List<ListValue<ScannedDocument>> after = caseData.getDocuments().getScannedDocuments();
        final List<ListValue<ScannedDocument>> before = beforeCaseData.getDocuments().getScannedDocuments();

        if (hasAddedDocuments(after, before)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of("Scanned documents cannot be added by 'Remove scanned documents'"))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
