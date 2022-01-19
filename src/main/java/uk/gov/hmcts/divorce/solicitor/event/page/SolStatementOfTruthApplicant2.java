package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

public class SolStatementOfTruthApplicant2 implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "applicant2StatementOfTruth=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolStatementOfTruthApplicant2")
            .showCondition("applicant2ConfirmApplicant1Information=\"No\"")
            .pageLabel("Statement of truth and reconciliation")
            .readonlyNoSummary(CaseData::getDivorceOrDissolution, ALWAYS_HIDE)
            .complex(CaseData::getApplication)
                .label("LabelPrayer", "## The prayer ##")
                .mandatory(Application::getApplicant2PrayerHasBeenGivenCheckbox)
                .label("LabelSolStatementOTruth-PrayerDivorceBullet",
                "- dissolve their marriage (get a divorce)",
                "divorceOrDissolution=\"divorce\"")
                .label("LabelSolStatementOTruth-PrayerCivilBullet",
                "- end their civil partnership",
                "divorceOrDissolution=\"dissolution\"")
                .label("LabelApp2SolStatementOfTruth-SOT", "## Statement of truth ##")
                .mandatory(Application::getApplicant2StatementOfTruth)
                .mandatory(Application::getApplicant2SolSignStatementOfTruth)
                .label("LabelApp2SolStatementOfTruth-SOTInfo",
                    "This confirms that the information you are submitting on behalf of the applicant is true and accurate, "
                        + "to the best of your knowledge. It’s known as the ‘statement of truth’. ")
                .label("LabelApp2SolStatementOTruth-Statement",
                    "**Proceedings for contempt of court may be brought against anyone who makes, or causes to be made, "
                        + "a false statement verified by a statement of truth without an honest belief in its truth.**")
                .mandatory(Application::getApplicant2SolStatementOfReconciliationName)
                .mandatory(Application::getApplicant2SolStatementOfReconciliationFirm)
                .label("LabelApp2SolStatementOfTruth-Comments",
                    "If you have any comments you would like to make to the court staff regarding the application you "
                        + "may include them below.")
                .optionalNoSummary(Application::getApplicant2StatementOfReconciliationComments)
            .done();
    }
}
