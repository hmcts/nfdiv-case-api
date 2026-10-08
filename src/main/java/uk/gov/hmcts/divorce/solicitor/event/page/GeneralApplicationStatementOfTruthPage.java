package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationStatementOfTruthPage implements CcdPageConfiguration {

    public static final String GENERAL_APPLICATION_SOT_INFO_LABEL = """
        This confirms that the information you are submitting on behalf of the applicant is true and accurate,
        to the best of your knowledge. It's known as the 'statement of truth'.
        """;

    public static final String GENERAL_APPLICATION_SOT_COMMENTS_LABEL = """
        If you have any comments you would like to make to the court staff regarding the
        application, you may include them below.
        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationStatementOfTruth")
            .complex(CaseData::getGeneralApplication)
            .readonlyNoSummary(GeneralApplication::getGeneralApplicationType)
            .mandatory(GeneralApplication::getGeneralApplicationStatementOfTruth)
            .mandatory(GeneralApplication::getGeneralApplicationSignStatementOfTruth)
            .label("sOTInfo", GENERAL_APPLICATION_SOT_INFO_LABEL)
            .mandatory(GeneralApplication::getGeneralApplicationStatementOfTruthSolsName)
            .mandatory(GeneralApplication::getGeneralApplicationStatementOfTruthSolsFirm)
            .label("sOTComments", GENERAL_APPLICATION_SOT_COMMENTS_LABEL)
            .optionalNoSummary(GeneralApplication::getGeneralApplicationStatementOfTruthComments)
            .done();
    }
}
