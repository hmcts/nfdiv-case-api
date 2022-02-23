package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_NOTICE_OF_CHANGE = "caseworker-notice-of-change";
    private static final String NEVER_SHOW = "nocWhichApplicant=\"never\"";

    @Autowired
    private CcdAccessService caseAccessService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_NOTICE_OF_CHANGE)
            .forAllStates()
            .name("Notice of change")
            .description("Change applicant representation")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grant(READ, LEGAL_ADVISOR))
            .page("changeRepresentation-1")
            .pageLabel("Which applicant")
            .complex(CaseData::getNoticeOfChange)
                .mandatory(NoticeOfChange::getWhichApplicant)
                .done()
            .complex(CaseData::getNoticeOfChange)
                .mandatory(NoticeOfChange::getAreTheyRepresented, "nocWhichApplicant=\"applicant1\" OR nocWhichApplicant=\"applicant2\"")
                .mandatory(NoticeOfChange::getAreTheyDigital, "nocAreTheyRepresented=\"Yes\"")
                .done()
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\"")
                    .mandatory(Solicitor::getPhone, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\"")
                    .mandatory(Solicitor::getEmail, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress,
                        "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"No\"")
                    .complex(Solicitor::getOrganisationPolicy,
                        "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation, "nocWhichApplicant=\"applicant1\"")
                            .mandatory(Organisation::getOrganisationId, "nocWhichApplicant=\"applicant1\"")
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_1_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                        .done()
                    .done()
                .mandatory(Applicant::getCorrespondenceAddress, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"No\"")
                .done()
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\"")
                    .mandatory(Solicitor::getPhone, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\"")
                    .mandatory(Solicitor::getEmail, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\"")
                    .mandatory(Solicitor::getAddress,
                        "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"No\"")
                    .complex(Solicitor::getOrganisationPolicy,
                        "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation, "nocWhichApplicant=\"applicant2\"")
                            .mandatory(Organisation::getOrganisationId, "nocWhichApplicant=\"applicant2\"")
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_2_SOLICITOR)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                        .done()
                    .done()
                .mandatory(Applicant::getCorrespondenceAddress, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"No\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker notice of change aboutToSubmit callback started");

        final var data = details.getData();
        final var applicant = data.getNoticeOfChange().getWhichApplicant() == APPLICANT_1
            ? data.getApplicant1()
            : data.getApplicant2();

        if (!data.getNoticeOfChange().getAreTheyRepresented().toBoolean()) {
            applicant.setSolicitor(null);
            applicant.setSolicitorRepresented(NO);
            applicant.setOffline(YES);
        } else if (data.getNoticeOfChange().getAreTheyDigital() == null || !data.getNoticeOfChange().getAreTheyDigital().toBoolean()) {
            applicant.getSolicitor().setOrganisationPolicy(null);
            applicant.setSolicitorRepresented(YES);
            applicant.setOffline(YES);
        } else {
            applicant.setSolicitorRepresented(YES);
            applicant.setOffline(NO);
        }

        final var roles = data.getNoticeOfChange().getWhichApplicant() == APPLICANT_1
            ? List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole())
            : List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());

        caseAccessService.removeUsersWithRole(details.getId(), roles);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(correctRepresentationDetails(data, beforeDetails.getData()))
            .build();
    }

    /** On NOC event, CCD is somehow removing solicitor details for the applicant other than the one selected for NOC.
    * Hence, putting the solicitor details back to the new case details using the before details.
    * */
    private CaseData correctRepresentationDetails(final CaseData data,
                                                  final CaseData beforeData) {
        Applicant applicantToBeCorrected;

        if (data.getNoticeOfChange().getWhichApplicant()
            .equals(APPLICANT_1)) {

            applicantToBeCorrected = data.getApplicant2();

            if (applicantToBeCorrected.isRepresented()) {
                applicantToBeCorrected.setSolicitor(beforeData.getApplicant2().getSolicitor());
            }
        } else {
            applicantToBeCorrected = data.getApplicant1();

            if (applicantToBeCorrected.isRepresented()) {
                applicantToBeCorrected.setSolicitor(beforeData.getApplicant1().getSolicitor());
            }
        }

        return data;
    }
}
