package uk.gov.hmcts.divorce.document.print.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.D11GeneralApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static uk.gov.hmcts.divorce.document.DocumentConstants.D11_GENERAL_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D11_GENERAL_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_APPLICATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class D11GeneralApplicationGenerator {
    private final CaseDataDocumentService caseDataDocumentService;
    private final D11GeneralApplicationTemplateContent templateContent;

    public DivorceDocument generateDocument(final long caseId,
                                            final Applicant applicant,
                                            final CaseData caseData,
                                            final GeneralApplication generalApplication) {
        log.info("Generating D11 general application document for {}", caseId);

        Document document = caseDataDocumentService.renderDocument(
            templateContent.getTemplateContent(caseData, caseId, applicant, generalApplication),
            caseId,
            D11_GENERAL_APPLICATION_TEMPLATE_ID,
            applicant.getLanguagePreference(),
            D11_GENERAL_APPLICATION_DOCUMENT_NAME
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
