package uk.gov.hmcts.divorce.solicitor.event.page;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;

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
            .label("labelSolAboutTheSol","**Enter the details of ${labelContentApplicantOrApplicant1} solicitor**.")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .mandatoryWithLabel(Solicitor::getName, "Solicitor’s full name")
                    .optionalWithLabel(Solicitor::getReference, "Solicitor reference")
                    .optionalWithLabel(Solicitor::getPhone,  "Solicitor’s direct phone number")
                    .mandatoryWithLabel(Solicitor::getEmail, "Solicitor’s email address")
                    .mandatory(Solicitor::getAgreeToReceiveEmailsCheckbox)
                    .complex(Solicitor::getOrganisationPolicy, null, "Solicitor’s firm address")
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_1_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                    .done()
                .done()
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        log.info("Mid-event callback triggered for SolAboutTheSolicitor");

        CaseData caseData = details.getData();

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisationAndEmail(
            caseData.getApplicant1().getSolicitor(),
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(caseInfo.getErrors())
            .build();
    }
}
