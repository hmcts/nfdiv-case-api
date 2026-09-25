package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class GeneralApplicationD11Page6 implements CcdPageConfiguration {

    private final String consentOrNotRequiredCondition;

    public GeneralApplicationD11Page6(String applicantFieldPrefix) {
        this.consentOrNotRequiredCondition =
            applicantFieldPrefix + "GenAppHearingNotRequired=\"yesPartnerAgreesWithApplication\" "
                + "OR " + applicantFieldPrefix + "GenAppHearingNotRequired=\"yesPartnerAgreesWithNoHearing\" "
                + "OR " + applicantFieldPrefix + "GenAppHearingNotRequired=\"yesDoesNotNeedConsent\"";
    }

    public static final String FEE_INFO_LABEL = """
        ## Your application will cost £60

        You have indicated that the other party consents to your application, or consent is not needed.
        Therefore, your application will cost £60.

        If the judge decides that your evidence of consent is not strong enough, or that consent is
        required from the other party, you may need to pay a higher fee. If this happens, we will contact
        you to let you know and will refund you this fee of £60.
        """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("SolGenAppD11FeeInfo")
            .showCondition(consentOrNotRequiredCondition)
            .label("solGeneralAppD11FeeInfoLabel", FEE_INFO_LABEL);
    }
}
