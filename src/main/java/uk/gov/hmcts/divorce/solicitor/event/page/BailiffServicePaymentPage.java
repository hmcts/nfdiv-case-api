package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class BailiffServicePaymentPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "applicant1InterimAppsPaymentMethod = \"NEVER_SHOW\"";

    private static final String BAILIFF_SERVICE_LABEL = """
            ## Request bailiff service (D89)

            Request to have the papers service on the respondent by a county court bailiff.

            Court bailiffs can only serve documents to an address in England or Wales where postal delivery has
            already been tried.

            If the papers are successfully delivered, the bailiff will complete a certificate of service and send it to the
            court. The divorce will then proceed whether or not the respondent responds.

            We will ask you some questions about the respondent to help the bailiff identify them. It will be helpful if
            you are able to provide a photo.

            There is a fee of ${applicant1BailiffServiceFeeAmount} to apply for bailiff service.
            """;

    public static final String PAYMENT_HEADING = "## How will payment be made?";

    public static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("bailiffServicePayment")
            .pageLabel("Bailiff Service App")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getBailiffServiceJourneyOptions)
                        .readonly(BailiffServiceJourneyOptions::getBailiffServiceFeeAmount, NEVER_SHOW)
                        .label("bailiffServiceLabel", BAILIFF_SERVICE_LABEL)
                    .done()
                .done()
            .done()
            .label("paymentLabel", PAYMENT_HEADING)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                .mandatoryWithLabel(InterimApplicationOptions::getInterimAppsPaymentMethod, PAYMENT_LABEL)
            .done()
            .done();
    }
}
