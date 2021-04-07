package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataContext;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChain;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.OrganisationPolicy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitorOrganisationPolicyReferenceTest {

    @Mock
    private CaseDataContext caseDataContext;

    @Mock
    private CaseDataUpdaterChain caseDataUpdaterChain;

    @InjectMocks
    private SolicitorOrganisationPolicyReference solicitorOrganisationPolicyReference;

    @Test
    void shouldSetOrganisationPolicyReferenceForBothPetitionerAndRespondent() {

        final String solicitorReference = "Solicitor Reference";
        final String respondentSolicitorReference = "Respondent Solicitor Reference";
        final OrganisationPolicy petitionerOrganisationPolicy = OrganisationPolicy.builder().build();
        final OrganisationPolicy respondentOrganisationPolicy = OrganisationPolicy.builder().build();

        final CaseData caseData = CaseData.builder()
            .solicitorReference(solicitorReference)
            .respondentSolicitorReference(respondentSolicitorReference)
            .petitionerOrganisationPolicy(petitionerOrganisationPolicy)
            .respondentOrganisationPolicy(respondentOrganisationPolicy)
            .build();

        setupMocks(caseData);

        solicitorOrganisationPolicyReference.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(caseData.getPetitionerOrganisationPolicy().getOrgPolicyReference(), is(solicitorReference));
        assertThat(caseData.getRespondentOrganisationPolicy().getOrgPolicyReference(), is(respondentSolicitorReference));
    }

    @Test
    void shouldNotSetSolicitorOrganisationPolicyReferenceIfNoSolicitorReference() {

        final OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().build();
        final CaseData caseData = CaseData.builder()
            .petitionerOrganisationPolicy(organisationPolicy)
            .build();

        setupMocks(caseData);

        solicitorOrganisationPolicyReference.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(organisationPolicy.getOrgPolicyReference(), is(nullValue()));
    }

    @Test
    void shouldNotSetSolicitorOrganisationPolicyReferenceIfThereIsNoPetitionerOrganisationPolicy() {

        final String solicitorReference = "Solicitor Reference";

        final CaseData caseData = CaseData.builder()
            .solicitorReference(solicitorReference)
            .build();

        setupMocks(caseData);

        solicitorOrganisationPolicyReference.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(caseData.getPetitionerOrganisationPolicy(), is(nullValue()));
    }

    @Test
    void shouldSetRespondentSolicitorOrganisationPolicyReferenceIfThereIsNoRespondentSolicitorReference() {

        final OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().build();

        final CaseData caseData = CaseData.builder()
            .respondentOrganisationPolicy(organisationPolicy)
            .build();

        setupMocks(caseData);

        solicitorOrganisationPolicyReference.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(organisationPolicy.getOrgPolicyReference(), is(nullValue()));
    }

    @Test
    void shouldSetRespondentSolicitorOrganisationPolicyReferenceIfThereIsNoRespondentOrganisationPolicy() {

        final String respondentSolicitorReference = "Respondent Solicitor Reference";

        final CaseData caseData = CaseData.builder()
            .respondentSolicitorReference(respondentSolicitorReference)
            .build();

        setupMocks(caseData);

        solicitorOrganisationPolicyReference.updateCaseData(caseDataContext, caseDataUpdaterChain);

        assertThat(caseData.getRespondentOrganisationPolicy(), is(nullValue()));
    }

    private void setupMocks(final CaseData caseData) {
        when(caseDataContext.copyOfCaseData()).thenReturn(caseData);
        when(caseDataContext.handlerContextWith(caseData)).thenReturn(caseDataContext);
        when(caseDataUpdaterChain.processNext(caseDataContext)).thenReturn(caseDataContext);
    }
}