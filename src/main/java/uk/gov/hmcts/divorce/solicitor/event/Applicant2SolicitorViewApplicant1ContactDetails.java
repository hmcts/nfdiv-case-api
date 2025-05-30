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

import java.util.Collections;

import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class Applicant2SolicitorViewApplicant1ContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String APPLICANT_2_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO = "app2-solicitor-view-app1-contact-info";
    public static final String CONFIDENTIAL_APPLICANT_ERROR = """
        The applicants contact details are confidential. Please complete a general application
        to seek permission to obtain the address from the court.
        """;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(APPLICANT_2_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("View applicant contact info")
            .description("View applicant contact details")
            .showSummary(false)
            .aboutToStartCallback(this::aboutToStart)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("applicant1ContactDetails")
            .pageLabel("Applicant 1 Contact Details")
            .complex(CaseData::getApplicant1)
                .readonly(Applicant::getNonConfidentialAddress)
                .readonly(Applicant::getPhoneNumber)
                .readonly(Applicant::getEmail)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("{} about to start callback invoked for Case Id: {}",
            APPLICANT_2_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO, details.getId()
        );

        boolean applicantIsConfidential = details.getData().getApplicant1().isConfidentialContactDetails();
        if (applicantIsConfidential) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
             .errors(Collections.singletonList(CONFIDENTIAL_APPLICANT_ERROR))
             .build();
        }

        var applicant1 = details.getData().getApplicant1();
        applicant1.setNonConfidentialAddress(applicant1.getAddress());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} about to submit callback invoked for Case Id: {}",
            APPLICANT_2_SOLICITOR_VIEW_APPLICANT_1_CONTACT_INFO, details.getId()
        );

        details.getData().getApplicant1().setNonConfidentialAddress(null);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
