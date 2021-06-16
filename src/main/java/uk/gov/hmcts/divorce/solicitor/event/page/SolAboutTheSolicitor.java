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
import uk.gov.hmcts.divorce.common.CaseInfo;
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.Solicitor;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

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
                "LabelNFDBanner-AboutSolicitor",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-AboutSolicitor",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .label(
                "LabelSolAboutTheSolPara-1",
                "Please note that the information provided will be used as evidence by the court to decide if "
                    + "the applicant is entitled to legally end their marriage. **A copy of this form is sent to "
                    + "the respondent**")
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, null, null, "Your name")
                    .mandatory(Solicitor::getReference, null, null, "Your reference")
                    .mandatory(Solicitor::getPhone, null, null, "Your phone number")
                    .mandatory(Solicitor::getEmail, null, null, "Your email address")
                    .mandatory(Solicitor::getAgreeToReceiveEmails)
                    .complex(Solicitor::getOrganisationPolicy, null, "Your firm's address or DX number")
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                            "applicant1NameChanged=\"NeverShow\"",
                            APPLICANT_1_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, "applicant1NameChanged=\"NeverShow\"")
                    .done()
                .done()
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {
        log.info("Mid-event callback triggered for SolAboutTheSolicitor");

        final CaseInfo caseInfo = solicitorCreateApplicationService.validateSolicitorOrganisation(
            details.getData(),
            details.getId(),
            request.getHeader(AUTHORIZATION)
        );

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .errors(caseInfo.getErrors())
            .build();
    }
}
