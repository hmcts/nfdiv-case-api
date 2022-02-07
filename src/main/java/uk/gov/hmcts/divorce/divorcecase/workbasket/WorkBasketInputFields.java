package uk.gov.hmcts.divorce.divorcecase.workbasket;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

@Component
public class WorkBasketInputFields implements CCDConfig<CaseData, State, UserRole> {

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .workBasketInputFields()
            .caseReferenceField()
            .field("marriageDate", "Marriage Date")
            .field("applicant1HWFReferenceNumber", "HWF reference")
            .field("solUrgentCase", "Urgent case")
            .field("", "Urgent general referral case")
            .field("applicant1SolicitorFirmName", "Solicitor firm name")
            .field("alternativeServiceType", "Type of service")
            .field("applicant1HomeAddress", "Applicant Postcode", "PostCode")
            .field("applicant2HomeAddress", "Respondent Postcode", "PostCode")
            .field("applicant1Email", "Applicant Email")
            .field("applicant2Email", "Respondent Email")
            .field("applicant1FirstName", "Applicant's first name")
            .field("applicant1LastName", "Applicant's Last Name")
            .field("applicant2FirstName", "Respondent's first name")
            .field("applicant2LastName", "Respondent's last name")
            .field("evidenceHandled", "Supplementary evidence handled");
    }
}
