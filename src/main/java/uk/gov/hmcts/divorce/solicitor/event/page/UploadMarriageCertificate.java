package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class UploadMarriageCertificate implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("UploadSupportingDocuments")
            .pageLabel("Upload the marriage certificate")
            .label(
                "LabelNFDBanner-UploadSupportingDocuments",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-UploadSupportingDocuments",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label(
                "LabelUploadDocumentsPara-1",
                "You need to upload a digital photo or scan of the marriage certificate.\n\n"
                    + "You can also upload other documents that you need to send to the court, e.g.\n"
                    + "- Certified translation of a non-English marriage certificate\n"
                    + "- Change of name deed\n\n"
                    + "The image must be of the entire document and has to be readable by court staff. "
                    + "You can upload image files with jpg, jpeg, bmp, tif, tiff or PDF file extensions, maximum size 100MB per file")
            .optional(CaseData::getDocumentsUploaded);
    }
}
