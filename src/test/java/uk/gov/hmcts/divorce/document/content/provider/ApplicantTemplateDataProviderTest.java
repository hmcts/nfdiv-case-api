package uk.gov.hmcts.divorce.document.content.provider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;

@ExtendWith(MockitoExtension.class)
class ApplicantTemplateDataProviderTest {

    @InjectMocks
    private ApplicantTemplateDataProvider applicantTemplateDataProvider;

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
                .addressLine3("")
                .postTown("Post Town")
                .postCode("Post Code")
                .build())
            .contactDetailsType(PUBLIC)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isEqualTo("Line 1\nLine 2\nPost Town\nPost Code");
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
            .contactDetailsType(PRIVATE)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isNull();
    }

    @Test
    void shouldReturnNullIfApplicantIsNotRepresentedAndApplicantAddressNotPresent() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .build();

        assertThat(applicantTemplateDataProvider.deriveApplicantPostalAddress(applicant))
            .isNull();
    }

    @Test
    public void shouldReturnSolicitorAddressIfApplicantIsSolicitorRepresented() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(YES)
            .contactDetailsType(PUBLIC)
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
            .contactDetailsType(PUBLIC)
            .correspondenceAddress(AddressGlobalUK.builder()
                .addressLine1("Correspondence Address")
                .addressLine2("Line 2")
                .addressLine3("")
                .postTown("Post Town")
                .postCode("Post Code")
                .build())
            .build();

        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isEqualTo("Correspondence Address\nLine 2\nPost Town\nPost Code");
    }

    @Test
    public void shouldReturnHomeAddressIfApplicant2IsNotSolicitorRepresentedAndIsCitizenApplication() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PUBLIC)
            .homeAddress(AddressGlobalUK.builder()
                .addressLine1("Home Address")
                .addressLine2("Line 2")
                .addressLine3("")
                .postTown("Post Town")
                .postCode("Post Code")
                .build())
            .build();

        final Application application = Application.builder()
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isEqualTo("Home Address\nLine 2\nPost Town\nPost Code");
    }

    @Test
    public void shouldReturnNullIfApplicant2IsNotSolicitorRepresentedAndDetailsAreConfidential() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
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

    @Test
    public void shouldReturnNullIfApplicant2IsNotSolicitorRepresentedAndNoAddressSet() {

        final Applicant applicant = Applicant.builder()
            .solicitorRepresented(NO)
            .contactDetailsType(PRIVATE)
            .build();

        final Application application = Application.builder()
            .solSignStatementOfTruth(YES)
            .build();

        final String result = applicantTemplateDataProvider.deriveApplicant2PostalAddress(applicant, application);

        assertThat(result).isNull();
    }
}
