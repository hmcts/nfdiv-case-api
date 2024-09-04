package uk.gov.hmcts.divorce.solicitor.service.task;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.divorce.divorcecase.util.SolicitorAddressPopulator.parseOrganisationAddress;

@Component
@Slf4j
public class SetApplicant1SolicitorAddress implements CaseTask {

    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        if (caseDetails.getData().getApplicant1().isRepresented()) {
            final List<OrganisationContactInformation> contactInformation = organisationClient
                .getUserOrganisation(request.getHeader(AUTHORIZATION), authTokenGenerator.generate())
                .getContactInformation();

            if (!isEmpty(contactInformation)) {
                final String solicitorAddress = parseOrganisationAddress(contactInformation);

                log.info("Setting Applicant 1 solicitor address.  Case ID: {}", caseDetails.getId());
                caseDetails.getData().getApplicant1().getSolicitor().setAddress(solicitorAddress);
            }
        }

        return caseDetails;
    }
}
