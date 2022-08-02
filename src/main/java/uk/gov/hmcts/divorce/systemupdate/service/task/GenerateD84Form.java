package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;

@Component
@Slf4j
public class GenerateD84Form {

    private static final String D84_FILE_LOCATION = "/D84.pdf";
    private static final String D84_FILENAME = "D84.pdf";
    private static final String D84_DISPLAY_NAME = "D84";

    @Autowired
    private GenerateFormHelper generateFormHelper;

    public void generateD84Document(final CaseData caseData, Long caseId) {
        final boolean d84DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D84);

        if (!d84DocumentAlreadyGenerated) {
            try {
                log.info("Adding D84 to list of generated documents for case id: {}", caseId);
                generateFormHelper.addFormToGeneratedDocuments(caseData, D84, D84_DISPLAY_NAME, D84_FILENAME, D84_FILE_LOCATION);
            } catch (Exception e) {
                log.error("Error encountered whilst adding D84 document to list of generated documents for case id: {}", caseId);
            }
        }
    }
}
