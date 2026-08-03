package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class AlternativeServiceConfirmPage implements CcdPageConfiguration {

    private static final String SERVICE_CONFIRM_PARAGRAPH = """
        ## Apply for alternative service (D11) ##


        ## Acceptance that information will be shared ##

        If the application is successful, we will share the answers and any evidence you provide with the
        respondent.

        We will not share the applicant’s contact details if you've told us to keep them private

        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("alternativeServiceConfirm")
            .pageLabel("Alternative Service App")
            .label("LabelAlternativeServiceConfirmPara-1", SERVICE_CONFIRM_PARAGRAPH, "")
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .mandatoryNoSummary(InterimApplicationOptions::getAgreeToShareDetailsWithRespondentCheckbox);
    }
}
