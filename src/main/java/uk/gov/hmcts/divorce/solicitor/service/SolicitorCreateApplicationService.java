package uk.gov.hmcts.divorce.solicitor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.service.task.DivorceApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.InitialiseSolicitorCreatedApplication;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicant1SolicitorAddress;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicantContactDetails;
import uk.gov.hmcts.divorce.solicitor.service.task.SetApplicantGender;
import uk.gov.hmcts.divorce.solicitor.service.task.SolicitorCourtDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
@RequiredArgsConstructor
public class SolicitorCreateApplicationService {

    private final InitialiseSolicitorCreatedApplication initialiseSolicitorCreatedApplication;

    private final SolicitorCourtDetails solicitorCourtDetails;

    private final DivorceApplicationDraft divorceApplicationDraft;

    private final SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    private final OrganisationClient organisationClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final SetApplicantGender setApplicantGender;

    private final SetApplicantContactDetails setApplicantContactDetails;

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        return caseTasks(
            initialiseSolicitorCreatedApplication,
            setApplicantContactDetails,
            solicitorCourtDetails,
            setApplicant1SolicitorAddress,
            divorceApplicationDraft,
            setApplicantGender
        ).run(caseDetails);
    }

    public CaseInfo validateSolicitorOrganisationAndEmail(
        final Solicitor solicitor,
        final Long caseId,
        final String userAuth
    ) {

        if (solicitor == null || !solicitor.hasOrgId()) {
            log.error("CaseId: {}, the applicant org policy is not populated", caseId);

            return CaseInfo.builder()
                .errors(singletonList("Please select an organisation"))
                .build();
        }

        String solicitorUserOrgId = organisationClient
            .getUserOrganisation(userAuth, authTokenGenerator.generate())
            .getOrganisationIdentifier();

        log.info("Solicitor organisation {} retrieved from Prd Api for case id {} ", solicitorUserOrgId, caseId);

        String solicitorSelectedOrgId =
            solicitor
                .getOrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

        if (!solicitorSelectedOrgId.equalsIgnoreCase(solicitorUserOrgId)) {
            log.error("CaseId: {}, wrong organisation selected {} != {}", caseId, solicitorSelectedOrgId, solicitorUserOrgId);

            return CaseInfo.builder()
                .errors(singletonList("Please select an organisation you belong to"))
                .build();
        }

        boolean validEmail = EmailValidator.getInstance().isValid(solicitor.getEmail());
        if (!validEmail) {
            return CaseInfo.builder()
                .errors(singletonList("You have entered an invalid email address. "
                    + "Please check the email and enter it again, before submitting the application."))
                .build();
        }

        return CaseInfo.builder().build();
    }
}
