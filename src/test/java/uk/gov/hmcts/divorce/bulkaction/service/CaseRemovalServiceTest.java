package uk.gov.hmcts.divorce.bulkaction.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@ExtendWith(MockitoExtension.class)
public class CaseRemovalServiceTest {

    @Mock
    private BulkTriggerService bulkTriggerService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private CaseRemovalService caseRemovalService;

    @Test
    void shouldSuccessfullyRemoveCasesSelectedForRemoval() {

    }
}
