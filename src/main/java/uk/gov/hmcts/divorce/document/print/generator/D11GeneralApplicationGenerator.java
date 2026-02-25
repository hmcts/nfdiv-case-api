package uk.gov.hmcts.divorce.document.print.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.SearchGovRecordsApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.LocalDate;

import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_APPLICATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class D11GeneralApplicationGenerator {
    private final CaseDataDocumentService caseDataDocumentService;
    private final SearchGovRecordsApplicationTemplateContent templateContent;

    public DivorceDocument generateDocument(final long caseId,
                                            final Applicant applicant,
                                            final CaseData caseData,
                                            final GeneralApplication generalApplication) {
        log.info("Generating general application d11 document for {}", caseId);

        return DivorceDocument
            .builder()
            .documentFileName("test.pdf")
            .documentType(GENERAL_APPLICATION)
            .documentDateAdded(LocalDate.now())
            .build();
    }
}
