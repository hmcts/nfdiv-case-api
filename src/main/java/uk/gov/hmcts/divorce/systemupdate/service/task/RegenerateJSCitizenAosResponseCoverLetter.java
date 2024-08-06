package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.templatecontent.AosResponseLetterTemplateContent;

import java.time.Clock;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegenerateJSCitizenAosResponseCoverLetter implements CaseTask {

    private final AosResponseLetterTemplateContent aosResponseLetterTemplateContent;
    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        if (caseData.getApplicant1().isApplicantOffline() && !caseData.getApplicant1().isRepresented()) {
            regenerateApplicant1CoverSheet(caseId, caseData);
        }
        return caseDetails;
    }

    public void regenerateApplicant1CoverSheet(Long caseId, CaseData caseData) {
        log.info("Regenerating applicant 1 js aos response letter for case id {} ", caseId);
        generateJsCitizenAosResponseLetter(caseData, caseId, caseData.getApplicant1());
        caseData.getApplicant1().setJsCitizenAosResponseLetterRegenerated(YES);
    }

    public void generateJsCitizenAosResponseLetter(final CaseData caseData,
                                                   final Long caseId,
                                                   final Applicant applicant) {

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            COVERSHEET,
            aosResponseLetterTemplateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            COVERSHEET_APPLICANT,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, COVERSHEET_DOCUMENT_NAME, now(clock))
        );
    }
}
