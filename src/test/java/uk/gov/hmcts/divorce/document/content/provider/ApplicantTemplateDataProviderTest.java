package uk.gov.hmcts.divorce.document.content.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.FinancialOrderFor.CHILDREN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;

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
            .financialOrderFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant))
            .isEqualTo("applicants, and for the children of both the applicants.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant))
            .isEqualTo("applicants.");
    }

    @Test
    void shouldReturnCorrectStringForJointFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveJointFinancialOrder(applicant))
            .isEqualTo("children of both the applicants.");
    }

    @Test
    void shouldReturnNullForSoleIfNoFinancialOrder() {

        final Applicant applicant = Applicant.builder().build();

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
            .financialOrderFor(Set.of(APPLICANT, CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("applicant, and for the children of the applicant and the respondent.");
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForApplicant() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(APPLICANT))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("applicant.");
    }

    @Test
    void shouldReturnCorrectStringForSoleFinancialOrderForChildren() {

        final Applicant applicant = Applicant.builder()
            .financialOrder(YES)
            .financialOrderFor(Set.of(CHILDREN))
            .build();

        assertThat(applicantTemplateDataProvider.deriveSoleFinancialOrder(applicant))
            .isEqualTo("children of the applicant and the respondent.");
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
            .keepContactDetailsConfidential(NO)
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
            .keepContactDetailsConfidential(YES)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isNull();
    }

    @Test
    void shouldReturnNullIfApplicantIsNotRepresentedAndApplicantAddressNotPresent() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .keepContactDetailsConfidential(NO)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isNull();
    }

    @Test
    public void shouldReturnSolicitorAddressIfApplicantIsSolicitorRepresented() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .contactDetailsConfidential(SHARE)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("Home Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .correspondenceAddress(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .solicitor(Solicitor
                .builder()
                .email(TEST_SOLICITOR_EMAIL)
                .address("Solicitor Address")
                .build())
            .build();

        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isEqualTo("Solicitor Address");
    }

    @Test
    public void shouldReturnCorrespondenceAddressIfApplicant2IsNotSolicitorRepresentedAndIsSolicitorApplication() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsConfidential(SHARE)
            .correspondenceAddress(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .build();

        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isEqualTo("Correspondence Address\nLine 2\nLine 3\nPost Town\nCounty\nPost Code");
    }

    @Test
    public void shouldReturnHomeAddressIfApplicant2IsNotSolicitorRepresentedAndIsCitizenApplication() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsConfidential(SHARE)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("Home Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .build();

        final Application application = Application.builder()
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isEqualTo("Home Address\nLine 2\nLine 3\nPost Town\nCounty\nPost Code");
    }

    @Test
    public void shouldReturnNullIfApplicant2IsNotSolicitorRepresentedAndDetailsAreConfidential() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsConfidential(KEEP)
            .correspondenceAddress(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("Line 3")
                .postTown("Post Town")
                .county("County")
                .postCode("Post Code")
                .build())
            .build();

        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isNull();
    }
}