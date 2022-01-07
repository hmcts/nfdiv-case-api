package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.FEMALE;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.OPPOSITE_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.MarriageFormation.SAME_SEX_COUPLE;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.divorce.divorcecase.model.WhoDivorcing.WIFE;

@ExtendWith(MockitoExtension.class)
class SetApplicantGenderTest {
    private final SetApplicantGender task = new SetApplicantGender();

    @Test
    void shouldSetGenderForADivorce() {
        var data = createCaseData(DIVORCE, null, WIFE, OPPOSITE_SEX_COUPLE);
        var details = CaseDetails.<CaseData, State>builder().data(data).build();
        var result = task.apply(details).getData();

        assertEquals(result.getApplicant1().getGender(), MALE);
        assertEquals(result.getApplicant2().getGender(), FEMALE);
        assertEquals(result.getApplication().getDivorceWho(), WIFE);
    }

    @Test
    void shouldSetGenderForASameSexDivorce() {
        var data = createCaseData(DIVORCE, null, HUSBAND, SAME_SEX_COUPLE);
        var details = CaseDetails.<CaseData, State>builder().data(data).build();
        var result = task.apply(details).getData();

        assertEquals(result.getApplicant1().getGender(), MALE);
        assertEquals(result.getApplicant2().getGender(), MALE);
        assertEquals(result.getApplication().getDivorceWho(), HUSBAND);
    }

    @Test
    void shouldSetGenderForADissolution() {
        var data = createCaseData(DISSOLUTION, MALE, null, OPPOSITE_SEX_COUPLE);
        var details = CaseDetails.<CaseData, State>builder().data(data).build();
        var result = task.apply(details).getData();

        assertEquals(result.getApplicant1().getGender(), MALE);
        assertEquals(result.getApplicant2().getGender(), FEMALE);
        assertEquals(result.getApplication().getDivorceWho(), WIFE);
    }

    @Test
    void shouldSetGenderForASameSexDissolution() {
        var data = createCaseData(DISSOLUTION, FEMALE, null, SAME_SEX_COUPLE);
        var details = CaseDetails.<CaseData, State>builder().data(data).build();
        var result = task.apply(details).getData();

        assertEquals(result.getApplicant1().getGender(), FEMALE);
        assertEquals(result.getApplicant2().getGender(), FEMALE);
        assertEquals(result.getApplication().getDivorceWho(), WIFE);
    }

    private CaseData createCaseData(final DivorceOrDissolution type,
                                    final Gender app1Gender,
                                    final WhoDivorcing whoDivorcing,
                                    final MarriageFormation formation) {
        return CaseData.builder()
            .divorceOrDissolution(type)
            .applicant1(
                Applicant.builder()
                    .gender(app1Gender)
                    .build()
            )
            .application(Application.builder()
                .divorceWho(whoDivorcing)
                .marriageDetails(
                    MarriageDetails.builder()
                        .formationType(formation)
                        .build())
                .build())
            .build();
    }
}
