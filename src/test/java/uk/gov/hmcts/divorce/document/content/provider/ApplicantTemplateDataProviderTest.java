package uk.gov.hmcts.divorce.document.content.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;

import java.util.Set;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

@ExtendWith(MockitoExtension.class)
class ApplicantTemplateDataProviderTest {

    @InjectMocks
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Test
    void shouldReturnNullForJointIfNoFinancialOrder() {

        final Applicant applicant = Applicant.builder().build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullForJointIfEmptyFinancialOrder() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(emptySet())
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullForJointIfFinancialOrderIsNo() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicantAndChildrenForJoint() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant))
            .isEqualTo("applicants, and for the children of both the applicants.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant))
            .isEqualTo("applicants.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant))
            .isEqualTo("children of both the applicants.");
    }

    @Test
    void shouldReturnNullForJointIfEmptyFinancialOrderFor() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(emptySet())).isNull();
    }

    @Test
    void shouldReturnCorrectStringIfFinancialOrderForContainsApplicantAndChildrenForJoint() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(Set.of(APPLICANT, CHILDREN)))
            .isEqualTo("applicants, and for the children of both the applicants.");
    }

    @Test
    void shouldReturnCorrectStringIfFinancialOrderForContainsApplicant() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(Set.of(APPLICANT)))
            .isEqualTo("applicants.");
    }

    @Test
    void shouldReturnCorrectStringIfFinancialOrderForContainsChildren() {
        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(Set.of(CHILDREN)))
            .isEqualTo("children of both the applicants.");
    }

    @Test
    void shouldReturnNullForSoleIfNoFinancialOrder() {

        final Applicant applicant = Applicant.builder().build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullForSoleIfEmptyFinancialOrder() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(emptySet())
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullForSoleIfFinancialOrderIsNo() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForApplicantAndChildrenForJoint() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("applicant, and for the children of the applicant and the respondent.");
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("applicant.");
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrdersFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("children of the applicant and the respondent.");
    }
}
