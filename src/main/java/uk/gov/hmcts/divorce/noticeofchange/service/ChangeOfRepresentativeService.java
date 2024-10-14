package uk.gov.hmcts.divorce.noticeofchange.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentative;
import uk.gov.hmcts.divorce.noticeofchange.model.Representative;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor.SOLICITOR_NOTICE_OF_CHANGE;

@Component
@RequiredArgsConstructor
public class ChangeOfRepresentativeService {

    private final IdamService idamService;
    private final HttpServletRequest request;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;

    public void buildChangeOfRepresentative(CaseData caseData,
                                            CaseData beforeCaseData,
                                            String updatedVia,
                                            boolean isApplicant1) {
        Representative addedRepresentative;
        Representative removedRepresentative = null;
        String clientName = isApplicant1 ? caseData.getApplicant1().getFullName() : caseData.getApplicant2().getFullName();
        String updatedBy;
        Solicitor currentSolicitor = isApplicant1 ? caseData.getApplicant1().getSolicitor()  : caseData.getApplicant2().getSolicitor();

        if (SOLICITOR_NOTICE_OF_CHANGE.getValue().equals(updatedVia)) {
            String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
            String s2sToken = authTokenGenerator.generate();
            var changeOrganisationRequest = caseData.getChangeOrganisationRequestField();
            Organisation orgToAdd = changeOrganisationRequest.getOrganisationToAdd();
            String loggedInUserEmail = changeOrganisationRequest.getCreatedBy().toLowerCase();
            String organisationId = orgToAdd.getOrganisationId();

            ProfessionalUser nocRequestingUser = getProfessionalUsers(sysUserToken, s2sToken, organisationId, loggedInUserEmail);
            OrganisationsResponse nocSolicitorOrg = organisationClient
                .getOrganisationByUserId(sysUserToken, s2sToken, nocRequestingUser.getUserIdentifier());
            orgToAdd.setOrganisationName(nocSolicitorOrg.getName());

            Solicitor beforeSolicitor = isApplicant1 ? caseData.getApplicant1().getSolicitor()  : caseData.getApplicant2().getSolicitor();;
            if (beforeSolicitor != null && beforeSolicitor.getOrganisationPolicy() != null) {
                removedRepresentative = updateRepresentative(beforeSolicitor);
            }
            updateOrgPolicyAndSolicitorDetails(currentSolicitor,  nocSolicitorOrg, nocRequestingUser, loggedInUserEmail);
            setApplicantRepresented(isApplicant1 ? caseData.getApplicant1() : caseData.getApplicant2());
            updatedBy = String.join(" ", nocRequestingUser.getFirstName(), nocRequestingUser.getLastName());
            addedRepresentative = updateRepresentative(updatedBy, loggedInUserEmail, orgToAdd);
        } else {
            var userDetails = idamService.retrieveUser(request.getHeader(AUTHORIZATION)).getUserDetails();
            var beforeSolicitor = isApplicant1 ? beforeCaseData.getApplicant1().getSolicitor()
                    : beforeCaseData.getApplicant2().getSolicitor();
            updatedBy = userDetails.getName();
            addedRepresentative = updateRepresentative(currentSolicitor);
            if (beforeSolicitor != null && beforeSolicitor.getOrganisationPolicy() != null) {
                removedRepresentative = updateRepresentative(beforeSolicitor);
            }
        }

        updateChangeOfRepresentativeTab(caseData, clientName, updatedBy,
                updatedVia, addedRepresentative,
                removedRepresentative, getParty(isApplicant1, caseData.getApplicationType()));
    }

    private String getParty(boolean isApplicant1, ApplicationType applicationType) {
        if (isApplicant1) {
            return "Applicant";
        } else {
            return SOLE_APPLICATION.equals(applicationType) ? "Respondent" : "Applicant2";
        }
    }

    private void setApplicantRepresented(Applicant applicant) {
        applicant.setSolicitorRepresented(YesOrNo.YES);
        applicant.setOffline(YesOrNo.NO);
    }

    private void updateOrgPolicyAndSolicitorDetails(Solicitor applicantSolicitor, OrganisationsResponse nocRequestingUserOrg,
                                                    ProfessionalUser nocRequestingUser, String loggedInUserEmail) {

        applicantSolicitor.setName(String.join(" ", nocRequestingUser.getFirstName(), nocRequestingUser.getLastName()));
        applicantSolicitor.setEmail(loggedInUserEmail);
        applicantSolicitor.setFirmName(nocRequestingUserOrg.getName());
        applicantSolicitor.setAddressToOrganisationDefault(nocRequestingUserOrg);
        applicantSolicitor.setAgreeToReceiveEmailsCheckbox(Collections.emptySet());
        applicantSolicitor.setReference(null);
        applicantSolicitor.setPhone(null);
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

    private Representative updateRepresentative(String solicitorName, String solicitorEmail,
                                                Organisation organisation) {
        return Representative.builder()
                .solicitorName(solicitorName)
                .solicitorEmail(solicitorEmail)
                .organisation(organisation)
                .build();
    }

    private Representative updateRepresentative(Solicitor solicitor) {
        return updateRepresentative(solicitor.getName(), solicitor.getEmail(), solicitor.getOrganisationPolicy().getOrganisation());
    }

    private void updateChangeOfRepresentativeTab(CaseData caseData,
                                                String clientName,
                                                String updatedBy,
                                                String updatedVia,
                                                Representative addedRepresentative,
                                                Representative removedRepresentative,
                                                String party) {

        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm:ss a", Locale.US);

        List<ListValue<ChangeOfRepresentative>> representatives = caseData.getChangeOfRepresentatives();
        representatives.add(new ListValue<>(null, ChangeOfRepresentative.builder()
                .party(party)
                .clientName(clientName)
                .updatedBy(updatedBy)
                .updatedVia(updatedVia)
                .addedDateTime(LocalDateTime.now().format(pattern))
                .addedRepresentative(addedRepresentative)
                .removedRepresentative(removedRepresentative)
                .build()));
        Collections.reverse(representatives);
        caseData.setChangeOfRepresentatives(representatives);
    }
}
