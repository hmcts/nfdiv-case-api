package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.content.templatecontent.CoversheetApplicantTemplateContent;

import java.time.Clock;
import java.time.LocalDate;

import static java.time.LocalDateTime.now;
import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AMENDED_APPLICATION_COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;

@Component
@Slf4j
public class CaseworkerIssueAmendedApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_ISSUE_AMENDED_APPLICATION = "caseworker-issue-amended-application";
    private static final long ISSUE_AMENDED_APPLICATION_OFFSET_DAYS = 16;

    @Autowired
    private Clock clock;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CASEWORKER_ISSUE_AMENDED_APPLICATION)
            .forStateTransition(GeneralConsiderationComplete, AwaitingAos)
            .name("Issue amended application")
            .description("Issue amended application")
            .grant(CREATE_READ_UPDATE_DELETE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker Issue Amended Application about to submit callback invoked for case id: {}", details.getId());

        CaseData caseData = details.getData();

        if (isEmpty(caseData.getDocuments().getAmendedApplications())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("Amended application is not uploaded"))
                .build();
        }

        caseData.setDueDate(LocalDate.now(clock).plusDays(ISSUE_AMENDED_APPLICATION_OFFSET_DAYS));
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);

        Long caseId = details.getId();
        Applicant applicant = caseData.getApplicant1();

        generateCoversheet.generateCoversheet(
            caseData,
            caseId,
            COVERSHEET_APPLICANT,
            coversheetApplicantTemplateContent.apply(caseData, caseId, applicant),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, AMENDED_APPLICATION_COVERSHEET_DOCUMENT_NAME, now(clock))
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
