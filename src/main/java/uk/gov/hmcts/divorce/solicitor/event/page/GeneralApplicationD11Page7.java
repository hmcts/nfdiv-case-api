package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class GeneralApplicationD11Page7 implements CcdPageConfiguration {

    private final String noHearingCondition;

    public GeneralApplicationD11Page7(String applicantFieldPrefix) {
        this.noHearingCondition =
            applicantFieldPrefix + "GenAppHearingNotRequired=\"no\"";
    }

    public static final String FEE_INFO_LABEL = """
        ## Your application will cost £190
        Since the other party does not consent to your application, the application will cost £190.
        """;

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("SolGenAppD11OtherFeeInfo")
            .showCondition(noHearingCondition)
            .label("solGeneralAppD11OtherFeeInfoLabel", FEE_INFO_LABEL);
    }
}
