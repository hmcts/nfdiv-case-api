package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class DispenseWithServiceAddressAfterParting implements CcdPageConfiguration {

    private static final String LIVED_AFTER_PARTING_SHOW_CONDITION = "applicant1DispenseAwarePartnerLived=\"Yes\"";
    private static final String LABEL_WHERE_DID_THEY_LIVE_AFTER_PARTING = """
        ### Where did the respondent live after they parted with the applicant? ###
        Include all addresses you know of that the respondent has lived at since they parted
        """;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceAfterParting");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseAwarePartnerLived)
                        .label("labelWhereDidTheyLiveAfterParting", LABEL_WHERE_DID_THEY_LIVE_AFTER_PARTING,
                            LIVED_AFTER_PARTING_SHOW_CONDITION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerPastAddress1, LIVED_AFTER_PARTING_SHOW_CONDITION)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerPastAddressEnquiries1,
                            LIVED_AFTER_PARTING_SHOW_CONDITION)
                        .optional(DispenseWithServiceJourneyOptions::getDispensePartnerPastAddress2, LIVED_AFTER_PARTING_SHOW_CONDITION)
                        .optional(DispenseWithServiceJourneyOptions::getDispensePartnerPastAddressEnquiries2,
                            LIVED_AFTER_PARTING_SHOW_CONDITION)
                    .done()
                .done()
            .done();
    }
}
