package uk.gov.hmcts.reform.divorce.ccd.search;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

public class SearchTab implements CcdConfiguration {

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.searchResultFields().field(CaseData::getD8PetitionerFirstName, "FirstName")
                .field(CaseData::getD8PetitionerLastName, "LastName")
            .field(CaseData::getD8PetitionerEmail, "Email");

        //configBuilder.searchResultFields().field(CaseData::getD8PetitionerLastName, "LastName");
        //configBuilder.searchResultFields().field(CaseData::getD8PetitionerEmail, "Email");

    }
}
