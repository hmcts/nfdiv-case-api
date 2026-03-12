package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralReferral;

public class GeneralReferralDocuments implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("generalReferralDocs")
            .showCondition("generalReferralReason=\"generalApplicationReferral\"")
            .pageLabel("General Referral")
            .complex(CaseData::getGeneralReferral)
                .optional(GeneralReferral::getGeneralReferralDocuments)
            .done();
    }
}
