package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.hasDeletedDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerUploadGeneralReferralDocuments implements CCDConfig<CaseData, State, UserRole> {
    private static final String NEVER_SHOW = "generalReferralDocuments=\"NEVER_SHOW\"";
    public static final String UPLOAD_GENERAL_REFERRAL_DOCS = "cw-upload-gen-referral-docs";
    private static final String UPLOAD_GENERAL_REFERRAL_DOCS_NAME = "Upload general referral docs";
    public static final String CANNOT_REMOVE_REMOVE_DOCUMENTS = "Already uploaded documents cannot be removed. "
        + "Use 'Remove documents' to remove documents from general referral.";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(UPLOAD_GENERAL_REFERRAL_DOCS)
            .forAllStates()
            .name(UPLOAD_GENERAL_REFERRAL_DOCS_NAME)
            .description(UPLOAD_GENERAL_REFERRAL_DOCS_NAME)
            .showCondition("generalReferralReason=\"*\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, SOLICITOR))
            .page("cwUploadGeneralReferralDocs")
            .pageLabel(UPLOAD_GENERAL_REFERRAL_DOCS_NAME)
            .complex(CaseData::getGeneralReferral)
            .readonlyNoSummary(GeneralReferral::getGeneralReferralReason, NEVER_SHOW)
            .optional(GeneralReferral::getGeneralReferralDocuments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Callback invoked for {}, Case Id: {}", UPLOAD_GENERAL_REFERRAL_DOCS, details.getId());

        var caseData = details.getData();
        final var beforeCaseData = beforeDetails.getData();

        final List<ListValue<DivorceDocument>> after = caseData.getGeneralReferral().getGeneralReferralDocuments();
        final List<ListValue<DivorceDocument>> before = beforeCaseData.getGeneralReferral().getGeneralReferralDocuments();

        if (hasDeletedDocuments(after, before)) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(List.of(CANNOT_REMOVE_REMOVE_DOCUMENTS))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
