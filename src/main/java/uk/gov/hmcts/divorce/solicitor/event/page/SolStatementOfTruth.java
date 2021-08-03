package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolStatementOfTruth implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant1StatementOfTruth=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolStatementOfTruth")
            .pageLabel("Statement of truth and reconciliation")
            .readonlyNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .label(
                "LabelNFDBanner-SolStatementOfTruth",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-SolStatementOfTruth",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label("LabelSolStatementOfTruthPara-1", "## The applicant is applying to the court")
            .label(
                "LabelSolStatementOfTruthPara-1.1",
                "• That the marriage be dissolved as it has broken down irretrievably",
                "applicant1FinancialOrder=\"No\"")
            .label(
                "LabelSolStatementOfTruthPara-1.2",
                "- That the marriage be dissolved as it has broken down irretrievably\n"
                    + "- That a costs order may be granted.",
                "applicant1FinancialOrder=\"No\"")
            .label(
                "LabelSolStatementOfTruthPara-1.3",
                "- That the marriage be dissolved as it has broken down irretrievably\n"
                    + "- That a financial order may be granted.",
                "applicant1FinancialOrder=\"Yes\"")
            .label(
                "LabelSolStatementOfTruthPara-1.4",
                "- That the marriage be dissolved as it has broken down irretrievably.\n"
                    + "- That a costs order may be granted.\n"
                    + "- That a financial order may be granted.",
                "applicant1FinancialOrder=\"Yes\"")
            .complex(CaseData::getApplication)
                .mandatory(Application::getSolUrgentCase)
                .optional(Application::getSolUrgentCaseSupportingInformation, "solUrgentCase=\"Yes\"")
                .done()
            .complex(CaseData::getApplicant1)
                .readonlyNoSummary(Applicant::getFinancialOrder, ALWAYS_HIDE)
                .done()
            .label("LabelSolServiceMethod", "## Service method")
            .complex(CaseData::getApplication)
                .mandatory(Application::getSolServiceMethod)
                .label(
                "LabelSolPersonalService",
                "After service is complete you must notify the court by completing the 'Confirm Service' form "
                    + "in CCD. Refer to the information pack for further instruction on how to do this",
                "solServiceMethod=\"personalService\"")
                .label("LabelSolStatementOTruthPara-3", "## Statement of reconciliation")
                .mandatory(Application::getSolStatementOfReconciliationCertify)
                .mandatory(Application::getSolStatementOfReconciliationDiscussed)
                .label("LabelSolStatementOfTruthPara-2", "## Statement of truth")
                .mandatory(Application::getApplicant1StatementOfTruth)
                .mandatory(Application::getSolSignStatementOfTruth)
                .label("LabelPrayer", "## Prayer")
                .mandatory(Application::getApplicant1PrayerHasBeenGiven)
                .mandatory(Application::getSolStatementOfReconciliationName)
                .mandatory(Application::getSolStatementOfReconciliationFirm)
                .label(
                    "LabelSolStatementOTruthPara-2",
                    "You could be fined or imprisoned for contempt of court if you deliberately submit false information.\n\n"
                        + "If you have any comments you would like to make to the court staff regarding the application "
                        + "you may include them below.")
                .optionalNoSummary(Application::getStatementOfReconciliationComments)
                .readonlyNoSummary(Application::getSolApplicationFeeInPounds)
            .done();
    }
}
