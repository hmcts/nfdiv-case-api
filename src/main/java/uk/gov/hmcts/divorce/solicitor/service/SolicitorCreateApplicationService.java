package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.CaseInfo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.service.updater.ClaimsCost;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorCourtDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Service
@Slf4j
public class SolicitorCreateApplicationService {

    @Autowired
    private ClaimsCost claimsCost;

    @Autowired
    private SolicitorCourtDetails solicitorCourtDetails;

    @Autowired
    private MiniApplicationDraft miniApplicationDraft;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    public CaseData aboutToSubmit(
        final CaseData caseData,
        final Long caseId,
        final LocalDate createdDate,
        final String idamAuthToken
    ) {

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            claimsCost,
            solicitorCourtDetails,
            miniApplicationDraft);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .createdDate(createdDate)
            .userAuthToken(idamAuthToken)
            .build();

        return caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();
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
