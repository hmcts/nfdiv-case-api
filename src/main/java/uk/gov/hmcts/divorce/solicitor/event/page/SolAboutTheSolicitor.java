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
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreatePetitionService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.UserRole.PETITIONER_SOLICITOR;

@Component
@Slf4j
public class SolAboutTheSolicitor implements CcdPageConfiguration {

    @Autowired
    private SolicitorCreatePetitionService solicitorCreatePetitionService;

    @Autowired
    private HttpServletRequest request;

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("SolAboutTheSolicitor", this::midEvent)
            .pageLabel("About the Solicitor")
            .label(
                "LabelSolAboutEditingApplication-AboutSolicitor",
                "You can make changes at the end of your application.")
            .label(
                "LabelSolAboutTheSolPara-1",
                "Please note that the information provided will be used as evidence by the court to decide if "
                    + "the petitioner is entitled to legally end their marriage. **A copy of this form is sent to the "
                    + "respondent**")
            .mandatory(CaseData::getPetitionerSolicitorName)
            .mandatory(CaseData::getSolicitorReference)
            .mandatory(CaseData::getPetitionerSolicitorPhone)
            .mandatory(CaseData::getPetitionerSolicitorEmail)
            .mandatory(CaseData::getSolicitorAgreeToReceiveEmails)
            .mandatory(CaseData::getDerivedPetitionerSolicitorAddress)
            .complex(CaseData::getPetitionerOrganisationPolicy)
            .complex(OrganisationPolicy::getOrganisation)
            .mandatory(Organisation::getOrganisationId)
            .done()
            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                "petitionerNameChanged=\"NeverShow\"",
                PETITIONER_SOLICITOR)
            .optional(OrganisationPolicy::getOrgPolicyReference, "petitionerNameChanged=\"NeverShow\"");

    }

    // detailsBefore not used for this callback hence suppression
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for SolAboutTheSolicitor");
        return solicitorCreatePetitionService.validateSolicitorOrganisation(
            details.getData(),
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );
    }
}
