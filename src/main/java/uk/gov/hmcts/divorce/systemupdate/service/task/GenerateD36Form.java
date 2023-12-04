package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D36;

@Component
@Slf4j
public class GenerateD36Form {

    private static final String D36_FILE_LOCATION = "/D36.pdf";
    private static final String D36_FILENAME = "D36.pdf";
    private static final String D36_DISPLAY_NAME = "D36";

    @Autowired
    private GenerateFormHelper generateFormHelper;

    public void generateD36Document(final CaseData caseData) {
        final boolean d36DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D36);

        if (!d36DocumentAlreadyGenerated) {
            try {
                generateFormHelper.addFormToGeneratedDocuments(caseData, D36, D36_DISPLAY_NAME, D36_FILENAME, D36_FILE_LOCATION);
            } catch (Exception e) {
                log.error("Error encountered whilst adding D36 document to list of generated documents");
            }
        }
    }
}
