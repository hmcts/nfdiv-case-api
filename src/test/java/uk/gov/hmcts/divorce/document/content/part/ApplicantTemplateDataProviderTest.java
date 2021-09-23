package uk.gov.hmcts.divorce.document.content.part;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ConfidentialAddress.KEEP;
import static uk.gov.hmcts.divorce.divorcecase.model.ConfidentialAddress.SHARE;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;

@ExtendWith(MockitoExtension.class)
class ApplicantTemplateDataProviderTest {

    @InjectMocks
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

    @Test
    void shouldReturnNullIfNoFinancialOrder() {

        final Applicant applicant = Applicant.builder().build();

        assertThat(applicantTemplateDataProvider.deriveFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnNullIfFinancialOrderIsNo() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveFinancialOrder(applicant)).isNull();
    }

    @Test
    void shouldReturnCorrectStringForFinancialOrderForApplicantAndChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveFinancialOrder(applicant))
            .isEqualTo("applicants, and for the children of both the applicants.");
    }

    @Test
    void shouldReturnCorrectStringForFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveFinancialOrder(applicant))
            .isEqualTo("applicants.");
    }

    @Test
    void shouldReturnCorrectStringForFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveFinancialOrder(applicant))
            .isEqualTo("children of both the applicants.");
    }

    @Test
    void shouldReturnSolicitorAddressIfApplicantIsRepresented() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .address("solicitor address")
                .email("email@email.com")
                .build())
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isEqualTo("solicitor address");
    }

    @Test
    void shouldReturnApplicantAddressIfApplicantIsNotRepresentedAndDetailsAreShareable() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("Line 1")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .contactDetailsConfidential(SHARE)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isEqualTo("Line 1\nLine 2\nLine 3\nPost Town\nCounty\nPost Code");
    }

    @Test
    void shouldReturnNullIfApplicantIsNotRepresentedAndDetailsAreConfidential() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("Line 1")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .contactDetailsConfidential(KEEP)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isNull();
    }

    @Test
    void shouldReturnNullIfApplicantIsNotRepresentedAndApplicantAddressNotPresent() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsConfidential(SHARE)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isNull();
    }
}