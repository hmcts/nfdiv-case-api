package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class GeneralApplicationD11Page6 implements CcdPageConfiguration {

    private final String consentOrNotRequiredCondition;

    public static final String FEE_INFO_LABEL = """
        ## Your application will cost £60

        You have indicated that the other party consents to your application, or consent is not needed.
        Therefore, your application will cost £60.

        If the judge decides that your evidence of consent is not strong enough, or that consent is
        required from the other party, you may need to pay a higher fee. If this happens, we will contact
        you to let you know and will refund you this fee of £60.
        """;

    public GeneralApplicationD11Page6(String applicantFieldPrefix) {
        this.consentOrNotRequiredCondition =
            applicantFieldPrefix + "GenAppHearingNotRequired=\"yesPartnerAgreesWithApplication\" "
                + "OR " + applicantFieldPrefix + "GenAppHearingNotRequired=\"yesPartnerAgreesWithNoHearing\" "
                + "OR " + applicantFieldPrefix + "GenAppHearingNotRequired=\"yesDoesNotNeedConsent\"";
    }

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD11FeeInfo");

        String combinedCondition = consentOrNotRequiredCondition;

        if (isNotBlank(pageShowCondition)) {
            combinedCondition = pageShowCondition + " AND (" + consentOrNotRequiredCondition + ")";
        }
        page.showCondition(combinedCondition)
            .label("solGeneralAppD11FeeInfoLabel", FEE_INFO_LABEL);
    }
}
