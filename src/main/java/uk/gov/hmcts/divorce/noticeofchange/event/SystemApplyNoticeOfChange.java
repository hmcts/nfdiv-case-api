package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.LitigantGrantOfRepresentationConfirmationTemplateContent;
import uk.gov.hmcts.divorce.document.print.BulkPrintService;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.document.print.model.Print;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.NOC_APPROVER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest.acaRequest;
import static uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor.SOLICITOR_NOTICE_OF_CHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemApplyNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {
    public static final String NOTICE_OF_CHANGE_APPLIED = "notice-of-change-applied";
    public static final String LETTER_TYPE_GRANT_OF_REPRESENTATION = "grant-of-representation";

    private final  AuthTokenGenerator authTokenGenerator;
    private final  ObjectMapper objectMapper;
    private final  AssignCaseAccessClient assignCaseAccessClient;
    private final  IdamService idamService;
    private final ChangeOfRepresentativeService changeOfRepresentativeService;
    private final CaseDataDocumentService caseDataDocumentService;
    private final LitigantGrantOfRepresentationConfirmationTemplateContent templateContent;
    private final BulkPrintService bulkPrintService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(NOTICE_OF_CHANGE_APPLIED)
            .forStates(POST_SUBMISSION_STATES)
            .name("Notice Of Change Applied")
            .grant(CREATE_READ_UPDATE, NOC_APPROVER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, CASE_WORKER, SUPER_USER)
            .aboutToStartCallback(this::aboutToStart));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Applying notice of change for case id: {}", details.getId());

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();
        var changeOrganisationRequest = details.getData().getChangeOrganisationRequestField();
        boolean isApplicant1 = APPLICANT_1_SOLICITOR.getRole().equals(changeOrganisationRequest.getCaseRoleId().getRole());
        CaseData caseData = details.getData();

        changeOfRepresentativeService.buildChangeOfRepresentative(caseData, null,
                SOLICITOR_NOTICE_OF_CHANGE.getValue(), isApplicant1);

        AboutToStartOrSubmitCallbackResponse response =
            assignCaseAccessClient.applyNoticeOfChange(sysUserToken, s2sToken, acaRequest(details));

        Map<String, Object> data = response.getData();
        List<String> responseErrors = response.getErrors();

        if (Objects.nonNull(responseErrors) && !responseErrors.isEmpty()) {
            log.info("Notice of change failed with the following error(s) for CaseID {}:", details.getId());
            responseErrors.forEach(log::info);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(details.getData())
                    .state(details.getState())
                    .errors(responseErrors)
                    .build();
        }

        CaseData responseData = objectMapper.convertValue(data, CaseData.class);

        Applicant applicant = isApplicant1 ? responseData.getApplicant1() : responseData.getApplicant2();
        generateNoCNotificationLetterAndSend(responseData, details.getId(), applicant);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(responseData)
            .state(details.getState())
            .build();
    }

    private void generateNoCNotificationLetterAndSend(CaseData caseData, Long caseId, Applicant applicant) {

        Document generatedDocument = generateDocument(caseId, applicant, caseData);

        Letter letter = new  Letter(generatedDocument, 1);
        String caseIdString = String.valueOf(caseId);

        final Print print = new Print(
                List.of(letter),
                caseIdString,
                caseIdString,
                LETTER_TYPE_GRANT_OF_REPRESENTATION,
                applicant.getFullName(),
                applicant.getAddressOverseas()
        );

        final UUID letterId = bulkPrintService.print(print);

        log.info("Letter service responded with letter Id {} for case {}", letterId, caseId);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {

        return caseDataDocumentService.renderDocument(templateContent.getTemplateContent(caseData, caseId, applicant),
                caseId,
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_APP1_APP2_TEMPLATE_ID,
                applicant.getLanguagePreference(),
                NFD_NOTICE_OF_CHANGE_CONFIRMATION_DOCUMENT_NAME);
    }
}
