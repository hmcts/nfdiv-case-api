package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

public class BailiffServicePaymentMethodPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "applicant1InterimAppsPaymentMethod = \"NEVER_SHOW\"";

    private static final String BAILIFF_SERVICE_LABEL = """
            ## Request bailiff service (D89)

            Request to have the papers service on the respondent by a county court bailiff.

            Court bailiffs can only serve documents to an address in England or Wales where postal delivery has
            already been tried.

            If the papers are successfully delivered, the bailiff will complete a certificate of service and send it to the
            court. The ${labelContentDivorceOrCivilPartnershipApplication} will then proceed whether or not the respondent responds.

            We will ask you some questions about the respondent to help the bailiff identify them. It will be helpful if
            you are able to provide a photo.
            """;

    public static final String PAYMENT_HEADING = "## How will payment be made?";

    public static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("bailiffServicePayment")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnershipApplication, NEVER_SHOW)
            .done()
            .label("bailiffServiceLabel", BAILIFF_SERVICE_LABEL)
            .label("paymentLabel", PAYMENT_HEADING)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatory(InterimApplicationOptions::getInterimAppsPaymentMethod, ALWAYS_SHOW, NO_DEFAULT_VALUE, PAYMENT_LABEL)
                .done()
            .done();
    }
}
