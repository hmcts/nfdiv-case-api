package uk.gov.hmcts.divorce.noticeofchange.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.noticeofchange.client.AssignCaseAccessClient;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentative;
import uk.gov.hmcts.divorce.noticeofchange.model.Representative;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.NOC_APPROVER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.noticeofchange.model.AcaRequest.acaRequest;
import static uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor.SOLICITOR_NOTICE_OF_CHANGE;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemApplyNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {

    private final  AuthTokenGenerator authTokenGenerator;
    private final  ObjectMapper objectMapper;
    private final  AssignCaseAccessClient assignCaseAccessClient;
    private final  IdamService idamService;
    private final OrganisationClient organisationClient;

    public static final String NOTICE_OF_CHANGE_APPLIED = "notice-of-change-applied";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(NOTICE_OF_CHANGE_APPLIED)
            .forStates(POST_SUBMISSION_STATES)
            .name("Notice Of Change Applied")
            .grant(CREATE_READ_UPDATE, NOC_APPROVER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE, CASE_WORKER, SUPER_USER)
            .aboutToStartCallback(this::aboutToStart));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {
        log.info("Applying notice of change for case id: {}", details.getId());

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        updateChangeOfRepresentation(details, sysUserToken, s2sToken);

        AboutToStartOrSubmitCallbackResponse response =
            assignCaseAccessClient.applyNoticeOfChange(sysUserToken, s2sToken, acaRequest(details));

        Map<String, Object> data = response.getData();
        List<String> responseErrors = response.getErrors();

        if (!Objects.isNull(responseErrors)) {
            log.info("Notice of change failed with the following error(s) for CaseID {}:", details.getId());
            responseErrors.forEach(log::info);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .data(details.getData())
                    .state(details.getState())
                    .errors(responseErrors)
                    .build();
        }

        CaseData responseData = objectMapper.convertValue(data, CaseData.class);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(responseData)
            .state(details.getState())
            .build();
    }

    private void updateChangeOfRepresentation(CaseDetails<CaseData, State> details, String sysUserToken, String s2sToken) {
        var caseData = details.getData();
        var changeOrganisationRequest = caseData.getChangeOrganisationRequestField();
        var loggedInUserEmail = changeOrganisationRequest.getCreatedBy().toLowerCase();
        var applicant1Solicitor = caseData.getApplicant1().getSolicitor();
        var applicant2Solicitor = caseData.getApplicant2().getSolicitor();
        var orgToAdd = changeOrganisationRequest.getOrganisationToAdd();
        var organisationId = orgToAdd.getOrganisationId();
        var isApplicant1 = APPLICANT_1_SOLICITOR.getRole().equals(changeOrganisationRequest.getCaseRoleId().getRole());
        var clientName = isApplicant1 ? caseData.getApplicant1().getFullName() : caseData.getApplicant2().getFullName();

        final ProfessionalUser professionalUser = getProfessionalUsers(sysUserToken, s2sToken, organisationId, loggedInUserEmail);
        var updatedBy = String.join(" ", professionalUser.getFirstName(), professionalUser.getLastName());
        final var nocRequestingUser = getProfessionalUsers(sysUserToken, s2sToken, organisationId, loggedInUserEmail);

        String nocSolicitorOrgName = organisationClient
                .getOrganisationByUserId(sysUserToken, s2sToken, nocRequestingUser.getUserIdentifier())
                .getName();
        orgToAdd.setOrganisationName(nocSolicitorOrgName);

        String party;
        Representative addedRepresentative;
        Representative removedRepresentative;

        if (isApplicant1) {
            party = "Applicant";
            addedRepresentative = updateRepresentative(updatedBy, loggedInUserEmail, orgToAdd);
            removedRepresentative = updateRepresentative(applicant1Solicitor.getName(),
             applicant1Solicitor.getEmail(), applicant1Solicitor.getOrganisationPolicy().getOrganisation());
            updateOrgPolicyAndSolicitorDetails(applicant1Solicitor,
                    nocRequestingUser, loggedInUserEmail);
        } else {
            party = SOLE_APPLICATION.equals(caseData.getApplicationType()) ? "Respondent" : "Applicant2";
            addedRepresentative = updateRepresentative(updatedBy, loggedInUserEmail, orgToAdd);
            removedRepresentative = updateRepresentative(applicant2Solicitor.getName(),
                    applicant2Solicitor.getEmail(), applicant2Solicitor.getOrganisationPolicy().getOrganisation());
            updateOrgPolicyAndSolicitorDetails(applicant2Solicitor,
                    nocRequestingUser, loggedInUserEmail);
        }

        updateChangeOfRepresentativeTab(caseData, clientName, updatedBy,
                SOLICITOR_NOTICE_OF_CHANGE.getValue(), addedRepresentative,
                removedRepresentative, party);
    }

    private ProfessionalUser getProfessionalUsers(String sysUserToken, String s2sToken, String organisationId, String loggedInUserEmail) {
        var organisationUsers = Optional.ofNullable(organisationClient.getOrganisationUsers(sysUserToken, s2sToken, organisationId))
                .map(FindUsersByOrganisationResponse::getUsers)
                .orElse(Collections.emptyList());

        return organisationUsers.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(loggedInUserEmail))
                .findFirst()
                .orElseThrow();
    }

    private static void updateOrgPolicyAndSolicitorDetails(Solicitor applicantSolicitor,
                                                           ProfessionalUser nocRequestingUser, String loggedInUserEmail) {

        applicantSolicitor.setName(String.join(" ", nocRequestingUser.getFirstName(), nocRequestingUser.getLastName()));
        applicantSolicitor.setEmail(loggedInUserEmail);
    }


    private static ChangeOfRepresentative buildChangeOfRepresentative(String clientName,
                                                              String updatedBy,
                                                              String updatedVia,
                                                              Representative addedRepresentative,
                                                              Representative removedRepresentative,
                                                              String party) {

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm:ss a", Locale.US);
        return ChangeOfRepresentative.builder()
                .party(party)
                .clientName(clientName)
                .updatedBy(updatedBy)
                .updatedVia(updatedVia)
                .addedDateTime(LocalDateTime.now().format(pattern))
                .addedRepresentative(addedRepresentative)
                .removedRepresentative(removedRepresentative)
                .build();
    }

    public static Representative updateRepresentative(String solicitorName, String solicitorEmail, Organisation organisation) {
        return Representative.builder()
                .solicitorName(solicitorName)
                .solicitorEmail(solicitorEmail)
                .organisation(organisation)
                .build();
    }

    public static void updateChangeOfRepresentativeTab(CaseData caseData,
                                                 String clientName,
                                                 String updatedBy,
                                                 String updatedVia,
                                                 Representative addedRepresentative,
                                                 Representative removedRepresentative,
                                                 String party) {

        List<ListValue<ChangeOfRepresentative>> representatives = caseData.getChangeOfRepresentatives();
        ChangeOfRepresentative representative = buildChangeOfRepresentative(clientName, updatedBy, updatedVia,
                addedRepresentative, removedRepresentative, party);
        representatives.add(new ListValue<>(null, representative));
        Collections.reverse(representatives);
        caseData.setChangeOfRepresentatives(representatives);
    }
}
