package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D11;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenerateD11Form {

    private static final String D11_FILE_LOCATION = "/D11.pdf";
    private static final String D11_FILENAME = "D11.pdf";
    private static final String D11_DISPLAY_NAME = "D11";

    private final GenerateFormHelper generateFormHelper;

    public void generateD11Document(final CaseData caseData) {
        final boolean d11DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D11);

        if (!d11DocumentAlreadyGenerated) {
            try {
                generateFormHelper.addFormToGeneratedDocuments(caseData, D11, D11_DISPLAY_NAME, D11_FILENAME, D11_FILE_LOCATION);
            } catch (Exception e) {
                log.error("Error encountered whilst adding D11 document to list of generated documents");
            }
        }
    }
}
