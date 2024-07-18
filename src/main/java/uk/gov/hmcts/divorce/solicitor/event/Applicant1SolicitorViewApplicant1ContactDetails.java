package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class Applicant1SolicitorViewApplicant1ContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_1_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO = "app1-solicitor-view-app1-contact-info";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(APPLICANT_1_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("View applicant contact info")
            .description("View applicant contact details")
            .showSummary(false)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("applicant1ContactDetails")
            .pageLabel("Applicant Contact Details")
            .complex(CaseData::getApplicant1)
                .readonly(Applicant::getAddress)
                .readonly(Applicant::getPhoneNumber)
                .readonly(Applicant::getEmail)
            .done();
    }
}
