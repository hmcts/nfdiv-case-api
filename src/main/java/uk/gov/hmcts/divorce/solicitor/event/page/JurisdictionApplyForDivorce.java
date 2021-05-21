package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class JurisdictionApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("JurisdictionApplyForDivorce")
            .pageLabel("Jurisdiction - Apply for a divorce")
            .label(
                "LabelNFDBanner-JurisdictionApplyForDivorce",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-JurisdictionApplyForDivorce",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label(
                "LabelSolJurisdictionPara-1",
                "The court has legal power to deal with this application because the following applies:")
            .label(
                "LabelSolicitorOppositeSexRegulations",
                "Divorce – *Opposite Sex Couple* – Article 3(1) of Council Regulation (EC) No 2201/2003"
                    + "of 27 November 2003"
            )
            .mandatory(CaseData::getLegalConnections)
            .label(
                "ResidualJurisdiction-Jurisdiction",
                "### Residual Jurisdiction"
            )
            .label(
                "labelResidualJurisdictionPara-1",
                "The court may have residual jurisdiction if;"
            )
            .label(
                "labelResidualJurisdictionPara-2",
                "none of the other connections applies in relation to England and Wales;"
            )
            .label(
                "labelResidualJurisdictionPara-3",
                "either applicant 1 or applicant 2 is domiciled in England and Wales; and"
            )
            .label(
                "labelResidualJurisdictionPara-4",
                "neither applicant 1 nor applicant 2 is able to apply for a divorce in another member state "
                    + "of the EU on the basis of any of the other connections."
            )
            .label(
                "labelResidualJurisdictionPara-5",
                "In addition, in the case of a same-sex marriage, "
                    + "the court may have residual jurisdiction if the following apply:"
            )
            .label(
                "labelResidualJurisdictionBullet-1",
                "• Applicant 1 and applicant 2 married each other in England and Wales; and"
            )
            .label(
                "labelResidualJurisdictionBullet-2",
                "• Neither applicant 1 nor applicant 2 is able to apple for a divorce in any other country; and"
            )
            .label(
                "labelResidualJurisdictionBullet-3",
                "• It would be in the interests of justice for the court to consider the application "
                    + "(this may apply if, for example, applicant 1's or applicant 2's home country doesn't "
                    + "allow divorce between same-sex couples)."
            );
    }
}
