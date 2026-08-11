package uk.gov.hmcts.divorce.solicitor.event.page.dispense;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DispenseWithServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static uk.gov.hmcts.divorce.solicitor.event.page.dispense.DispenseWithServiceRespondentEmailAddress.LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE;

public class DispenseWithServiceAnyFriendsAndRelatives implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        addWithShowCondition(pageBuilder, ALWAYS_SHOW);
    }

    @Override
    public void addWithShowCondition(PageBuilder pageBuilder, String pageShowCondition) {
        var page = pageBuilder.page("dispenseServiceFriendsRelatives");

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
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseContactFriendsOrRelativesDetails)
                        .mandatory(DispenseWithServiceJourneyOptions::getDispenseOtherEnquiries)
                        .label("labelYouCanUploadFriendsEvidence", LABEL_CAN_UPLOAD_EVIDENCE_DISPENSE_SERVICE)
                    .done()
                .done()
            .done();
    }
}
