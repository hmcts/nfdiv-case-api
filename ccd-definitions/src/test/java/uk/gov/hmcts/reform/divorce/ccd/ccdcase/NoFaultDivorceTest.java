package uk.gov.hmcts.reform.divorce.ccd.ccdcase;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.divorce.ccd.model.Constants.CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.ccd.model.Constants.JURISDICTION;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
public class NoFaultDivorceTest {

    private final NoFaultDivorce nofaultDivorce = new NoFaultDivorce();

    @Mock
    private ConfigBuilder configBuilder;

    @SuppressWarnings("unchecked")
    @Test
    public void shouldBuildNoFaultDivorceCaseTypeWithConfigBuilder() {

        nofaultDivorce.applyTo(configBuilder);

        verify(configBuilder).caseType(CASE_TYPE, "No Fault Divorce case", "Handling of the dissolution of marriage");
        verify(configBuilder).jurisdiction(JURISDICTION, "Family Divorce", "Family Divorce: dissolution of marriage");
        verify(configBuilder).grant(Draft, "CRU", CITIZEN);
        verify(configBuilder).grant(Draft, "R", CASEWORKER_DIVORCE_COURTADMIN_BETA);
        verify(configBuilder).grant(Draft, "R", CASEWORKER_DIVORCE_COURTADMIN);
        verify(configBuilder).grant(Draft, "R", CASEWORKER_DIVORCE_SOLICITOR);
        verify(configBuilder).grant(Draft, "R", CASEWORKER_DIVORCE_SUPERUSER);
        verify(configBuilder).grant(Draft, "R", CASEWORKER_DIVORCE_COURTADMIN_LA);

        verifyNoMoreInteractions(configBuilder);
    }
}
