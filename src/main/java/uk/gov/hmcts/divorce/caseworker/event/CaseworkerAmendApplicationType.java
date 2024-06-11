package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
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
import static uk.gov.hmcts.divorce.common.event.RegenerateApplication.REGENERATE_APPLICATION;
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
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CcdUpdateService ccdUpdateService;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CASEWORKER_AMEND_APPLICATION_TYPE)
            .forStates(STATES_NOT_WITHDRAWN_OR_REJECTED)
            .name("Amend application type")
            .description("Amend application type")
            .grant(CREATE_READ_UPDATE, SUPER_USER)
            .grantHistoryOnly(LEGAL_ADVISOR, SOLICITOR, CASE_WORKER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Caseworker Amend Application Type about to submit callback invoked for case id: {}", details.getId());
        CaseData caseData = details.getData();
        ApplicantPrayer applicant1Prayer = caseData.getApplicant1().getApplicantPrayer();
        ApplicantPrayer applicant2Prayer = caseData.getApplicant2().getApplicantPrayer();
        Set<DissolveDivorce> dissolveDivorce = Set.of(DissolveDivorce.DISSOLVE_DIVORCE);
        Set<EndCivilPartnership> endCivilPartnership = Set.of(EndCivilPartnership.END_CIVIL_PARTNERSHIP);
        Set<JudicialSeparation> judicialSeparation = Set.of(JudicialSeparation.JUDICIAL_SEPARATION);
        Set<Separation> separation = Set.of(Separation.SEPARATION);

        if (isNull(caseData.getDivorceOrDissolution())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .errors(Collections.singletonList("divorceOrDissolution is null, cannot continue submitting event"))
                .build();
        }

        if (caseData.isJudicialSeparationCase()) {
            if (caseData.isDivorce()) {
                caseData.setSupplementaryCaseType(SEPARATION);
                if (applicant1Prayer.getPrayerJudicialSeparation().equals(judicialSeparation)) {
                    applicant1Prayer.setPrayerSeparation(separation);
                }
                if (applicant2Prayer.getPrayerJudicialSeparation().equals(judicialSeparation)) {
                    applicant2Prayer.setPrayerSeparation(separation);
                }
            } else {
                caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
                if (applicant1Prayer.getPrayerSeparation().equals(separation)) {
                    applicant1Prayer.setPrayerJudicialSeparation(judicialSeparation);
                }
                if (applicant2Prayer.getPrayerSeparation().equals(separation)) {
                    applicant2Prayer.setPrayerJudicialSeparation(judicialSeparation);
                }
            }
        } else {
            if (caseData.isDivorce()) {
                caseData.setDivorceOrDissolution(DISSOLUTION);
                if (applicant1Prayer.getPrayerDissolveDivorce().equals(dissolveDivorce)) {
                    applicant1Prayer.setPrayerEndCivilPartnership(endCivilPartnership);
                }
                if (applicant2Prayer.getPrayerDissolveDivorce().equals(dissolveDivorce)) {
                    applicant2Prayer.setPrayerEndCivilPartnership(endCivilPartnership);
                }
            } else {
                caseData.setDivorceOrDissolution(DIVORCE);
                if (applicant1Prayer.getPrayerEndCivilPartnership().equals(endCivilPartnership)) {
                    applicant1Prayer.setPrayerDissolveDivorce(dissolveDivorce);
                }
                if (applicant2Prayer.getPrayerEndCivilPartnership().equals(endCivilPartnership)) {
                    applicant2Prayer.setPrayerDissolveDivorce(dissolveDivorce);
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
                .submitEvent(details.getId(), REGENERATE_APPLICATION, user, serviceAuth);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
