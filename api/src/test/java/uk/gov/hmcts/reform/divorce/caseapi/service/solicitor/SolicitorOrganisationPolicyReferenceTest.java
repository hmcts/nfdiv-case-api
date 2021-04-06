package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.OrganisationPolicy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class SolicitorOrganisationPolicyReferenceTest {

    @InjectMocks
    private SolicitorOrganisationPolicyReference solicitorOrganisationPolicyReference;

    @Test
    void shouldSetOrganisationPolicyReferenceForBothPetitionerAndRespondent() {

        final String solicitorReference = "Solicitor Reference";
        final String respondentSolicitorReference = "Respondent Solicitor Reference";
        final OrganisationPolicy petitionerOrganisationPolicy = new OrganisationPolicy();
        final OrganisationPolicy respondentOrganisationPolicy = new OrganisationPolicy();

        final CaseData caseData = new CaseData();
        caseData.setSolicitorReference(solicitorReference);
        caseData.setRespondentSolicitorReference(respondentSolicitorReference);
        caseData.setPetitionerOrganisationPolicy(petitionerOrganisationPolicy);
        caseData.setRespondentOrganisationPolicy(respondentOrganisationPolicy);

        solicitorOrganisationPolicyReference.handle(caseData);

        assertThat(petitionerOrganisationPolicy.getOrgPolicyReference(), is(solicitorReference));
        assertThat(respondentOrganisationPolicy.getOrgPolicyReference(), is(respondentSolicitorReference));
    }

    @Test
    void shouldNotSetSolicitorOrganisationPolicyReferenceIfNoSolicitorReference() {

        final OrganisationPolicy organisationPolicy = new OrganisationPolicy();

        final CaseData caseData = new CaseData();
        caseData.setPetitionerOrganisationPolicy(organisationPolicy);

        solicitorOrganisationPolicyReference.handle(caseData);

        assertThat(organisationPolicy.getOrgPolicyReference(), is(nullValue()));
    }

    @Test
    void shouldNotSetSolicitorOrganisationPolicyReferenceIfThereIsNoPetitionerOrganisationPolicy() {

        final String solicitorReference = "Solicitor Reference";

        final CaseData caseData = new CaseData();
        caseData.setSolicitorReference(solicitorReference);

        solicitorOrganisationPolicyReference.handle(caseData);

        assertThat(caseData.getPetitionerOrganisationPolicy(), is(nullValue()));
    }

    @Test
    void shouldSetRespondentSolicitorOrganisationPolicyReferenceIfThereIsNoRespondentSolicitorReference() {

        final OrganisationPolicy organisationPolicy = new OrganisationPolicy();

        final CaseData caseData = new CaseData();
        caseData.setRespondentOrganisationPolicy(organisationPolicy);

        solicitorOrganisationPolicyReference.handle(caseData);

        assertThat(organisationPolicy.getOrgPolicyReference(), is(nullValue()));
    }

    @Test
    void shouldSetRespondentSolicitorOrganisationPolicyReferenceIfThereIsNoRespondentOrganisationPolicy() {

        final String respondentSolicitorReference = "Respondent Solicitor Reference";

        final CaseData caseData = new CaseData();
        caseData.setRespondentSolicitorReference(respondentSolicitorReference);

        solicitorOrganisationPolicyReference.handle(caseData);

        assertThat(caseData.getRespondentOrganisationPolicy(), is(nullValue()));
    }
}