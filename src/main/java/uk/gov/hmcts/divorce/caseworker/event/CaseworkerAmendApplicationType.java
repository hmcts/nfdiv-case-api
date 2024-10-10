package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.EndCivilPartnership;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.JudicialSeparation;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.Separation;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.util.Collections;
import java.util.Set;

import static java.util.Objects.isNull;
import static uk.gov.hmcts.divorce.common.event.RegenerateApplicationDocument.REGENERATE_APPLICATION;
import static uk.gov.hmcts.divorce.common.event.RegenerateNoticeOfProceedings.REGENERATE_NOTICE_OF_PROCEEDINGS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.STATES_NOT_WITHDRAWN_OR_REJECTED;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseworkerAmendApplicationType implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_AMEND_APPLICATION_TYPE = "caseworker-amend-application-type";
    private static final Set<DissolveDivorce> DISSOLVE_DIVORCE_SET = Set.of(DissolveDivorce.DISSOLVE_DIVORCE);
    private static final Set<EndCivilPartnership> END_CIVIL_PARTNERSHIP_SET = Set.of(EndCivilPartnership.END_CIVIL_PARTNERSHIP);
    private static final Set<JudicialSeparation> JUDICIAL_SEPARATION_SET = Set.of(JudicialSeparation.JUDICIAL_SEPARATION);
    private static final Set<Separation> SEPARATION_SET = Set.of(Separation.SEPARATION);
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_AMEND_APPLICATION_TYPE)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("Amend application type")
            .description("Amend application type")
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted))
            .page("Amend application type")
            .label("prayerChangeLabel","Completing this event will change the prayer on the case. "
                + "Please make sure that you have confirmed this with the Applicant before proceeding.");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker Amend Application Type about to submit callback invoked for case id: {}", details.getId());
        CaseData caseData = details.getData();
        ApplicantPrayer app1Prayer = caseData.getApplicant1().getApplicantPrayer();
        ApplicantPrayer app2Prayer = caseData.getApplicant2().getApplicantPrayer();

        if (isNull(caseData.getDivorceOrDissolution())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList("divorceOrDissolution is null, cannot continue submitting event"))
                .build();
        }

        if (caseData.isJudicialSeparationCase()) {
            if (caseData.isDivorce()) {
                caseData.setSupplementaryCaseType(SEPARATION);
                if (JUDICIAL_SEPARATION_SET.equals(app1Prayer.getPrayerJudicialSeparation())) {
                    app1Prayer.setPrayerSeparation(SEPARATION_SET);
                }
                if (JUDICIAL_SEPARATION_SET.equals(app2Prayer.getPrayerJudicialSeparation())) {
                    app2Prayer.setPrayerSeparation(SEPARATION_SET);
                }
            } else {
                caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
                if (SEPARATION_SET.equals(app1Prayer.getPrayerSeparation())) {
                    app1Prayer.setPrayerJudicialSeparation(JUDICIAL_SEPARATION_SET);
                }
                if (SEPARATION_SET.equals(app2Prayer.getPrayerSeparation())) {
                    app2Prayer.setPrayerJudicialSeparation(JUDICIAL_SEPARATION_SET);
                }
            }
        } else {
            if (caseData.isDivorce()) {
                caseData.setDivorceOrDissolution(DISSOLUTION);
                if (DISSOLVE_DIVORCE_SET.equals(app1Prayer.getPrayerDissolveDivorce())) {
                    app1Prayer.setPrayerEndCivilPartnership(END_CIVIL_PARTNERSHIP_SET);
                }
                if (DISSOLVE_DIVORCE_SET.equals(app2Prayer.getPrayerDissolveDivorce())) {
                    app2Prayer.setPrayerEndCivilPartnership(END_CIVIL_PARTNERSHIP_SET);
                }
            } else {
                caseData.setDivorceOrDissolution(DIVORCE);
                if (END_CIVIL_PARTNERSHIP_SET.equals(app1Prayer.getPrayerEndCivilPartnership())) {
                    app1Prayer.setPrayerDissolveDivorce(DISSOLVE_DIVORCE_SET);
                }
                if (END_CIVIL_PARTNERSHIP_SET.equals(app2Prayer.getPrayerEndCivilPartnership())) {
                    app2Prayer.setPrayerDissolveDivorce(DISSOLVE_DIVORCE_SET);
                }
            }
        }
        caseData.getLabelContent().setUnionType(caseData.getDivorceOrDissolution());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {
        log.info("{} submitted callback invoked for case id: {}", CASEWORKER_AMEND_APPLICATION_TYPE, details.getId());

        if (null != details.getData().getApplication().getIssueDate()) {
            final User user = idamService.retrieveSystemUpdateUserDetails();
            final String serviceAuth = authTokenGenerator.generate();

            ccdUpdateService
                .submitEvent(details.getId(), REGENERATE_NOTICE_OF_PROCEEDINGS, user, serviceAuth);
            ccdUpdateService
                .submitEvent(details.getId(), REGENERATE_APPLICATION, user, serviceAuth);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
