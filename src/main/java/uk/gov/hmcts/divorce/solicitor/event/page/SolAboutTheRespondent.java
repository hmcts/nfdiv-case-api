package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;

public class SolAboutTheRespondent implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutTheRespondent")
            .pageLabel("About the respondent")
            .label(
                "LabelSolAboutEditingApplication-AboutRespondent",
                "You can make changes at the end of your application.")
            .mandatory(CaseData::getRespondentFirstName)
            .mandatory(CaseData::getRespondentLastName)
            .mandatory(CaseData::getRespondentNameAsOnMarriageCertificate)
            .optional(
                CaseData::getRespNameDifferentToMarriageCertExplain,
                "respondentNameAsOnMarriageCertificate=\"Yes\"")
            .mandatory(CaseData::getInferredRespondentGender);
    }
}
