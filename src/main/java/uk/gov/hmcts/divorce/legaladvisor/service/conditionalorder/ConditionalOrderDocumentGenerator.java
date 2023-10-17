package uk.gov.hmcts.divorce.legaladvisor.service.conditionalorder;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateCoversheet;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.CoversheetApplicantTemplateContent;
import uk.gov.hmcts.divorce.document.content.CoversheetSolicitorTemplateContent;
import uk.gov.hmcts.divorce.document.content.JudicialSeparationCoRefusalTemplateContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.model.Letter;
import uk.gov.hmcts.divorce.legaladvisor.service.task.CoRefusalTemplateContent;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.time.LocalDateTime.now;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT2_SOLICITOR;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConditionalOrderDocumentGenerator {

    private static final Set<DocumentType> COMMON_DOCUMENTS = Set.of(APPLICATION, COVERSHEET, CONDITIONAL_ORDER_REFUSAL);

    private final CoversheetSolicitorTemplateContent coversheetSolicitorTemplateContent;
    private final CoversheetApplicantTemplateContent coversheetApplicantTemplateContent;
    private final JudicialSeparationCoRefusalTemplateContent judicialSeparationCoRefusalTemplateContent;
    private final CoRefusalTemplateContent coRefusalTemplateContent;
    private final GenerateCoversheet generateCoversheet;
    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;

    public List<Letter> generateDocuments(final CaseData caseData,
                                          final Long caseId,
                                          final Applicant applicant,
                                          final CoRefusalDocPack documentPack) {

        Letter coversheet = new Letter(getCoversheet(caseId, applicant, caseData), 1);
        Letter coverLetter = new Letter(getCoverLetter(caseData, caseId, applicant, documentPack), 1);
        final Letter refusalLetter = firstElement(getLettersBasedOnContactPrivacy(caseData, CONDITIONAL_ORDER_REFUSAL));
        final List<Letter> coRefusalLetterPack = new ArrayList<>(List.of(coversheet, coverLetter, refusalLetter));

        if (documentPack.getDocumentPack().contains(APPLICATION)) {
            final Letter application = firstElement(getLettersBasedOnContactPrivacy(caseData, APPLICATION));

        }

        return coRefusalLetterPack;
    }

    private Document getCoversheet(long caseId, Applicant applicant, CaseData caseData) {

        final Map<String, Object> templateContent = applicant.isRepresented()
            ? coversheetSolicitorTemplateContent.apply(caseId, applicant)
            : coversheetApplicantTemplateContent.apply(caseData, caseId, applicant);

        final String templateId = applicant.isRepresented()
            ? COVERSHEET_APPLICANT2_SOLICITOR
            : COVERSHEET_APPLICANT;

        return generateCoversheet.generateAndGetCoversheet(caseId,
            templateId,
            templateContent,
            applicant.getLanguagePreference());
    }

    private Document getCoverLetter(final CaseData caseData,
                                    final long caseId,
                                    final Applicant applicant,
                                    final CoRefusalDocPack documentPack) {

        Map<String, Object> templateContent = caseData.isJudicialSeparationCase()
            ? judicialSeparationCoRefusalTemplateContent.templateContent(caseData, caseId, applicant)
            : coRefusalTemplateContent.templateContent(caseData, caseId, applicant);

        DocumentType letterToGenerate = Sets.difference(documentPack.getDocumentPack(), COMMON_DOCUMENTS)
            .stream()
            .findFirst()
            .orElseThrow();

        return caseDataDocumentService.renderUpdateAndFetchDocument(caseData,
            letterToGenerate,
            templateContent,
            caseId,
            documentPack.getCoverLetterTemplateId(),
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, REJECTED_REFUSAL_ORDER_COVER_LETTER_DOCUMENT_NAME, now(clock)));
    }

}
