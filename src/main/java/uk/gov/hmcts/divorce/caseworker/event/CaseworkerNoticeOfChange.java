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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.citizen.notification.NocCitizenToSolsNotifications;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorValidationService;

import java.util.ArrayList;
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
import static uk.gov.hmcts.divorce.noticeofchange.event.SystemApplyNoticeOfChange.resetConditionalOrderFields;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_NOTICE_OF_CHANGE = "caseworker-notice-of-change";
    private static final String NEVER_SHOW = "nocWhichApplicant=\"never\"";

    private final NoticeOfChangeService noticeOfChangeService;
    private final SolicitorValidationService solicitorValidationService;
    private final ChangeOfRepresentativeService changeOfRepresentativeService;
    private final NocCitizenToSolsNotifications nocCitizenToSolsNotifications;
    private final NotificationDispatcher notificationDispatcher;

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
            .page("changeRepresentation-1", this::midEvent)
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
                    .mandatory(Solicitor::getName, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\"", true)
                    .mandatory(Solicitor::getPhone, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\"", true)
                    .mandatory(Solicitor::getEmail, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\"", true)
                    .mandatory(Solicitor::getAddress,
                        "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"No\"", true)
                    .mandatory(Solicitor::getAddressOverseas,
                        "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"No\"", true)
                    .complex(Solicitor::getOrganisationPolicy,
                        "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation, "nocWhichApplicant=\"applicant1\"")
                            .mandatory(Organisation::getOrganisationId, "nocWhichApplicant=\"applicant1\"", true)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_1_SOLICITOR, true)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW, true)
                        .done()
                    .done()
                .mandatory(Applicant::getAddress, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"No\"", true)
                .mandatory(Applicant::getAddressOverseas, "nocWhichApplicant=\"applicant1\" AND nocAreTheyRepresented=\"No\"", true)
                .done()
            .complex(CaseData::getApplicant2)
                .complex(Applicant::getSolicitor)
                    .mandatory(Solicitor::getName, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\"", true)
                    .mandatory(Solicitor::getPhone, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\"", true)
                    .mandatory(Solicitor::getEmail, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\"", true)
                    .mandatory(Solicitor::getAddress,
                        "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"No\"", true)
                    .mandatory(Solicitor::getAddressOverseas,
                        "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"No\"", true)
                    .complex(Solicitor::getOrganisationPolicy,
                        "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"Yes\" AND nocAreTheyDigital=\"Yes\"")
                        .complex(OrganisationPolicy::getOrganisation, "nocWhichApplicant=\"applicant2\"")
                            .mandatory(Organisation::getOrganisationId, "nocWhichApplicant=\"applicant2\"", true)
                            .done()
                        .optional(OrganisationPolicy::getOrgPolicyCaseAssignedRole, NEVER_SHOW, APPLICANT_2_SOLICITOR, true)
                        .optional(OrganisationPolicy::getOrgPolicyReference, NEVER_SHOW, true)
                        .done()
                    .done()
                .mandatory(Applicant::getAddress, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"No\"", true)
                .mandatory(Applicant::getAddressOverseas, "nocWhichApplicant=\"applicant2\" AND nocAreTheyRepresented=\"No\"", true)
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> detailsBefore
    ) {
        CaseData data = details.getData();
        List<String> errors = new ArrayList<>();

        if (data.getNoticeOfChange().isNotAddingNewDigitalSolicitor()) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
        }

        final boolean isApplicant1 = data.getNoticeOfChange().getWhichApplicant() == APPLICANT_1;
        final Applicant applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        String email = applicant.getSolicitor().getEmail();
        String orgId = applicant.getSolicitor().getOrganisationPolicy().getOrganisation().getOrganisationId();

        if (email == null) {
            errors.add("No email provided - please provide an email for the solicitor you wish to add");
        } else {
            errors = solicitorValidationService.validateEmailBelongsToOrgUser(email, details.getId(), orgId);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(errors)
                .data(data)
                .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("About to start submitting Notice of Change for {} on Case Id: {}",
            details.getData().getNoticeOfChange().getWhichApplicant(), details.getId());

        final var data = details.getData();
        final var beforeData = beforeDetails.getData();

        final boolean isApplicant1 = data.getNoticeOfChange().getWhichApplicant() == APPLICANT_1;
        final var orgPolicyCaseAssignedRole = isApplicant1 ? APPLICANT_1_SOLICITOR : APPLICANT_2_SOLICITOR;

        final var applicant = isApplicant1 ? data.getApplicant1() : data.getApplicant2();
        final var beforeApplicant = isApplicant1 ? beforeData.getApplicant1() : beforeData.getApplicant2();

        updateSolicitorInformation(data, orgPolicyCaseAssignedRole, applicant);

        final var roles = isApplicant1
            ? List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole())
            : List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());

        NoticeType noticeType = calculateNoticeType(applicant, beforeApplicant);

        noticeType.applyNoticeOfChange(applicant,
            beforeApplicant,
            roles,
            orgPolicyCaseAssignedRole.getRole(),
            details,
            noticeOfChangeService);

        changeOfRepresentativeService.buildChangeOfRepresentative(data, beforeData,
                ChangeOfRepresentationAuthor.CASEWORKER_NOTICE_OF_CHANGE.getValue(), isApplicant1);


        //could get which applicant from case data but use param to avoid mishap
        //this can move to submitted once we have more NOC data on casedata
        notificationDispatcher.sendNOC(nocCitizenToSolsNotifications, details.getData(),
            beforeData, details.getId(), isApplicant1, noticeType);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(correctRepresentationDetails(details.getData(), beforeData))
            .build();
    }

    private NoticeType calculateNoticeType(Applicant applicant, Applicant beforeApplicant) {
        Solicitor beforeSolicitor = beforeApplicant.getSolicitor();
        Solicitor afterSolicitor = applicant.getSolicitor();

        String beforeOrgID = beforeSolicitor.getOrganisationId();
        String afterOrgID = afterSolicitor.getOrganisationId();

        boolean hadOrgBefore = beforeOrgID != null;
        boolean hasOrgAfter = afterOrgID != null;

        if (!hadOrgBefore) {
            return hasOrgAfter ? NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG : NoticeType.OFFLINE_NOC;
        }

        if (!hasOrgAfter) {
            return NoticeType.ORG_REMOVED;
        }

        if (!beforeOrgID.equals(afterOrgID)) {
            return NoticeType.NEW_DIGITAL_SOLICITOR_NEW_ORG;
        }

        //if email and org is the same as pre-event then it means the user has probably used the event to update contact details
        //erroneously and not change case access, doing this check ensures that we don't actually alter the case access
        boolean solEmailHasChanged = !afterSolicitor.getEmail().equals(beforeSolicitor.getEmail());
        return solEmailHasChanged ? NoticeType.NEW_DIGITAL_SOLICITOR_EXISTING_ORG : NoticeType.OFFLINE_NOC;
    }

    private void updateSolicitorInformation(CaseData data, UserRole orgPolicyCaseAssignedRole, Applicant applicant) {
        if (!YesOrNo.YES.equals(data.getNoticeOfChange().getAreTheyRepresented())) {
            Solicitor solicitor = solicitorWithDefaultOrganisationPolicy(new Solicitor(), orgPolicyCaseAssignedRole);
            applicant.setSolicitor(solicitor);
            applicant.setSolicitorRepresented(NO);
            applicant.setOffline(YES);
        } else if (!YesOrNo.YES.equals(data.getNoticeOfChange().getAreTheyDigital())) {
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
    private CaseData correctRepresentationDetails(final CaseData data, final CaseData beforeData) {
        if (beforeData == null) {
            return data;
        }

        if (data.getNoticeOfChange().getWhichApplicant().equals(APPLICANT_1)) {
            safelySetOrganisationPolicy(data.getApplicant2(), beforeData.getApplicant2());
            safelySetAddress(data.getApplicant2(), beforeData.getApplicant2());
            if (YES.equals(data.getNoticeOfChange().getAreTheyRepresented())) {
                safelySetAddress(data.getApplicant1(), beforeData.getApplicant1());
            }

            setSolicitorFirmName(data.getApplicant1());
        } else {
            safelySetOrganisationPolicy(data.getApplicant1(), beforeData.getApplicant1());
            safelySetAddress(data.getApplicant1(), beforeData.getApplicant1());
            if (YES.equals(data.getNoticeOfChange().getAreTheyRepresented())) {
                safelySetAddress(data.getApplicant2(), beforeData.getApplicant2());
            }

            setSolicitorFirmName(data.getApplicant2());
        }

        if (YES.equals(data.getNoticeOfChange().getAreTheyRepresented())) {
            resetConditionalOrderFields(data);
        }

        return data;
    }

    private void setSolicitorFirmName(Applicant applicant) {
        Solicitor applicantSolicitor = applicant.getSolicitor();
        if (applicantSolicitor != null && applicantSolicitor.hasOrgName()) {
            String orgName = applicantSolicitor.getOrganisationPolicy().getOrganisation().getOrganisationName();
            applicantSolicitor.setFirmName(orgName);
        }
    }

    private void safelySetOrganisationPolicy(Applicant target, Applicant source) {
        if (target != null && target.getSolicitor() != null && source != null && source.getSolicitor() != null) {
            OrganisationPolicy<UserRole> orgPolicy = source.getSolicitor().getOrganisationPolicy();
            if (orgPolicy != null) {
                target.getSolicitor().setOrganisationPolicy(orgPolicy);
            }
        }
    }

    private void safelySetAddress(Applicant target, Applicant source) {
        if (target != null && source != null) {
            target.setAddress(source.getAddress());
        }
    }

    private Solicitor solicitorWithDefaultOrganisationPolicy(Solicitor solicitor, UserRole role) {
        OrganisationPolicy<UserRole> defaultOrgPolicy = OrganisationPolicy.<UserRole>builder()
            .orgPolicyCaseAssignedRole(role)
            .organisation(new Organisation(null, null))
            .build();

        solicitor.setOrganisationPolicy(defaultOrgPolicy);
        return solicitor;
    }
}
