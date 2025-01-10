package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseFlagsService {

    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    public void setSupplementaryDataForCaseFlags(Long caseId) {

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        ccdUpdateService.submitSupplementaryDataToCcdForServiceID(
            caseId.toString(),
            sysUserToken,
            s2sToken
        );
    }
}
