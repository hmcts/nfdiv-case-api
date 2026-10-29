package uk.gov.hmcts.divorce.document.print.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.GeneralApplicationD11TemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_APPLICATION_D11_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_APPLICATION_D11_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_APPLICATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneralApplicationD11Generator {
    private final CaseDataDocumentService caseDataDocumentService;
    private final GeneralApplicationD11TemplateContent templateContent;

    public DivorceDocument generateDocument(final long caseId,
                                            final Applicant applicant,
                                            final CaseData caseData,
                                            final GeneralApplication generalApplication) {
        log.info("Generating general application D11 document for {}", caseId);

        Document document = caseDataDocumentService.renderDocument(
            templateContent.getTemplateContent(caseData, caseId, applicant, generalApplication),
            caseId,
            GENERAL_APPLICATION_D11_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            GENERAL_APPLICATION_D11_DOCUMENT_NAME
        );

        return DivorceDocument
            .builder()
            .documentLink(document)
            .documentFileName(document.getFilename())
            .documentType(GENERAL_APPLICATION)
            .documentDateAdded(LocalDate.now())
            .build();
    }
}
