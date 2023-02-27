package uk.gov.hmcts.divorce.common.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateD10Form;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.AosResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.document.content.AosUndefendedResponseLetterTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.DocumentConstants.AOS_RESPONSE_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;

@Component
@Slf4j
public class GenerateAosResponseLetterDocument implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private AosResponseLetterTemplateContent aosResponseLetterTemplateContent;

    @Autowired
    private AosUndefendedResponseLetterTemplateContent aosUndefendedResponseLetterTemplateContent;

    @Autowired
    private GenerateD84Form generateD84Form;

    @Autowired
    private GenerateD10Form generateD10Form;

    @Autowired
    private GenerateCoversheet generateCoversheet;

    @Autowired
    private CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();
        final AcknowledgementOfService acknowledgementOfService = caseData.getAcknowledgementOfService();

        if (caseData.getApplicant1().isApplicantOffline()) {
            if (caseData.isJudicialSeparationCase()) {
                if (acknowledgementOfService.isDisputed()) {
                    generateD84Form.generateD84Document(caseData, caseId);
                    generateCoversheet.generateCoversheet(
                        caseData,
                        caseId,
                        COVERSHEET_APPLICANT,
                        coversheetApplicantTemplateContent.apply(caseData, caseId, caseData.getApplicant1()),
                        caseData.getApplicant1().getLanguagePreference()
                    );

                    if (caseData.getApplicant1().isRepresented()) {
                        log.info("Generating Solicitor JS aos response (disputed) letter pdf for case id: {}", caseDetails.getId());
                        caseDataDocumentService.renderDocumentAndUpdateCaseData(
                            caseData,
                            AOS_RESPONSE_LETTER,
                            aosResponseLetterTemplateContent.apply(caseData, caseId),
                            caseId,
                            NFD_NOP_APP1_SOL_JS_SOLE_DISPUTED,
                            caseData.getApplicant1().getLanguagePreference(),
                            AOS_RESPONSE_LETTER_DOCUMENT_NAME
                        );

                    } else {
                        log.info("Generating JS aos response (disputed) letter pdf for case id: {}", caseDetails.getId());
                        caseDataDocumentService.renderDocumentAndUpdateCaseData(
                            caseData,
                            AOS_RESPONSE_LETTER,
                            aosResponseLetterTemplateContent.apply(caseData, caseId),
                            caseId,
                            NFD_NOP_APP1_JS_SOLE_DISPUTED,
                            caseData.getApplicant1().getLanguagePreference(),
                            AOS_RESPONSE_LETTER_DOCUMENT_NAME
                        );
                    }
                } else {
                    if (caseData.getApplicant1().isRepresented()) {
                        log.info("Generating aos response (undefended) JS letter pdf, D10 and D84 forms for case id: {}", caseId);

                        generateD10Form.apply(caseDetails);
                        generateD84Form.generateD84Document(caseData, caseId);

                        caseDataDocumentService.renderDocumentAndUpdateCaseData(
                            caseData,
                            AOS_RESPONSE_LETTER,
                            aosUndefendedResponseLetterTemplateContent.apply(caseData, caseId),
                            caseId,
                            NFD_NOP_APP1_SOL_JS_SOLE_UNDISPUTED,
                            caseData.getApplicant1().getLanguagePreference(),
                            AOS_RESPONSE_LETTER_DOCUMENT_NAME
                        );
                    }
                }
            } else {
                if (acknowledgementOfService.isDisputed()) {
                    log.info("Generating aos response (disputed) letter pdf for case id: {}", caseDetails.getId());
                    caseDataDocumentService.renderDocumentAndUpdateCaseData(
                        caseData,
                        AOS_RESPONSE_LETTER,
                        aosResponseLetterTemplateContent.apply(caseData, caseId),
                        caseId,
                        RESPONDENT_RESPONDED_DISPUTED_TEMPLATE_ID,
                        caseData.getApplicant1().getLanguagePreference(),
                        AOS_RESPONSE_LETTER_DOCUMENT_NAME
                    );
                } else {
                    log.info("Generating aos response (undefended) letter pdf for case id: {}", caseId);
                    caseDataDocumentService.renderDocumentAndUpdateCaseData(
                        caseData,
                        AOS_RESPONSE_LETTER,
                        aosUndefendedResponseLetterTemplateContent.apply(caseData, caseId),
                        caseId,
                        RESPONDENT_RESPONDED_UNDEFENDED_TEMPLATE_ID,
                        caseData.getApplicant1().getLanguagePreference(),
                        AOS_RESPONSE_LETTER_DOCUMENT_NAME
                    );
                }
            }
        }
        return caseDetails;
    }
}
