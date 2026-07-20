package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

public class DeemedServiceConfirmPage implements CcdPageConfiguration {

    private final String pageShowCondition;

    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private static final String SERVICE_CONFIRM_PARAGRAPH = """
            ## Apply for deemed service (D11) ##

            If you have evidence that the respondent or their legal representative has received the
            ${labelContentDivorceOrCivilPartnership} papers, you can apply for deemed service.

            ## Acceptance that information will be shared ##

            If the application is successful, we will share the answers and any evidence you provide with the
            respondent.

            We will not share the applicant’s contact details if you've told us to keep them private
            """;

    public DeemedServiceConfirmPage() {
        this(null);
    }

    public DeemedServiceConfirmPage(String pageShowCondition) {
        this.pageShowCondition = pageShowCondition;
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        var page = pageBuilder.page("deemedServiceConfirm")
                    .pageLabel("Deemed Service App");

        if (pageShowCondition != null) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnership, NEVER_SHOW)
            .done()
            .label("LabelDeemedServiceConfirmPara-1", SERVICE_CONFIRM_PARAGRAPH)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatoryNoSummary(InterimApplicationOptions::getAgreeToShareDetailsWithRespondentCheckbox)
                .done()
            .done();
    }
}
