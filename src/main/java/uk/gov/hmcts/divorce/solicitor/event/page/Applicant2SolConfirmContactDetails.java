package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;


public class Applicant2SolConfirmContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolConfirmContactDetails")
            .pageLabel("Confirm contact details")
            .complex(CaseData::getApplicant2)
            .complex(Applicant::getSolicitor)
            .mandatoryNoSummary(Solicitor::getName, null, "Respondent's solicitor's name")
            .optionalNoSummary(Solicitor::getPhone, null , "Respondent's solicitor's Phone number")
            .label("LabelRespSol-EmailHeader", "### Email contact details")
            .label("LabelRespSol-Email", "Email address will be used to send case updates.")
            .mandatoryNoSummary(Solicitor::getEmail, null, "Respondent's solicitor's Email")
            .mandatoryNoSummary(Solicitor::getAgreeToReceiveEmails)
            .done();
    }
}
