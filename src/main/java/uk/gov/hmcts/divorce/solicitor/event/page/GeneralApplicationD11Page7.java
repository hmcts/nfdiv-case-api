package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("SolGenAppD11OtherFeeInfo");

        String combinedCondition = noHearingCondition;

        if (isNotBlank(pageShowCondition)) {
            combinedCondition = pageShowCondition + " AND (" + noHearingCondition + ")";
        }
        page.showCondition(combinedCondition)
            .label("solGeneralAppD11OtherFeeInfoLabel", FEE_INFO_LABEL);
    }
}
