package uk.gov.hmcts.reform.divorce.ccd.event.solicitor.page;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class SolAboutTheSolicitor implements CcdPageConfiguration {

    @Override
    public void addTo(final FieldCollectionBuilder<CaseData, EventBuilder<CaseData, UserRole, State>> fieldCollectionBuilder) {

        fieldCollectionBuilder
            .page("SolAboutTheSolicitor")
            .pageLabel("About the Solicitor")
            .label(
                "LabelSolAboutEditingApplication-AboutSolicitor",
                "You can make changes at the end of your application.")
            .label(
                "LabelSolAboutTheSolPara-1",
                "Please note that the information provided will be used as evidence by the court to decide if "
                    + "the petitioner is entitled to legally end their marriage. **A copy of this form is sent to the "
                    + "respondent**")
            .mandatory(CaseData::getPetitionerSolicitorName)
            .mandatory(CaseData::getSolicitorReference)
            .mandatory(CaseData::getPetitionerSolicitorPhone)
            .mandatory(CaseData::getPetitionerSolicitorEmail)
            .mandatory(CaseData::getSolicitorAgreeToReceiveEmails)
            .mandatory(CaseData::getDerivedPetitionerSolicitorAddress);
    }
}
