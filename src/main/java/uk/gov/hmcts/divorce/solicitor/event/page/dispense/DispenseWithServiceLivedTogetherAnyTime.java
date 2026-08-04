package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceLivedTogetherAnyTime implements CcdPageConfiguration {

    private static final String LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION = "applicant1DispenseLiveTogether=\"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceLivedTogether")
                    .pageLabel("Dispense with service app");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page.complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLiveTogether)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLivedTogetherDate,
                                LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLivedTogetherAddress,
                                LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseLivedTogetherAddressOverseas,
                                LIVED_TOGETHER_ANY_TIME_SHOW_CONDITION)
                    .done()
                .done()
            .done();
    }
}
