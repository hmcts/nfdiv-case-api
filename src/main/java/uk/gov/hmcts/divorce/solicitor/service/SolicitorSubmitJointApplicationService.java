package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.List;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.Applicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.common.event.Applicant2RequestChanges.APPLICANT_2_REQUEST_CHANGES;

@Service
@Slf4j
public class SolicitorSubmitJointApplicationService {

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private HttpServletRequest request;

    @Async
    public void submitEventForApprovalOrRequestingChanges(final CaseDetails<CaseData, State> details) {
        final Application application = details.getData().getApplication();

        if (details.getData().getApplicant2().isRepresented()) {
            setApplicant2SolicitorAddress(details);
        }

        String eventId = YES.equals(application.getApplicant2ConfirmApplicant1Information())
            ? APPLICANT_2_REQUEST_CHANGES
            : APPLICANT_2_APPROVE;

        User solUser = idamService.retrieveSystemUpdateUserDetails();

        final String serviceAuthorization = authTokenGenerator.generate();

        log.info("Submitting event id {} for case id: {}", eventId, details.getId());

        ccdUpdateService.submitEvent(details, eventId, solUser, serviceAuthorization);
    }

    private void setApplicant2SolicitorAddress(CaseDetails<CaseData, State> caseDetails) {
        final List<OrganisationContactInformation> contactInformation = organisationClient
            .getUserOrganisation(request.getHeader(AUTHORIZATION), authTokenGenerator.generate())
            .getContactInformation();

        if (!isEmpty(contactInformation)) {
            final OrganisationContactInformation organisationContactInformation = contactInformation.get(0);

            final String solicitorAddress = Stream.of(
                organisationContactInformation.getAddressLine1(),
                organisationContactInformation.getAddressLine2(),
                organisationContactInformation.getAddressLine3(),
                organisationContactInformation.getTownCity(),
                organisationContactInformation.getCounty(),
                organisationContactInformation.getPostCode(),
                organisationContactInformation.getCountry())
                .filter(value -> !isEmpty(value))
                .collect(joining("\n"));

            log.info("Setting Applicant 2 solicitor address.  Case ID: {}", caseDetails.getId());
            caseDetails.getData().getApplicant2().getSolicitor().setAddress(solicitorAddress);
        }
    }
}
