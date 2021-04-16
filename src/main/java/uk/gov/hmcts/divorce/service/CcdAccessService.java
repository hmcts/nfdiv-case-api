package uk.gov.hmcts.divorce.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Set;

import static uk.gov.hmcts.divorce.ccd.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.ccd.model.UserRole.PETITIONER_SOLICITOR;

@Service
@Slf4j
public class CcdAccessService {
    @Autowired
    private CaseUserApi caseUserApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void addPetitionerSolicitorRole(String solicitorIdamToken, Long caseId) {
        User solicitorUser = idamService.retrieveUser(solicitorIdamToken);
        User caseworkerUser = idamService.retrieveCaseWorkerDetails();

        Set<String> caseRoles = Set.of(CREATOR.getRole(), PETITIONER_SOLICITOR.getRole());

        String solicitorUserId = solicitorUser.getUserDetails().getId();

        log.info("Adding roles {} to case Id {} and user Id {}",
            caseRoles,
            caseId,
            solicitorUserId
        );

        caseUserApi.updateCaseRolesForUser(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            String.valueOf(caseId),
            solicitorUserId,
            new CaseUser(solicitorUserId, caseRoles)
        );

        log.info("Successfully added petitioner solicitor roles to case Id {} ", caseId);
    }
}
