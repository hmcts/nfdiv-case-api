package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceConfirmPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    private static final String LABEL_HOW_PAYMENT = "## How will payment be made?";

    private static final String SERVICE_INFORMATION_PARAGRAPH = """
            ## Apply to dispense with service (D13b) ##

            To dispense with service means progressing the ${labelContentUnionType} without serving
            the papers on the respondent. In most cases, the ${labelContentUnionType} cannot be completed without the
            respondent's knowledge. Therefore, dispensing with service is considered by the court to be a last resort.

            ## What you need to do ##

            You'll need to prove to the court that you have made every reasonable attempt to find the respondent
            or send papers to them, without success, including:
            - trying to contact them by any known email addresses, telephone numbers or social media accounts
            - asking any friends, children or other relatives of the respondent that you are able to contact
            - trying to find them using a tracing agent or a people tracing service
            - searching for them online, or using online people finder services
            - applying to the court to search government records to find the respondent's current address if you
            think they're still in the UK
            - if known, asking their employer to deliver the documents to the respondent on your behalf

            If you know that the respondent is unaware of the applicant's whereabouts, you may need to request a
            <a href="https://www.gov.uk/copy-decree-absolute-final-order/do-not-know-which-court"
            target="_blank" rel="noopener noreferrer">search for a divorce decree absolute or a final order</a> from
            the Central Family Court. This is to make sure they have not already divorced the applicant.
            You will need to search from the date the applicant and respondent last had contact.

            If you cannot show that you have tried everything you reasonably can to send the ${labelContentUnionType}
            papers to the respondent, it is likely that your application will be rejected.

            """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServicePayment");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
            .done()
            .label("LabelDispenseServicePaymentParagraph", SERVICE_INFORMATION_PARAGRAPH)
            .label("LabelDispenseServicePaymentHeading", LABEL_HOW_PAYMENT)
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .mandatoryWithLabel(InterimApplicationOptions::getInterimAppsPaymentMethod, PAYMENT_LABEL)
                .done()
            .done();
    }
}
