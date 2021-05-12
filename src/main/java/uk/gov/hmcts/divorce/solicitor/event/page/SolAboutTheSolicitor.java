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
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_1_SOLICITOR;

@Component
@Slf4j
public class SolAboutTheSolicitor implements CcdPageConfiguration {

    @Autowired
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

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
                    + "applicant 1 is entitled to legally end their marriage. **A copy of this form is sent to the "
                    + "applicant 2**")
            .mandatory(CaseData::getApplicant1SolicitorName)
            .mandatory(CaseData::getSolicitorReference)
            .mandatory(CaseData::getApplicant1SolicitorPhone)
            .mandatory(CaseData::getApplicant1SolicitorEmail)
            .mandatory(CaseData::getSolicitorAgreeToReceiveEmails)
            .mandatory(CaseData::getDerivedApplicant1SolicitorAddress)
            .complex(CaseData::getApplicant1OrganisationPolicy)
            .complex(OrganisationPolicy::getOrganisation)
            .mandatory(Organisation::getOrganisationId)
            .done()
            .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                "petitionerNameChanged=\"NeverShow\"",
                APPLICANT_1_SOLICITOR)
            .optional(OrganisationPolicy::getOrgPolicyReference, "applicant1NameChanged=\"NeverShow\"");

    }

    // detailsBefore not used for this callback hence suppression
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for SolAboutTheSolicitor");
        return solicitorCreateApplicationService.validateSolicitorOrganisation(
            details.getData(),
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );
    }
}
