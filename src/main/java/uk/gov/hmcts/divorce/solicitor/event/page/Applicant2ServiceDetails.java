package uk.gov.hmcts.divorce.solicitor.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_1_SOLICITOR;

@Component
@Slf4j
public class Applicant2ServiceDetails implements CcdPageConfiguration {
    @Autowired
    private OrganisationClient organisationClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private HttpServletRequest request;


    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2ServiceDetails", this::midEvent)
            .pageLabel("Applicant 2 service details")
            .mandatory(CaseData::getApplicant2SolicitorRepresented)
            .optional(CaseData::getApplicant2OrgContactInformation, "applicant2SolicitorRepresented=\"NeverShow\"")
            .mandatory(CaseData::getApplicant2SolicitorName, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApplicant2SolicitorReference, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApplicant2SolicitorPhone, "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApplicant2SolicitorEmail, "applicant2SolicitorRepresented=\"Yes\"")
            .label(
                "LabelApplicant2ServiceDetails-DigitalOrPaper",
                "If applicant 2 solicitor's firm is registered with MyHMCTS, you can assign the case to them. "
                    + "This will allow applicant 2 solicitor to respond digitally. If you cannot find applicant 2 "
                    + "solicitor, a paper AOS pack will be sent to applicant 2's solicitor's address entered above.",
                "applicant2SolicitorRepresented=\"Yes\"")
            .mandatory(CaseData::getApp2SolDigital, "applicant2SolicitorRepresented=\"Yes\"")
            .complex(CaseData::getApplicant2OrganisationPolicy, "app2SolDigital=\"Yes\"")
            .complex(OrganisationPolicy::getOrganisation)
            .mandatory(Organisation::getOrganisationId)
            .done()
            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                "applicant1NameChanged=\"NeverShow\"",
                APPLICANT_1_SOLICITOR)
            .optional(OrganisationPolicy::getOrgPolicyReference, "applicant1NameChanged=\"NeverShow\"")
            .done()
            .optional(CaseData::getApplicant2HomeAddress, "applicant2SolicitorRepresented=\"No\"")
            .mandatory(CaseData::getApplicant2CorrespondenceAddress, "applicant2SolicitorRepresented=\"No\"");
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData caseData = details.getData();

        log.info("Mid-event callback triggered for Applicant2ServiceDetails");

        if (caseData.getApplicant2SolicitorRepresented().toBoolean() && caseData.getApp2SolDigital().toBoolean()) {
            OrganisationsResponse organisationsResponse = organisationClient.getUserOrganisation(
                request.getHeader(AUTHORIZATION),
                authTokenGenerator.generate()
            );

            String solicitorUserOrgId = organisationsResponse.getOrganisationIdentifier();

            log.info("Solicitor organisation {} retrieved from Prd Api for case id {} ", solicitorUserOrgId, details.getId());

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
