package uk.gov.hmcts.divorce.solicitor.event.page;

import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.JOINT_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLE_APPLICATION_CONDITION;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_JOINT_PREVIEW_BANNER;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

public class Applicant2ServiceDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {

        pageBuilder
            .page("Applicant2ServiceDetails")
            .pageLabel("The respondent service details")
            .label(
                "LabelNFDBanner-Applicant2ServiceDetails",
                SOLICITOR_NFD_PREVIEW_BANNER,
                SOLE_APPLICATION_CONDITION)
            .label(
                "LabelNFDJointBanner-Applicant2ServiceDetails",
                SOLICITOR_NFD_JOINT_PREVIEW_BANNER,
                JOINT_APPLICATION_CONDITION)
            .complex(CaseData::getApplicant2)
                .mandatory(Applicant::getSolicitorRepresented, null, null, "Is the respondent represented by a solicitor?")
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getReference, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getPhone, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getEmail, "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress, "applicant2SolicitorRepresented=\"Yes\"")
                    .label(
                        "LabelApplicant2ServiceDetails-DigitalOrPaper",
                        "If the respondent's solicitor's firm is registered with MyHMCTS, you can assign the case to them. "
                            + "This will allow the respondent's solicitor to respond digitally. If you cannot find the respondent's "
                            + "solicitor, a paper AOS pack will be sent to the respondent's solicitor's address entered above.",
                        "applicant2SolicitorRepresented=\"Yes\"")
                    .mandatory(Solicitor::getIsDigital, "applicant2SolicitorRepresented=\"Yes\"")
                    .complex(Solicitor::getOrganisationPolicy, "applicant2SolicitorIsDigital=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation)
                            .mandatory(Organisation::getOrganisationId)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole,
                            "applicant1NameChanged=\"NeverShow\"",
                            APPLICANT_2_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, "applicant1NameChanged=\"NeverShow\"")
                        .done()
                    .done()
                .optional(Applicant::getHomeAddress, "applicant2SolicitorRepresented=\"No\"")
                .mandatory(Applicant::getCorrespondenceAddress, "applicant2SolicitorRepresented=\"No\"")
            .done();
    }
}
