package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServiceUploadPhotoPage implements CcdPageConfiguration {

    private static final String RESPONDENT_PHOTO_LABEL = "Are you able to upload a recent photo of the respondent?";

    private static final String RESPONDENT_PHOTO = "applicant1InterimAppsCanUploadEvidence = \"Yes\"";

    private static final String RESPONDENT_PHOTO_UPLOAD_LABEL = """
        ## Upload your documents
        Upload a recent picture of the respondent\n
        Make sure your picture:\n
        • clearly shows the respondent’s face\n
        • does not include any other people, to avoid confusion\n
        • does not include any children\n
        Any blurred images, or images that show children or other people cannot be accepted by the court.\n
        The file must be in jpg, bmp, tiff or png format.
        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("bailiffServiceUploadPhotoPage")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatory(
                        InterimApplicationOptions::getInterimAppsCanUploadEvidence, ALWAYS_SHOW, NO_DEFAULT_VALUE, RESPONDENT_PHOTO_LABEL
                    )
                    .label("uploadPhotoLabel",  RESPONDENT_PHOTO_UPLOAD_LABEL, RESPONDENT_PHOTO)
                    .optional(InterimApplicationOptions::getInterimAppsEvidenceDocs, RESPONDENT_PHOTO)
                .done()
            .done()
            .done();
    }
}
