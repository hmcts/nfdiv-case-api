package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

public class JurisdictionApplyForDivorce implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollection.FieldCollectionBuilder<CaseData, Event.EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("JurisdictionApplyForDivorce")
            .pageLabel("Jurisdiction - Apply for a divorce")
            .label(
                "JurisdictionApplyForDivorce-Jurisdiction",
                "# Jurisdiction - Apply for a divorce"
            )
            .label(
                "LabelSolAboutEditingApplication-Jurisdiction",
                "You can make changes at the end of your application.")
            .label(
                "LabelSolJurisdictionPara-1",
                "The court has legal power to deal with this application because the following applies:")
            .label(
                "LabelSolicitorOppositeSexRegulations",
                "Divorce – *Opposite Sex Couple* – Article 3(1) of Council Regulation (EC) No 2201/2003 of 27 November 2003"
            )
            .optional(CaseData::getJurisdictionConnections)
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
                "either the petitioner or the respondent is domiciled in England and Wales; and"
            )
            .label(
                "labelResidualJurisdictionPara-4",
                "neither the petitioner nor the respondent is able to apply for a divorce in another member state of the EU on the basis of any of the other connections."
            )
            .label(
                "labelResidualJurisdictionPara-5",
                "In addition, in the case of a same-sex marriage, the court may have residual jurisdiction if the following apply:"
            )
            .label(
                "labelResidualJurisdictionBullet-1",
                "• The petitioner and the respondent married each other in England and Wales; and"
            )
            .label(
                "labelResidualJurisdictionBullet-2",
                "• Neither the petitioner nor the respondent is able to apple for a divorce in any other country; and"
            )
            .label(
                "labelResidualJurisdictionBullet-3",
                "• It would be in the interests of justice for hte court to consider the application " +
                    "(this may apply if, for example, the petitioner's or respondent's home country doesn't allow divorce between same-sex couples)."
            );
    }
}
