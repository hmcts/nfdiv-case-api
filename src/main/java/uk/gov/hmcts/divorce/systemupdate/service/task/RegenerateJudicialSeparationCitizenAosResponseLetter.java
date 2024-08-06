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
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentUtil.removeDocumentsBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegenerateJudicialSeparationCitizenAosResponseLetter implements CaseTask {

    private final AosResponseLetterTemplateContent aosResponseLetterTemplateContent;
    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        if (caseData.getApplicant1().isApplicantOffline() && !caseData.getApplicant1().isRepresented()) {
            removeAndRegenerateApplicant1(caseId, caseData);
        }
        return caseDetails;
    }

    public void removeAndRegenerateApplicant1(Long caseId, CaseData caseData) {
        boolean anyDocRemoved = removeExistingCoverLetterIfAny(caseData);

        if (anyDocRemoved) {
            log.info("Regenerating applicant 1 js aos response letter for case id {} ", caseId);
            generateJsCitizenAosResponseLetter(caseData, caseId, caseData.getApplicant1());
            caseData.getApplicant1().setJsCitizenAosResponseLetterRegenerated(YES);
        }
    }

    private boolean removeExistingCoverLetterIfAny(final CaseData caseData) {
        return removeDocumentsBasedOnContactPrivacy(caseData, AOS_RESPONSE_LETTER);
    }

    public void generateJsCitizenAosResponseLetter(final CaseData caseData,
                                                   final Long caseId,
                                                   final Applicant applicant) {

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            AOS_RESPONSE_LETTER,
            aosResponseLetterTemplateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            getTemplateId(caseData),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, AOS_RESPONSE_LETTER_DOCUMENT_NAME, now(clock))
        );
    }

    private String getTemplateId(CaseData caseData) {
        if (caseData.getAcknowledgementOfService().isDisputed()) {
            return NFD_NOP_APP1_JS_SOLE_DISPUTED;
        } else {
            return NFD_NOP_APP1_JS_SOLE_UNDISPUTED;
        }
    }
}
