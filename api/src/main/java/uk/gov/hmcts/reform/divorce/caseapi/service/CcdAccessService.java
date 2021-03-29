package uk.gov.hmcts.reform.divorce.caseapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Set;

import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.CREATOR_ROLE;
import static uk.gov.hmcts.reform.divorce.caseapi.enums.NotificationConstants.PET_SOL_ROLE;

@Service
@Slf4j
public class CcdAccessService {
    @Autowired
    private CaseUserApi caseUserApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void addPetitionerSolicitorRole(String solicitorIdamToken, String caseId) {
        User solicitorUser = idamService.retrieveUser(solicitorIdamToken);
        User caseworkerUser = idamService.retrieveCaseWorkerDetails();

        Set<String> caseRoles = Set.of(CREATOR_ROLE, PET_SOL_ROLE);

        String solicitorUserId = solicitorUser.getUserDetails().getId();

        log.info("Adding roles {} to case Id {} and user Id {}",
            caseRoles,
            caseId,
            solicitorUserId
        );

        caseUserApi.updateCaseRolesForUser(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            caseId,
            solicitorUserId,
            new CaseUser(solicitorUserId, caseRoles)
        );

        log.info("Successfully added petitioner solicitor roles to case Id {} ", caseId);
    }
}
