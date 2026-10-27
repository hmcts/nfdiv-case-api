package uk.gov.hmcts.divorce.solicitor.event.page.searchgov;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SearchGovConfirmPage implements CcdPageConfiguration {

    private static final String NEVER_SHOW = "[STATE]=\"NEVER_SHOW\"";

    private static final String PAYMENT_LABEL = "Choose the method of payment for the application";

    private static final String LABEL_HOW_PAYMENT = "## How will payment be made?";

    private static final String SEARCH_GOV_INFORMATION_PARAGRAPH = """
            ## Apply to ask the court to search government records (D11) ##

            You can ask the court to search a government department's records, such as HM Revenue and Customs (HMRC) or the Department
            for Work and Pensions (DWP), for the respondent's contact details.

            You will need the respondent's:
            - full name
            - date of birth

            It will help with the search if you can also provide the National Insurance number, but it is not
            mandatory.

            Government record searches are typically completed within 6-8 weeks. If the search is successful, the respondent's contact
            details will only be shared with the court and not with you. The court will send ${labelContentUnionType} papers to the
            respondent.
            """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("searchGovPayment");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getLabelContent)
            .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
            .done()
            .label("LabelSearchGovInfoParagraph", SEARCH_GOV_INFORMATION_PARAGRAPH)
            .label("LabelSearchGovPaymentHeading", LABEL_HOW_PAYMENT)
            .complex(CaseData::getApplicant1)
            .complex(Applicant::getInterimApplicationOptions)
            .mandatoryWithLabel(InterimApplicationOptions::getInterimAppsPaymentMethod, PAYMENT_LABEL)
            .done()
            .done();
    }
}
