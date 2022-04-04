package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CitizenRespondentAosInvitationTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicant2TemplateContent;
import uk.gov.hmcts.divorce.document.content.NoticeOfProceedingContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.time.Clock;
import java.util.Map;

import static java.time.LocalDateTime.now;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R1_SOLE_APP2_CIT_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@Slf4j
public class GenerateApplicant2NoticeOfProceedings implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private CitizenRespondentAosInvitationTemplateContent citizenRespondentAosInvitationTemplateContent;

    @Autowired
    private CoversheetApplicant2TemplateContent coversheetApplicant2TemplateContent;

    @Autowired
    private NoticeOfProceedingContent templateContent;

    @Autowired
    private Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating access code to allow the respondent to access the application");
        caseData.setCaseInvite(caseData.getCaseInvite().generateAccessCode());

        boolean isAddressKnown = isNull(caseData.getApplication().getApplicant1KnowsApplicant2Address())
            || YES.equals(caseData.getApplication().getApplicant1KnowsApplicant2Address());

        if (caseData.getApplicationType().isSole() && isAddressKnown) {

            final Applicant applicant2 = caseData.getApplicant2();

            if (applicant2.isRepresented()) {
                log.info("Generating solicitor respondent NoP for case id {} ", caseId);

                caseDataDocumentService.renderDocumentAndUpdateCaseData(
                    caseData,
                    NOTICE_OF_PROCEEDINGS_APP_2,
                    templateContent.apply(caseData, caseId),
                    caseId,
                    NFD_NOP_RS1_SOLE_APP2_SOL_ONLINE,
                    caseData.getApplicant1().getLanguagePreference(),
                    formatDocumentName(caseId, NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME, now(clock))
                );

            } else if (isNotEmpty(applicant2.getEmail())) {
                log.info("Generating citizen respondent(with email) NoP for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    NFD_NOP_R1_SOLE_APP2_CIT_ONLINE,
                    citizenRespondentAosInvitationTemplateContent.apply(caseData, caseId),
                    NOTICE_OF_PROCEEDINGS_APP_2,
                    NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME
                );
            } else if (isEmpty(applicant2.getEmail())) {
                log.info("Generating citizen respondent(without email) NoP for case id {} ", caseId);
                generateDocumentAndUpdateCaseData(
                    caseDetails,
                    NFD_NOP_R2_SOLE_APP2_CIT_OFFLINE,
                    citizenRespondentAosInvitationTemplateContent.apply(caseData, caseId),
                    NOTICE_OF_PROCEEDINGS_APP_2,
                    NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME
                );

                if (!caseData.getApplication().isPersonalServiceMethod()) {
                    log.info("Generating coversheet for case id {} ", caseId);
                    generateDocumentAndUpdateCaseData(
                        caseDetails,
                        COVERSHEET_APPLICANT,
                        coversheetApplicant2TemplateContent.apply(caseData, caseId),
                        COVERSHEET,
                        COVERSHEET_DOCUMENT_NAME
                    );
                }
            } else {
                log.info("Not generating AOS respondent invitation letter for case id {} ", caseId);
            }
        }

        return caseDetails;
    }

    private void generateDocumentAndUpdateCaseData(
        CaseDetails<CaseData, State> caseDetails,
        String templateId,
        Map<String, Object> templateContent,
        DocumentType documentType,
        String documentName
    ) {
        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            documentType,
            templateContent,
            caseId,
            templateId,
            caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, documentName, now(clock))
        );
    }
}
