package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class UploadMarriageCertificate implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("UploadSupportingDocuments")
            .pageLabel("Upload the marriage certificate")
            .label(
                "LabelUploadDocumentsPara-1",
                "You need to upload a digital photo or scan of the marriage certificate.\r\n"
                    + "You can also upload other documents that you need to send to the court, e.g.\r\n"
                    + "• Certified translation of a non-English marriage certificate\r\n"
                    + "• Change of name deed\r\n"
                    + "The image must be of the entire document and has to be readable by court staff. "
                    + "You can upload image files with jpg, jpeg, bmp, tif, tiff or PDF file extensions, maximum size 100MB per file")
            .optional(CaseData::getDocumentsUploaded);
    }
}
