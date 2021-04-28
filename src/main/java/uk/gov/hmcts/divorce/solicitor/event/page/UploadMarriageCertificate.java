package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class UploadMarriageCertificate implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("UploadMarriageCertificate")
            .pageLabel("Upload the marriage certificate - Apply for a divorce")
            .label(
                "LabelSolAboutEditingApplication-UploadMarriageCertificate",
                "You can make changes at the end of your application.")
            .label(
                "UploadPhotoOrScanMarriageCertificate-UploadMarriageCertificate",
                "You need to upload a digital photo or scan of the marriage certificate.")
            .label(
                "UploadDocumentsToSendToCourt-UploadMarriageCertificate",
                "You can also upload other documents that you need to send to the court, e.g.")
            .label(
                "CertifiedTranslation-UploadMarriageCertificate",
                "• Certified translation of a non-English marriage certificate")
            .label(
                "ChangeOfNameDeed-UploadMarriageCertificate",
                "• Change of name deed")
            .label(
                "DocumentTypes-UploadMarriageCertificate",
                "The image must be of the entire document and has to be readable by court staff."
                    + " You can upload jpg, bmp, tif, or PDF files (max file size 10MB per file)")
            .optional(CaseData::getDocumentsUploaded);
    }
}
