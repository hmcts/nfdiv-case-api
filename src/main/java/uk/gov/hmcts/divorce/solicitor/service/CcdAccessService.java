package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CREATOR;

@Service
@Slf4j
public class CcdAccessService {
    @Autowired
    private CaseUserApi caseUserApi;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public void addApplicant1SolicitorRole(String solicitorIdamToken, Long caseId) {
        User solicitorUser = idamService.retrieveUser(solicitorIdamToken);
        User caseworkerUser = idamService.retrieveCaseWorkerDetails();

        Set<String> caseRoles = Set.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        String solicitorUserId = solicitorUser.getUserDetails().getId();

        log.info("Adding roles {} to case Id {} and user Id {}",
            caseRoles,
            caseId,
            solicitorUserId
        );

        CollectionUtils.isEmpty()
        caseUserApi.updateCaseRolesForUser(
            caseworkerUser.getAuthToken(),
            authTokenGenerator.generate(),
            String.valueOf(caseId),
            solicitorUserId,
            new CaseUser(solicitorUserId, caseRoles)
        );

        log.info("Successfully added applicant 1 solicitor roles to case Id {} ", caseId);
    }
}
