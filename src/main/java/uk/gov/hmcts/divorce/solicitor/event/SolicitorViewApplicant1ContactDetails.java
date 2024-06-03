package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class SolicitorViewApplicant1ContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO = "solicitor-view-applicant-1-contact-info";
    public static final String CONFIDENTIAL_APPLICANT_ERROR = """
            The applicants contact details are confidential. Please complete a general application
            to seek permission to obtain the address from the court.
        """;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("View applicant 1 contact info")
            .description("View applicant 1 contact details")
            .showSummary(false)
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("applicant1ContactDetails")
            .pageLabel("Applicant 1 Contact Details")
            .complex(CaseData::getApplicant1)
                .readonly(Applicant::getAddress)
                .readonly(Applicant::getPhoneNumber)
                .readonly(Applicant::getEmail)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}", SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO, details.getId());

        List<String> errors = new ArrayList<>();

        boolean applicantIsConfidential = details.getData().getApplicant1().isConfidentialContactDetails();
        if (applicantIsConfidential) {
            errors.add(CONFIDENTIAL_APPLICANT_ERROR);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(errors)
            .build();
    }
}
