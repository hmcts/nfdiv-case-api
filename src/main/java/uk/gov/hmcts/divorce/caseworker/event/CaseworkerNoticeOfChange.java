package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_NOTICE_OF_CHANGE = "caseworker-notice-of-change";
    private static final String NEVER_SHOW = "nocWhichApplicant=\"never\"";

    private final NoticeOfChangeService noticeOfChangeService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_NOTICE_OF_CHANGE)
            .forStates(POST_SUBMISSION_STATES)
            .name("Notice of change")
            .description("Change applicant representation")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, CASE_WORKER, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, JUDGE))
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
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_1_SOLICITOR, true)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                        .done()
                    .done()
                .mandatory(Applicant::getAddress, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"No\"")
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
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_2_SOLICITOR, true)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW)
                        .done()
                    .done()
                .mandatory(Applicant::getAddress, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"No\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("About to start submitting Notice of Change");

        final var data = details.getData();
        final var beforeData = beforeDetails.getData();

        final boolean isApplicant1 = data.getNoticeOfChange().getWhichApplicant() == APPLICANT_1;
        final var orgPolicyCaseAssignedRole = isApplicant1 ? APPLICANT_1_SOLICITOR : APPLICANT_2_SOLICITOR;

        final var applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final var beforeApplicant = isApplicant1 ? beforeData.getApplicant1() : beforeData.getApplicant2();

        updateSolicitorInformation(data, orgPolicyCaseAssignedRole, applicant);

        final var roles = data.getNoticeOfChange().getWhichApplicant() == APPLICANT_1
            ? List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole())
            : List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());

        NoticeType noticeType = calculateNoticeType(applicant, beforeApplicant);

        switch (noticeType) {
            case NEW_DIGITAL_SOLICITOR_NEW_ORG -> noticeOfChangeService.applyNocDecisionAndGrantAccessToNewSol(
                details.getId(),
                applicant,
                beforeApplicant,
                roles,
                orgPolicyCaseAssignedRole.getRole());
            case ORG_REMOVED -> noticeOfChangeService.revokeCaseAccess(
                details.getId(),
                beforeApplicant,
                roles);
            case NEW_DIGITAL_SOLICITOR_EXISTING_ORG -> noticeOfChangeService.changeAccessWithinOrganisation(
                applicant.getSolicitor(),
                roles,
                orgPolicyCaseAssignedRole.getRole(),
                details.getId());
            default -> {

            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(correctRepresentationDetails(details.getData(), beforeDetails.getData()))
            .build();
    }

    private NoticeType calculateNoticeType(Applicant applicant,
                                           Applicant beforeApplicant) {
        OrganisationPolicy<UserRole> orgPolicy = applicant.getSolicitor().getOrganisationPolicy();
        OrganisationPolicy<UserRole> beforeOrgPolicy = beforeApplicant.getSolicitor().getOrganisationPolicy();

        if (beforeOrgPolicy.getOrganisation() != null && orgPolicy.getOrganisation() == null) {
            return NoticeType.ORG_REMOVED;
        } else if (orgPolicy.getOrganisation() != null && orgPolicy.getOrganisation().equals(beforeOrgPolicy.getOrganisation())) {
            return NoticeType.NEW_DIGITAL_SOLICITOR_EXISTING_ORG;
        } else if (orgPolicy.getOrganisation() != null && !orgPolicy.getOrganisation().equals(beforeOrgPolicy.getOrganisation())) {
            return NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;
        }

        return NoticeType.OFFLINE_NOC;
    }

    private void updateSolicitorInformation(CaseData data, UserRole orgPolicyCaseAssignedRole, Applicant applicant) {
        if (!data.getNoticeOfChange().getAreTheyRepresented().toBoolean()) {
            Solicitor solicitor = solicitorWithDefaultOrganisationPolicy(new Solicitor(), orgPolicyCaseAssignedRole);
            applicant.setSolicitor(solicitor);
            applicant.setSolicitorRepresented(NO);
            applicant.setOffline(YES);
        } else if (data.getNoticeOfChange().getAreTheyDigital() == null || !data.getNoticeOfChange().getAreTheyDigital().toBoolean()) {
            Solicitor solicitor = solicitorWithDefaultOrganisationPolicy(applicant.getSolicitor(), orgPolicyCaseAssignedRole);
            applicant.setSolicitor(solicitor);
            applicant.setSolicitorRepresented(YES);
            applicant.setOffline(YES);
        } else {
            applicant.setSolicitorRepresented(YES);
            applicant.setOffline(NO);
        }
    }

    /** On NOC event, CCD is somehow removing solicitor details for the applicant other than the one selected for NOC.
    * Hence, putting the solicitor details back to the new case details using the before details.
    * */
    private CaseData correctRepresentationDetails(final CaseData data,
                                                  final CaseData beforeData) {

        if (data.getNoticeOfChange().getWhichApplicant().equals(APPLICANT_1)) {
            data.getApplicant2().setSolicitor(beforeData.getApplicant2().getSolicitor());
            data.getApplicant2().setAddress(beforeData.getApplicant2().getAddress());
        } else {
            data.getApplicant1().setSolicitor(beforeData.getApplicant1().getSolicitor());
            data.getApplicant1().setAddress(beforeData.getApplicant1().getAddress());
        }

        if (YES.equals(data.getNoticeOfChange().getAreTheyRepresented())) {
            setConditionalOrderDefaultValues(data);
        }

        return data;
    }

    private void setConditionalOrderDefaultValues(CaseData data) {
        data.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsSubmitted(NO);
        data.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsDrafted(NO);
        if (!data.getApplicationType().isSole()) {
            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsSubmitted(NO);
            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsDrafted(NO);
        }
    }

    private Solicitor solicitorWithDefaultOrganisationPolicy(Solicitor solicitor, UserRole role) {
        OrganisationPolicy<UserRole> defaultOrgPolicy = OrganisationPolicy.<UserRole>builder()
            .orgPolicyCaseAssignedRole(role)
            .build();

        solicitor.setOrganisationPolicy(defaultOrgPolicy);
        return solicitor;
    }

    private enum NoticeType {
        NEW_DIGITAL_SOLICITOR_NEW_ORG,
        NEW_DIGITAL_SOLICITOR_EXISTING_ORG,
        ORG_REMOVED,
        OFFLINE_NOC

    }
}
