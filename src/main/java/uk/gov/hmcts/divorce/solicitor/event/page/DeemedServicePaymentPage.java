package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class DeemedServicePaymentPage implements CcdPageConfiguration {

    private final String pageShowCondition;

    public static final String PAYMENT_HEADING = "## How will payment be made?";

    public static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    public DeemedServicePaymentPage() {
        this(null);
    }

    public DeemedServicePaymentPage(String pageShowCondition) {
        this.pageShowCondition = pageShowCondition;
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        var page = pageBuilder.page("deemedServicePayment")
            .pageLabel("Deemed Service App");
        if (pageShowCondition != null) {
            page.showCondition(pageShowCondition);
        }
        page.label("paymentLabel", PAYMENT_HEADING)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                        .mandatoryWithLabel(InterimApplicationOptions::getInterimAppsPaymentMethod, PAYMENT_LABEL)
                .done()
            .done();
    }
}
