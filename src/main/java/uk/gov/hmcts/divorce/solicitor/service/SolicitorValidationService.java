package uk.gov.hmcts.divorce.solicitor.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Slf4j
@RequiredArgsConstructor
@Service
public class SolicitorValidationService {

    private final OrganisationClient organisationClient;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    public Optional<String> findSolicitorByEmail(String email, Long caseId) {
        final String authToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();
        try {
            log.info("finding user by email {} for case {}", email, caseId);
            return Optional.of(organisationClient.findUserByEmail(authToken, s2sToken, email).getUserIdentifier());
        } catch (FeignException.NotFound notFoundException) {
            log.info("User with email {} not found for case {}", email, caseId);
            return Optional.empty();
        } catch (FeignException exception) {
            throw new RuntimeException(getStackTrace(exception));
        }
    }

    public boolean isSolicitorInOrganisation(String userId, String organisationId) {
        final String authToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String s2sToken = authTokenGenerator.generate();

        final var organisationUsers = Optional.ofNullable(organisationClient.getOrganisationUsers(authToken, s2sToken, organisationId))
            .map(FindUsersByOrganisationResponse::getUsers)
            .orElse(Collections.emptyList());

        return organisationUsers.stream()
            .map(ProfessionalUser::getUserIdentifier)
            .anyMatch(userId::equals);
    }

    public List<String> validateEmailBelongsToOrgUser(String email,
                                                      Long caseId,
                                                      String organisationId) {
        List<String> errors = new ArrayList<>();

        Optional<String> userId = findSolicitorByEmail(email, caseId);

        if (userId.isEmpty()) {
            errors.add("No user found with provided email. Please check the email address and try again");
        } else if (!isSolicitorInOrganisation(userId.get(), organisationId)) {
            errors.add("The email address provided does not belong to a user in the selected organisation. "
                + "Please ensure the user is in the selected organisation.");
        }

        return errors;
    }
}
