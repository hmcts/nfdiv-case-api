package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAmendApplicationType.CASEWORKER_AMEND_APPLICATION_TYPE;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerAmendApplicationTypeTest {

    private static final Set<ApplicantPrayer.DissolveDivorce> DISSOLVE_DIVORCE_SET =
        Set.of(ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE);
    private static final Set<ApplicantPrayer.EndCivilPartnership> END_CIVIL_PARTNERSHIP_SET =
        Set.of(ApplicantPrayer.EndCivilPartnership.END_CIVIL_PARTNERSHIP);
    private static final Set<ApplicantPrayer.JudicialSeparation> JUDICIAL_SEPARATION_SET =
        Set.of(ApplicantPrayer.JudicialSeparation.JUDICIAL_SEPARATION);
    private static final Set<ApplicantPrayer.Separation> SEPARATION_SET =
        Set.of(ApplicantPrayer.Separation.SEPARATION);

    @InjectMocks
    private CaseworkerAmendApplicationType caseworkerAmendApplicationType;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerAmendApplicationType.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_AMEND_APPLICATION_TYPE);
    }

    @Test
    void shouldSetApplicationTypeAndPrayerFromDivorceToDissolutionOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.setApplicant1(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant1().getApplicantPrayer().setPrayerDissolveDivorce(DISSOLVE_DIVORCE_SET);
        caseData.setApplicant2(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant2().getApplicantPrayer().setPrayerDissolveDivorce(DISSOLVE_DIVORCE_SET);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertThat(response.getData().getDivorceOrDissolution().equals(DISSOLUTION));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("civil partnership"));
        assertThat(response.getData().getApplicant1().getApplicantPrayer()
            .getPrayerEndCivilPartnership().equals(END_CIVIL_PARTNERSHIP_SET));
        assertThat(response.getData().getApplicant2().getApplicantPrayer()
            .getPrayerEndCivilPartnership().equals(END_CIVIL_PARTNERSHIP_SET));
    }

    @Test
    void shouldSetApplicationTypeButNotPrayerFromDivorceToDissolutionOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setDivorceOrDissolution(DIVORCE);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertThat(response.getData().getDivorceOrDissolution().equals(DISSOLUTION));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("civil partnership"));
        assertNull(response.getData().getApplicant1().getApplicantPrayer().getPrayerEndCivilPartnership());
        assertNull(response.getData().getApplicant2().getApplicantPrayer().getPrayerEndCivilPartnership());
    }

    @Test
    void shouldSetApplicationTypeAndPrayerFromJudicialSeparationToSeparationOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.setApplicant1(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant1().getApplicantPrayer().setPrayerJudicialSeparation(JUDICIAL_SEPARATION_SET);
        caseData.setApplicant2(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant2().getApplicantPrayer().setPrayerJudicialSeparation(JUDICIAL_SEPARATION_SET);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertThat(response.getData().getDivorceOrDissolution().equals(DISSOLUTION));
        assertThat(response.getData().getSupplementaryCaseType().equals(SEPARATION));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("civil partnership"));
        assertThat(response.getData().getApplicant1().getApplicantPrayer().getPrayerSeparation().equals(SEPARATION_SET));
        assertThat(response.getData().getApplicant2().getApplicantPrayer().getPrayerSeparation().equals(SEPARATION_SET));
    }

    @Test
    void shouldSetApplicationTypeButNotPrayerFromJudicialSeparationToSeparationOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertThat(response.getData().getDivorceOrDissolution().equals(DISSOLUTION));
        assertThat(response.getData().getSupplementaryCaseType().equals(SEPARATION));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("civil partnership"));
        assertNull(response.getData().getApplicant1().getApplicantPrayer().getPrayerSeparation());
        assertNull(response.getData().getApplicant2().getApplicantPrayer().getPrayerSeparation());
    }

    @Test
    void shouldSetApplicationTypeAndPrayerFromDissolutionToDivorceOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        caseData.setApplicant1(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant1().getApplicantPrayer().setPrayerEndCivilPartnership(END_CIVIL_PARTNERSHIP_SET);
        caseData.setApplicant2(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant2().getApplicantPrayer().setPrayerEndCivilPartnership(END_CIVIL_PARTNERSHIP_SET);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertNotNull(response.getData().getDivorceOrDissolution());
        assertThat(response.getData().getDivorceOrDissolution().equals(DIVORCE));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("marriage"));
        assertThat(response.getData().getApplicant1().getApplicantPrayer().getPrayerDissolveDivorce().equals(DISSOLVE_DIVORCE_SET));
        assertThat(response.getData().getApplicant2().getApplicantPrayer().getPrayerDissolveDivorce().equals(DISSOLVE_DIVORCE_SET));
    }

    @Test
    void shouldSetApplicationTypeButNotPrayerFromDissolutionToDivorceOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertNotNull(response.getData().getDivorceOrDissolution());
        assertThat(response.getData().getDivorceOrDissolution().equals(DIVORCE));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("marriage"));
        assertNull(response.getData().getApplicant1().getApplicantPrayer().getPrayerDissolveDivorce());
        assertNull(response.getData().getApplicant2().getApplicantPrayer().getPrayerDissolveDivorce());
    }

    @Test
    void shouldSetApplicationTypeAndPrayerFromSeparationToJudicialSeparationOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setSupplementaryCaseType(SEPARATION);
        caseData.setApplicant1(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant1().getApplicantPrayer().setPrayerSeparation(SEPARATION_SET);
        caseData.setApplicant2(Applicant.builder().applicantPrayer(new ApplicantPrayer()).build());
        caseData.getApplicant2().getApplicantPrayer().setPrayerSeparation(SEPARATION_SET);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertThat(response.getData().getDivorceOrDissolution().equals(DIVORCE));
        assertThat(response.getData().getSupplementaryCaseType().equals(JUDICIAL_SEPARATION));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("marriage"));
        assertThat(response.getData().getApplicant1().getApplicantPrayer().getPrayerJudicialSeparation().equals(JUDICIAL_SEPARATION_SET));
        assertThat(response.getData().getApplicant2().getApplicantPrayer().getPrayerJudicialSeparation().equals(JUDICIAL_SEPARATION_SET));
    }

    @Test
    void shouldSetApplicationTypeButNotPrayerFromSeparationToJudicialSeparationOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setSupplementaryCaseType(SEPARATION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertThat(response.getData().getDivorceOrDissolution().equals(DIVORCE));
        assertThat(response.getData().getSupplementaryCaseType().equals(JUDICIAL_SEPARATION));
        assertThat(response.getData().getLabelContent().getMarriageOrCivilPartnership().equals("marriage"));
        assertNull(response.getData().getApplicant1().getApplicantPrayer().getPrayerJudicialSeparation());
        assertNull(response.getData().getApplicant2().getApplicantPrayer().getPrayerJudicialSeparation());
    }

    @Test
    void shouldSetApplicationTypeToNullOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getData().getDivorceOrDissolution());
        assertThat(response.getErrors()).contains("divorceOrDissolution is null, cannot continue submitting event");
    }

    @Test
    void shouldSetApplicationTypeToValidGivenNotNullOnAboutToSubmit() {
        final CaseData caseData = CaseData.builder().build();
        caseData.setDivorceOrDissolution(DISSOLUTION);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerAmendApplicationType
            .aboutToSubmit(caseDetails, caseDetails);

        assertNull(response.getErrors());
        assertNotNull(response.getData().getDivorceOrDissolution());
        assertThat(response.getData().getDivorceOrDissolution().equals(DIVORCE)
            || response.getData().getDivorceOrDissolution().equals(DISSOLUTION));
    }
}
