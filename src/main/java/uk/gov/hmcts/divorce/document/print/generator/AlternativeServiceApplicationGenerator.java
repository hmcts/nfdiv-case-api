package uk.gov.hmcts.divorce.document.print.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.AlternativeServiceApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static uk.gov.hmcts.divorce.document.DocumentConstants.ALTERNATIVE_SERVICE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.ALTERNATIVE_SERVICE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.ALTERNATIVE_SERVICE;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlternativeServiceApplicationGenerator {
    private final CaseDataDocumentService caseDataDocumentService;
    private final AlternativeServiceApplicationTemplateContent templateContent;

    public DivorceDocument generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData) {
        log.info("Generating alternative service application document for {}", caseId);

        Document document = caseDataDocumentService.renderDocument(
            templateContent.getTemplateContent(caseData, caseId, applicant),
            caseId,
            ALTERNATIVE_SERVICE_APPLICATION_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            ALTERNATIVE_SERVICE_APPLICATION_DOCUMENT_NAME
        );

        return DivorceDocument
          .builder()
          .documentLink(document)
          .documentFileName(document.getFilename())
          .documentType(ALTERNATIVE_SERVICE)
          .build();
    }
}
