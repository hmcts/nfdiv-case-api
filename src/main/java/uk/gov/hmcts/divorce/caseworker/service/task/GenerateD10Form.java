package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.task.GenerateFormHelper;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;

import static uk.gov.hmcts.divorce.document.DocumentUtil.documentsWithDocumentType;
import static uk.gov.hmcts.divorce.document.model.DocumentType.D10;

@Component
@Slf4j
public class GenerateD10Form implements CaseTask {

    private static final String D10_FILE_LOCATION = "/D10.pdf";
    private static final String D10_FILENAME = "D10.pdf";
    private static final String D10_DISPLAY_NAME = "D10";

    @Autowired
    private GenerateFormHelper generateFormHelper;

    public void apply(final CaseData caseData) {
        apply(caseData, null);
    }

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        apply(caseDetails.getData(), caseDetails.getId());
        return caseDetails;
    }

    private void apply(final CaseData caseData, final Long caseId) {
        final boolean d10DocumentAlreadyGenerated =
            documentsWithDocumentType(caseData.getDocuments().getDocumentsGenerated(), D10);

        var app2 = caseData.getApplicant2();
        var app2Offline = app2.isRepresented() && app2.getSolicitor() != null
            ? !app2.getSolicitor().hasOrgId()
            : StringUtils.isEmpty(caseData.getApplicant2().getEmail()) || caseData.getApplicant2().isApplicantOffline();

        var d10Needed = caseData.getApplicationType().isSole() && (!caseData.getApplication().isCourtServiceMethod() || app2Offline
            || caseData.isJudicialSeparationCase());

        if (d10Needed && !d10DocumentAlreadyGenerated) {
            try {
                log.info("Adding D10 to list of generated documents for case id: {}", caseId);
                generateFormHelper.addFormToGeneratedDocuments(caseData, D10, D10_DISPLAY_NAME, D10_FILENAME, D10_FILE_LOCATION);
            } catch (Exception e) {
                log.error("Error encountered whilst adding D10 document to list of generated documents for case id: {}", caseId);
            }
        }
    }
}
