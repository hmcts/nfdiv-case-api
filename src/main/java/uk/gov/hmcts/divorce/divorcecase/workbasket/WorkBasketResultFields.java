package uk.gov.hmcts.divorce.divorcecase.workbasket;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class WorkBasketResultFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .workBasketResultFields()
            .caseReferenceField()
            .field("applicationType", "Application Type")
            .field("applicant1FirstName", "Applicant's First Name")
            .field("applicant1LastName", "Applicant's Last Name")
            .field("applicant2FirstName", "Respondent's First Name")
            .field("applicant2LastName", "Respondent's Last Name")
            .field("dueDate","Due Date")
            .lastModifiedDate();
    }
}
