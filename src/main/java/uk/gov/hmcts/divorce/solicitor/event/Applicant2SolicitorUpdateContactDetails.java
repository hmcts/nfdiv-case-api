package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorUpdateContactDetails.INVALID_EMAIL_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class Applicant2SolicitorUpdateContactDetails implements CCDConfig<CaseData, State, UserRole> {

    public static final String APP2_SOLICITOR_UPDATE_CONTACT_DETAILS = "app2-solicitor-update-contact-details";

    private final SolicitorCreateApplicationService solicitorCreateApplicationService;

    private final HttpServletRequest request;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(APP2_SOLICITOR_UPDATE_CONTACT_DETAILS)
            .forStates(POST_SUBMISSION_STATES)
            .name("Update contact info")
            .description("Update contact info")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(CASE_WORKER, SUPER_USER, LEGAL_ADVISOR, JUDGE))
            .page("Applicant2SolUpdateContactDetails", this::midEvent)
            .pageLabel("Update your contact details")
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .mandatoryWithLabel(Solicitor::getName, "Your name")
                    .mandatoryWithLabel(Solicitor::getPhone, "Your phone number")
                    .mandatoryWithLabel(Solicitor::getEmail, "Your email")
                    .mandatory(Solicitor::getAgreeToReceiveEmailsCheckbox)
                    .mandatoryWithLabel(Solicitor::getAddress, "Firm address")
                    .mandatory(Solicitor::getAddressOverseas)
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        log.info("{} Mid-event callback invoked for Case Id: {}", APP2_SOLICITOR_UPDATE_CONTACT_DETAILS, details.getId());

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisationAndEmail(
            details.getData().getApplicant2().getSolicitor(), details.getId(), request.getHeader(AUTHORIZATION)
        );

        if (CollectionUtils.isNotEmpty(caseInfo.getErrors())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder().errors(List.of(INVALID_EMAIL_ERROR)).build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder().build();
    }
}
