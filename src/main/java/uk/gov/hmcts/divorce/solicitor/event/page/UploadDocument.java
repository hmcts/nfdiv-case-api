package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;

public class UploadDocument implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("UploadSupportingDocuments")
            .pageLabel("Upload your documents")
            .label(
                "LabelUploadDocumentsPara-1",
                "Upload a scan of the original ${labelContentMarriageOrCivilPartnership} certificate, "
                    + "or a certified copy or translation.\n\n"
                    + "You can also upload any other evidence or applications, if required. "
                    + "For example a deed poll to show a change of name, or an application for ‘dispensed with’ service.\n\n"
                    + "If you have indicated that there is a difference in the applicant's or respondent's names and how it is"
                    + " written on the ${labelContentMarriageOrCivilPartnership} certificate then upload some evidence like a"
                    + " government issued ID, a passport, driving licence or birth certificate, deed poll.\n\n"
                    + "Make sure the image you upload shows the entire document and all the text is legible.")
            .complex(CaseData::getDocuments)
                .optional(CaseDocuments::getApplicant1DocumentsUploaded)
                .done();
    }
}
