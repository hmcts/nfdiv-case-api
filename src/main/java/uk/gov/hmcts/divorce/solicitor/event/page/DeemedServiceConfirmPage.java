package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DeemedServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class DeemedServiceConfirmPage implements CcdPageConfiguration {

    public static final String SERVICE_CONFIRM_PARA = "## Apply for deemed service (D11) ## \n\n"
        + "If you have evidence that the respondent or their legal representative has received the "
        + "${labelContentDivorceOrCivilPartnership} papers, you can apply for deemed service.\n\n"
        + "## Acceptance that information will be shared ##\n\n"
        + "If the application is successful, we will share the answers and any evidence you provide with the \n"
        + "respondent.\n\n"
        + "We will not share the applicant’s contact details if you've told us to keep them private";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("deemedServiceConfirm")
            .pageLabel("Deemed Service App")
            .label("LabelDeemedServiceConfirmPara-1", SERVICE_CONFIRM_PARA)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDeemedServiceJourneyOptions)
                        .mandatoryNoSummary(DeemedServiceJourneyOptions::getAgreeToShareDetailsWithRespondentCheckbox)
                    .done()
                .done()
            .done();
    }
}
