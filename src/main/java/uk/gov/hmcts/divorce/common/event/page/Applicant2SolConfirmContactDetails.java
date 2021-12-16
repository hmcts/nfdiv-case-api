package uk.gov.hmcts.divorce.common.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;


public class Applicant2SolConfirmContactDetails implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant2SolicitorName=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolConfirmContactDetails")
            .pageLabel("Confirm contact details")
            .complex(CaseData::getLabelContent)
                .readonlyNoSummary(LabelContent::getApplicant2, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getApplicant2UC, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getTheApplicant2, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getTheApplicant2UC, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnershipApplication, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getMarriageOrCivilPartnership, ALWAYS_HIDE)
                .readonlyNoSummary(LabelContent::getDivorceOrCivilPartnership, ALWAYS_HIDE)
            .done()
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .mandatoryNoSummary(Solicitor::getName, null, "${labelContentApplicant2UC}'s solicitor's name")
                    .optionalNoSummary(Solicitor::getPhone, null, "${labelContentApplicant2UC}'s solicitor's Phone number")
                    .label("LabelRespSol-EmailHeader", "### Email contact details")
                    .label("LabelRespSol-Email", "Email address will be used to send case updates.")
                    .mandatoryNoSummary(Solicitor::getEmail, null, "${labelContentApplicant2UC}'s solicitor's Email")
                    .mandatoryNoSummary(Solicitor::getAgreeToReceiveEmailsCheckbox)
                    .done()
                .done();
    }
}
