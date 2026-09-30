package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

public class GeneralApplicationD11Page1 implements CcdPageConfiguration {

    private final TypedPropertyGetter<CaseData, Applicant> applicantRef;

    public GeneralApplicationD11Page1(TypedPropertyGetter<CaseData, Applicant> applicantRef) {
        this.applicantRef = applicantRef;
    }

    private static final String GENERAL_APPLICATION_D11_PARAGRAPH = """
            ## Make an application to the court (Form D11) ##

            Use this form to make a general application to the court.

            You can find more information about the types of applications you can make in the guidance on general
            applications <a href="https://www.gov.uk/copy-decree-absolute-final-order/do-not-know-which-court"
                                 target="_blank" rel="noopener noreferrer">Link to Solicitor's General Application guidance</a>

            ## Before you continue: we will share your application ##

            If your application is successful, we will share any answers and supporting evidence you provide with the
            other party.

            We will not share any contact details if you've told us to keep them private.
            """;

    public static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    public static final String PAYMENT_HEADING = "## How will payment be made?";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolGenAppD11")
                .label("LabelGeneralApplicationD11Para-1", GENERAL_APPLICATION_D11_PARAGRAPH)
            .complex(applicantRef)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatory(InterimApplicationOptions::getGeneralApplicationAcknowledgementCheckbox)
                    .label("solGeneralApplicationPaymentHeader", PAYMENT_HEADING)
                    .mandatoryWithLabel(InterimApplicationOptions::getInterimAppsPaymentMethod, PAYMENT_LABEL)
                .done()
            .done();
    }
}
