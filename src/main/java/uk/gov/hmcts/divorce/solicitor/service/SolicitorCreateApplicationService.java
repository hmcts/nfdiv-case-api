package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.service.task.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.task.SolicitorCourtDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorCreateApplicationService {

    @Autowired
    private SolicitorCourtDetails solicitorCourtDetails;

    @Autowired
    private MiniApplicationDraft miniApplicationDraft;

    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        return caseTasks(
            solicitorCourtDetails,
            miniApplicationDraft
        ).run(caseDetails);
    }

    public CaseInfo validateSolicitorOrganisation(
        final CaseData caseData,
        final Long caseId,
        final String userAuth
    ) {

        if (caseData.getApplicant1().getSolicitor() == null || !caseData.getApplicant1().getSolicitor().hasOrgId()) {
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
            caseData
                .getApplicant1()
                .getSolicitor()
                .getOrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

        if (!solicitorSelectedOrgId.equalsIgnoreCase(solicitorUserOrgId)) {
            log.error("CaseId: {}, wrong organisation selected {} != {}", caseId, solicitorSelectedOrgId, solicitorUserOrgId);

            return CaseInfo.builder()
                .errors(singletonList("Please select an organisation you belong to"))
                .build();
        }

        return CaseInfo.builder().build();
    }
}
