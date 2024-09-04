package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

public class GeneralApplicationUploadDocument implements CcdPageConfiguration {

    private static final String GENERAL_APPLICATION_DOCUMENT_ERROR = "Please upload a document in order to continue";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationUploadDocument", this::midEvent)
            .pageLabel("Upload document")
            .complex(CaseData::getGeneralApplication)
                .optional(GeneralApplication::getGeneralApplicationDocuments)
                .optional(GeneralApplication::getGeneralApplicationDocumentComments)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData caseData = details.getData();
        final GeneralApplication generalApplication = caseData.getGeneralApplication();

        if (isNull(generalApplication.getGeneralApplicationDocuments())
            || isEmpty(generalApplication.getGeneralApplicationDocuments())) {

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList(GENERAL_APPLICATION_DOCUMENT_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
