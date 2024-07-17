package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;

public class SolStatementOfTruth implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant1StatementOfTruth=\"ALWAYS_HIDE\"";
    private static final String DIVORCE_APPLICATION = "divorceOrDissolution = \"divorce\"";
    private static final String DISSOLUTION_APPLICATION = "divorceOrDissolution = \"dissolution\"";
    private static final String PERSONAL_SERVICE_ERROR =
        "Solicitors cannot select personal service. Select court service or solicitor service before proceeding.";
    private static final String COURT_SERVICE_ERROR_INT_ADDRESS =
        "You cannot select Court Service because the Respondent has an international address. "
            + "Please select Solicitor Service.";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolStatementOfTruth", this::midEvent)
            .pageLabel("Statement of truth and reconciliation")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .readonlyNoSummary(CaseData::getDivorceOrDissolution, ALWAYS_HIDE)
            .complex(CaseData::getApplication)
                .mandatory(Application::getSolUrgentCase)
                .optional(Application::getSolUrgentCaseSupportingInformation, "solUrgentCase=\"Yes\"")
                .done()
            .complex(CaseData::getApplicant1)
                .readonlyNoSummary(Applicant::getFinancialOrder, ALWAYS_HIDE)
                .done()
            .label("LabelSolServiceMethod", "## Service method", "applicationType=\"soleApplication\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getServiceMethod, "applicationType=\"soleApplication\"")
                .label("LabelSolicitorService",
                "After service is complete you must notify the court by completing the ‘Confirm Service’ event in CCD. "
                    + "Refer to the notification that will be sent upon the issuing of the the case",
                "serviceMethod=\"solicitorService\" AND applicationType=\"soleApplication\"")
                .label("LabelSolStatementOTruthPara-3", "## Statement of reconciliation")
                .mandatory(Application::getSolStatementOfReconciliationCertify)
                .mandatory(Application::getSolStatementOfReconciliationDiscussed)
            .done()
            .label("LabelPrayer", "## The prayer ##")
            .complex(CaseData::getApplicant1)
                    .complex(Applicant::getApplicantPrayer)
                    .mandatory(ApplicantPrayer::getPrayerDissolveDivorce, DIVORCE_APPLICATION)
                    .mandatory(ApplicantPrayer::getPrayerEndCivilPartnership, DISSOLUTION_APPLICATION)
                    .optional(ApplicantPrayer::getPrayerFinancialOrdersThemselves)
                    .optional(ApplicantPrayer::getPrayerFinancialOrdersChild)
                    .done()
            .done()
            .label("LabelSolStatementOfTruth-SOT", "## Statement of truth ##")
            .complex(CaseData::getApplication)
                .mandatory(Application::getApplicant1StatementOfTruth)
                .mandatory(Application::getSolSignStatementOfTruth)
                .label("LabelSolStatementOfTruth-SOTInfo",
                    "This confirms that the information you are submitting on behalf of the applicant is true and accurate, "
                        + "to the best of your knowledge. It’s known as the ‘statement of truth’. ")
                .label("LabelSolStatementOTruth-Statement",
                    "**Proceedings for contempt of court may be brought against anyone who makes, or causes to be made, "
                        + "a false statement verified by a statement of truth without an honest belief in its truth.**")
                .mandatory(Application::getSolStatementOfReconciliationName)
                .mandatory(Application::getSolStatementOfReconciliationFirm)
                .label("LabelSolStatementOfTruth-Comments",
                    "If you have any comments you would like to make to the court staff regarding the application you "
                        + "may include them below.")
                .optionalNoSummary(Application::getStatementOfReconciliationComments)
                .readonlyNoSummary(Application::getSolApplicationFeeInPounds, ALWAYS_HIDE)
                .label("LabelSolStatementOfTruth-ApplicationFee",
                    "**Solicitor application fee:**  \n**£${solApplicationFeeInPounds}**")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();

        if (PERSONAL_SERVICE.equals(data.getApplication().getServiceMethod())) {
            errors.add(PERSONAL_SERVICE_ERROR);
        }

        if (COURT_SERVICE.equals(data.getApplication().getServiceMethod())) {
            if (data.getApplicationType().isSole()
                && data.getApplicant2().getAddressOverseas() == YesOrNo.YES) {
                errors.add(COURT_SERVICE_ERROR_INT_ADDRESS);
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
