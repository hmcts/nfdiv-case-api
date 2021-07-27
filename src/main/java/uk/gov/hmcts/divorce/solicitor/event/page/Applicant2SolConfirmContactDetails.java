package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;


public class Applicant2SolConfirmContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolConfirmContactDetails")
            .pageLabel("Confirm contact details")
            .complex(CaseData::getApplicant2)
            .complex(Applicant::getSolicitor)
            .mandatoryNoSummary(Solicitor::getName, null, "${labelContentApplicant2UC}'s solicitor's name")
            .optionalNoSummary(Solicitor::getPhone, null, "${labelContentApplicant2UC}'s solicitor's Phone number")
            .label("LabelRespSol-EmailHeader", "### Email contact details")
            .label("LabelRespSol-Email", "Email address will be used to send case updates.")
            .mandatoryNoSummary(Solicitor::getEmail, null, "${labelContentApplicant2UC}'s solicitor's Email")
            .mandatoryNoSummary(Solicitor::getAgreeToReceiveEmails)
            .done();
    }
}
