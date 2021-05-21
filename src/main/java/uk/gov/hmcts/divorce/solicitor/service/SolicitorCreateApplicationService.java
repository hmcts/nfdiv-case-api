package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.service.updater.ClaimsCost;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationDraft;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorCourtDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.springframework.util.CollectionUtils.firstElement;

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


    public AboutToStartOrSubmitResponse<CaseData, State> validateSolicitorOrganisation(
        final CaseData caseData,
        final Long caseId,
        final String userAuth
    ) {
        if (!caseData.hasApplicant1OrgId()) {
            log.error("CaseId: {}, Applicant 1 org policy is not populated", caseId);
            return AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("Please select an organisation"))
                .build();
        }

        String solicitorUserOrgId = organisationClient
            .getUserOrganisation(userAuth, authTokenGenerator.generate())
            .getOrganisationIdentifier();

        log.info("Solicitor organisation {} retrieved from Prd Api for case id {} ", solicitorUserOrgId, caseId);

        String solicitorSelectedOrgId =
            caseData
                .getApplicant1OrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

        if (!solicitorSelectedOrgId.equalsIgnoreCase(solicitorUserOrgId)) {
            log.error("CaseId: {}, wrong organisation selected {} != {}", caseId, solicitorSelectedOrgId, solicitorUserOrgId);
            return AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .data(caseData)
                .errors(singletonList("Please select an organisation you belong to"))
                .build();
        }

        return AboutToStartOrSubmitResponse
            .<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> setApplicant2SolOrganisationInfo(
        final CaseData caseData,
        final Long caseId,
        final String userAuth
    ) {
        if (caseData.getApplicant2SolicitorRepresented().toBoolean() && caseData.getApp2SolDigital().toBoolean()) {
            OrganisationsResponse organisationsResponse = organisationClient.getUserOrganisation(userAuth, authTokenGenerator.generate());

            String solicitorUserOrgId = organisationsResponse.getOrganisationIdentifier();

            log.info("Solicitor organisation {} retrieved from Prd Api for case id {} ", solicitorUserOrgId, caseId);

            caseData.setApplicant2OrgContactInformation(firstElement(organisationsResponse.getContactInformation()));

            return AboutToStartOrSubmitResponse
                .<CaseData, State>builder()
                .data(caseData)
                .build();
        }
        return AboutToStartOrSubmitResponse
            .<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
