package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GeneralApplicationD8bPage1 implements CcdPageConfiguration {

    private static final String GENERAL_APPLICATION_D8B_PARAGRAPH = """
            ## Form D8b Answer to a divorce, dissolution, (judicial) separation application ##

            Use this event to submit the Answer to a divorce, dissolution or judicial separation application.

            This event can only be used by the respondent when the Acknowledgement of Service (Aos) has been submitted, and they have
             indicated that they want to dispute the application. The form D8b should be submitted within 21 days from the date of
              submission of the AoS.

            You will need to make an application to the court using Form D11 if you are submitting the answers out of time.

            The fee for the Form D8b Answer to a divorce, dissolution, (judicial) separation application is £245.

            """;

    public static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    public static final String PAYMENT_HEADING = "## How will payment be made?";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD8b");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }

        page.label("LabelGeneralApplicationD8bPara-1", GENERAL_APPLICATION_D8B_PARAGRAPH)
            .complex(CaseData::getApplicant2)
            .complex(Applicant::getInterimApplicationOptions)
            .label("solGeneralApplicationD8bPaymentHeader", PAYMENT_HEADING)
            .mandatoryWithLabel(InterimApplicationOptions::getInterimAppsPaymentMethod, PAYMENT_LABEL)
            .done()
            .done();
    }
}
