package uk.gov.hmcts.divorce.systemupdate.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_DISPLAY_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_FILENAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.D84_FILE_LOCATION;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D84;

@Component
@Slf4j
public class GenerateD84Form implements CaseTask {

    @Autowired
    private GenerateFormHelper generateFormHelper;

    public void generateD84Document(final CaseData caseData, Long caseId) {
        final boolean d84DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D84);

        if (!d84DocumentAlreadyGenerated) {
            addD84ToGeneratedDocuments(caseData, caseId);
        }
    }

    public void generateD84(final CaseData caseData) {
        final boolean d84DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D84);

        if (!d84DocumentAlreadyGenerated) {
            addD84ToGeneratedDocuments(caseData);
        }
    }

    @Override
    public CaseDetails<CaseData, State> apply(CaseDetails<CaseData, State> caseDetails) {
        final CaseData caseData = caseDetails.getData();
        final Long caseId = caseDetails.getId();
        final boolean d84DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D84);

        var d84Needed = !caseData.getApplicationType().isSole() && caseData.isJudicialSeparationCase();

        if (d84Needed && !d84DocumentAlreadyGenerated) {
            addD84ToGeneratedDocuments(caseData, caseId);
        }

        return caseDetails;
    }

    private void addD84ToGeneratedDocuments(CaseData caseData, Long caseId) {
        try {
            log.info("Adding D84 to list of generated documents for case id: {}", caseId);
            generateFormHelper.addFormToGeneratedDocuments(caseData, D84, D84_DISPLAY_NAME, D84_FILENAME, D84_FILE_LOCATION);
        } catch (Exception e) {
            log.error("Error encountered whilst adding D84 document to list of generated documents for case id: {}", caseId);
        }
    }

    private void addD84ToGeneratedDocuments(CaseData caseData) {
        try {
            generateFormHelper.addFormToGeneratedDocuments(caseData, D84, D84_DISPLAY_NAME, D84_FILENAME, D84_FILE_LOCATION);
        } catch (Exception e) {
            throw new IllegalStateException("Could not generate d84");
        }
    }
}
