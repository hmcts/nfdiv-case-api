package uk.gov.hmcts.divorce.solicitor.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.event.page.CheckApplicant1SolicitorAnswers;
import uk.gov.hmcts.divorce.solicitor.event.page.FinancialOrdersForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.HelpWithFeesPageForApplicant2;
import uk.gov.hmcts.divorce.solicitor.event.page.MarriageIrretrievablyBrokenForApplicant2;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.citizen.event.CitizenApplicant2RequestChanges.CITIZEN_APPLICANT_2_REQUEST_CHANGES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Slf4j
@Component
public class SolicitorSubmitJointApplication implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_SUBMIT_JOINT_APPLICATION = "solicitor-submit-joint-application";

    @Autowired
    private MarriageIrretrievablyBrokenForApplicant2 marriageIrretrievablyBrokenForApplicant2;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            marriageIrretrievablyBrokenForApplicant2,
            new FinancialOrdersForApplicant2(),
            new HelpWithFeesPageForApplicant2(),
            new CheckApplicant1SolicitorAnswers()
        );

        final PageBuilder pageBuilder = addEventConfig(configBuilder);

        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        return new PageBuilder(configBuilder.event(SOLICITOR_SUBMIT_JOINT_APPLICATION)
            .forStates(AwaitingApplicant2Response, Draft)
            .name("Submit joint application")
            .description("Submit joint application")
            .submittedCallback(this::submitted)
            .showSummary(false)
            .endButtonLabel("Submit Application")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grant(READ,
                APPLICANT_1_SOLICITOR,
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR));
    }

    public SubmittedCallbackResponse submitted(final CaseDetails<CaseData, State> details,
                                               final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Solicitor submit joint application submitted callback invoked for case id: {}", details.getId());

        submitEventForApprovalOrRequestingChanges(details);

        return SubmittedCallbackResponse.builder().build();
    }

    @Async
    public void submitEventForApprovalOrRequestingChanges(CaseDetails<CaseData, State> details) {
        final Application application = details.getData().getApplication();

        String eventId;
        if (YES.equals(application.getApplicant2ConfirmApplicant1Information())) {
            eventId = CITIZEN_APPLICANT_2_REQUEST_CHANGES;
        } else {
            eventId = APPLICANT_2_APPROVE;
        }

        String solicitorAuthToken = httpServletRequest.getHeader(AUTHORIZATION);
        User solUser = idamService.retrieveUser(solicitorAuthToken);

        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting event id {} for case id: {}", eventId, details.getId());

        ccdUpdateService.submitEvent(details, eventId, solUser, serviceAuthorization);
    }
}
