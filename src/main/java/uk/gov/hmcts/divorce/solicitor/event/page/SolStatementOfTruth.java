package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class SolStatementOfTruth implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "statementOfTruth=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolStatementOfTruth")
            .pageLabel("Statement of truth and reconciliation")
            .mandatoryNoSummary(CaseData::getApplicationType, ALWAYS_HIDE)
            .label(
                "LabelNFDBanner-SolStatementOfTruth",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-SolStatementOfTruth",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label("LabelSolStatementOfTruthPara-1", "## Applicant 1 is applying to the court")
            .label(
                "LabelSolStatementOfTruthPara-1.1",
                "• That the marriage be dissolved as it has broken down irretrievably",
                "divorceCostsClaim=\"No\" AND financialOrder=\"No\"")
            .label(
                "LabelSolStatementOfTruthPara-1.2",
                    "• That the marriage be dissolved as it has broken down irretrievably<br />"
                        + "• That a costs order may be granted.",
                    "divorceCostsClaim=\"Yes\" AND financialOrder=\"No\"")
            .label(
                "LabelSolStatementOfTruthPara-1.3",
                "• That the marriage be dissolved as it has broken down irretrievably<br />"
                    + "• That a financial order may be granted.",
                    "divorceCostsClaim=\"No\" AND financialOrder=\"Yes\"")
            .label(
                "LabelSolStatementOfTruthPara-1.4",
                "• That the marriage be dissolved as it has broken down irretrievably.<br />"
                    + "• That a costs order may be granted.<br />"
                    + "• That a financial order may be granted.",
                    "divorceCostsClaim=\"Yes\" AND financialOrder=\"Yes\"")
            .mandatory(CaseData::getSolUrgentCase)
            .optional(CaseData::getSolUrgentCaseSupportingInformation, "solUrgentCase=\"Yes\"")
            .mandatoryNoSummary(CaseData::getDivorceCostsClaim, ALWAYS_HIDE)
            .mandatoryNoSummary(CaseData::getFinancialOrder, ALWAYS_HIDE)
            .label("LabelSolServiceMethod", "## Service method")
            .mandatory(CaseData::getSolServiceMethod)
            .label(
                "LabelSolPersonalService",
                "After service is complete you must notify the court by completing the 'Confirm Service' form "
                    + "in CCD. Refer to the information pack for further instruction on how to do this",
                "solServiceMethod=\"personalService\"")
            .label("LabelSolStatementOTruthPara-3", "## Statement of reconciliation")
            .mandatory(CaseData::getSolStatementOfReconciliationCertify)
            .mandatory(CaseData::getSolStatementOfReconciliationDiscussed)
            .label("LabelSolStatementOfTruthPara-2", "## Statement of truth")
            .mandatory(CaseData::getStatementOfTruth)
            .mandatory(CaseData::getSolSignStatementOfTruth)
            .mandatory(CaseData::getSolStatementOfReconciliationName)
            .mandatory(CaseData::getSolStatementOfReconciliationFirm)
            .label(
                "LabelSolStatementOTruthPara-7",
                "You could be fined or imprisoned for contempt of court if you deliberately submit false information.")
            .label(
                "LabelSolStatementOTruthPara-8",
                "If you have any comments you would like to make to the court staff regarding the application "
                    + "you may include them below.")
            .optionalNoSummary(CaseData::getStatementOfReconciliationComments)
            .readonlyNoSummary(CaseData::getSolApplicationFeeInPounds);
    }
}
