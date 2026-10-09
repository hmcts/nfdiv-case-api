package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.common.ccd.PageBuilder.andShowCondition;

public class DispenseWithServiceAnyChildrenAndContact implements CcdPageConfiguration {

    private static final String CHILDREN_IN_FAMILY_YES = "applicant1DispenseChildrenOfFamily=\"Yes\"";

    private static final String RESPONDENT_CONTACT_CHILDREN_YES = "applicant1DispensePartnerContactWithChildren=\"Yes\"";
    private static final String RESPONDENT_CONTACT_CHILDREN_NO = "applicant1DispensePartnerContactWithChildren=\"No\"";

    private static final String CHILD_MAINTENANCE_ORDER_SHOW_CONDITION = "applicant1DispenseChildMaintenanceOrder=\"Yes\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceChildrenContact");

        if (isNotBlank(pageShowCondition)) {
            page.showCondition(pageShowCondition);
        }
        page
            .complex(CaseData::getLabelContent)
            .readonlyNoSummary(LabelContent::getUnionType, NEVER_SHOW)
            .done()
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getInterimApplicationOptions)
                    .complex(InterimApplicationOptions::getDispenseWithServiceJourneyOptions)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseChildrenOfFamily)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerContactWithChildren,
                            CHILDREN_IN_FAMILY_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseHowPartnerContactChildren,
                            andShowCondition(RESPONDENT_CONTACT_CHILDREN_YES, CHILDREN_IN_FAMILY_YES))
                        .mandatory(DispenseWithServiceJourneyOptions::getDispensePartnerLastContactChildren,
                            andShowCondition(RESPONDENT_CONTACT_CHILDREN_NO, CHILDREN_IN_FAMILY_YES))
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseChildMaintenanceOrder, CHILDREN_IN_FAMILY_YES)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseChildMaintenanceResults,
                            andShowCondition(CHILD_MAINTENANCE_ORDER_SHOW_CONDITION, CHILDREN_IN_FAMILY_YES))
                    .done()
                .done()
            .done();
    }
}
