package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

public class LanguagePreference implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder<CaseData, UserRole, State> pageBuilder) {

        pageBuilder
            .page("langPref")
            .pageLabel("Select Language")
            .complex(CaseData::getApplicant1)
                .mandatory(Applicant::getLanguagePreferenceWelsh,
                    null,
                    null,
                    "Include a Welsh copy of all generated divorce documents for Applicant 1?",
                    "An English copy will always be included"
                )
                .done();
    }
}
