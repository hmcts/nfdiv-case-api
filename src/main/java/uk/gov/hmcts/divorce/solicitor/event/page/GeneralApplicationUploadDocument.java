package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

@Slf4j
public class GeneralApplicationUploadDocument implements CcdPageConfiguration {

    private static final String GENERAL_APPLICATION_DOCUMENT_ERROR = "Please upload a document in order to continue";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationUploadDocument")
            .pageLabel("Upload document")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getGeneralApplicationDocument)
                .optional(GeneralApplication::getGeneralApplicationDocumentComments)
            .done();
    }
}
