package uk.gov.hmcts.divorce.common.event.page;

import org.apache.commons.validator.routines.EmailValidator;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LabelContent;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;


public class Applicant2SolConfirmContactDetails implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant2SolicitorName=\"ALWAYS_HIDE\"";
    private static final String BLANK_LABEL = "";
    private static final String INVALID_EMAIL_ERROR = "You have entered an invalid solicitor email address. "
        + "Please check the email and enter it again, before submitting the application.";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2SolConfirmContactDetails", this::midEvent)
            .pageLabel("Confirm solicitor contact details")
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
                    .optionalNoSummary(Solicitor::getPhone, null, "${labelContentApplicant2UC}'s solicitor's phone number")
                    .label("LabelRespSol-EmailHeader", "### Email updates and service")
                    .label("LabelRespSol-Email", "Updates on the case will be sent to this email address")
                    .mandatoryNoSummary(Solicitor::getEmail, null, "${labelContentApplicant2UC}'s solicitor's email")
                    .mandatoryNoSummary(Solicitor::getAgreeToReceiveEmailsCheckbox, null, BLANK_LABEL)
                    .done()
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        Solicitor applicant2Solicitor = caseData.getApplicant2().getSolicitor();

        boolean validEmail = EmailValidator.getInstance().isValid(applicant2Solicitor.getEmail());
        if (!validEmail) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(List.of(INVALID_EMAIL_ERROR))
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .errors(null)
            .build();
    }
}
