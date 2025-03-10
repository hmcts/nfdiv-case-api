package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;

public class GeneralApplicationSelectApplicationType implements CcdPageConfiguration {

    private static final String GENERAL_APPLICATION_SOL_GUIDE = "Refer to the <a href=\"https://www.gov.uk/government/publications/myhmcts"
            + "-how-to-make-follow-up-applications-for-a-divorce-or-dissolution/general-applications-alternative-service-and-deemed-"
            + "and-dispensed target=\"_blank\" rel=\"noopener noreferrer\">Solicitor Guidance</a>:";

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("generalApplicationSelectType")
            .pageLabel("Select Application Type")
            .complex(CaseData::getGeneralApplication)
                .mandatory(GeneralApplication::getGeneralApplicationType)
                .mandatory(GeneralApplication::getGeneralApplicationTypeOtherComments,
                    "generalApplicationType=\"other\"")
                .mandatory(GeneralApplication::getGeneralApplicationUrgentCase)
                .mandatory(GeneralApplication::getGeneralApplicationUrgentCaseReason, "generalApplicationUrgentCase=\"Yes\"")
                .done()
            .label("generalApplicationSolGuide", GENERAL_APPLICATION_SOL_GUIDE);
    }
}
