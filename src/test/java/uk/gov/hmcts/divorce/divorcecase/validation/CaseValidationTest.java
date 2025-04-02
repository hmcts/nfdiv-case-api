package uk.gov.hmcts.divorce.divorcecase.validation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_APP_2_RESIDENT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections.APP_1_RESIDENT_JOINT;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionTest.CANNOT_EXIST;
import static uk.gov.hmcts.divorce.divorcecase.model.JurisdictionTest.CONNECTION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.validation.ApplicationValidation.validateChangeServiceRequest;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.SUBMITTED_DATE_IS_NULL;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.notNull;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateApplicant1BasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateBasicCase;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCaseFieldsForIssueApplication;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCasesAcceptedToListForHearing;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateCitizenResendInvite;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateJurisdictionConnections;
import static uk.gov.hmcts.divorce.divorcecase.validation.ValidationUtil.validateMarriageDate;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;

public class CaseValidationTest {

    private static final String LESS_THAN_ONE_YEAR_AGO = " can not be less than one year and one day ago.";
    private static final String LESS_THAN_ONE_YEAR_SINCE_SUBMISSION =
        " can not be less than one year and one day prior to application submission.";
    private static final String EMPTY = " cannot be empty or null";
    private static final String IN_THE_FUTURE = " can not be in the future.";
    private static final String MORE_THAN_ONE_HUNDRED_YEARS_AGO = " can not be more than 100 years ago.";

    @Test
    public void shouldValidateBasicCase() {
        CaseData caseData = new CaseData();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        caseData.setDivorceOrDissolution(DIVORCE);
        List<String> errors = validateBasicCase(caseData);
        assertThat(errors).hasSize(14);
    }

    @Test
    public void shouldValidateBasicOfflineCase() {
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        List<String> errors = validateBasicCase(caseData);
        assertThat(errors).hasSize(11);
    }

    @Test
    public void shouldValidateApplicant1BasicCase() {
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        List<String> errors = validateApplicant1BasicCase(caseData);
        assertThat(errors).hasSize(8);
    }

    @Test
    public void shouldValidateApplicant1BasicOfflineCase() {
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        List<String> errors = validateApplicant1BasicCase(caseData);
        assertThat(errors).hasSize(6);
    }

    @Test
    public void shouldReturnErrorWhenStringIsNull() {
        List<String> response = notNull(null, "field");
        assertThat(response).isEqualTo(List.of("field" + EMPTY));
    }

    @Test
    public void shouldReturnErrorWhenDateIsInTheFuture() {
        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().plus(2, YEARS))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field");
        assertThat(response).isEqualTo(List.of("field" + IN_THE_FUTURE));
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearAgo() {

        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minusYears(1))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field");

        assertThat(response).isEqualTo(List.of("field" + LESS_THAN_ONE_YEAR_AGO));
    }

    @Test
    public void shouldNotReturnErrorWhenDateIsMoreThanOneYearAgo() {

        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minusYears(1).minusDays(1))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field");

        assertThat(response).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnErrorWhenApplicationSubmissionDateIsNullAndCurrentDateWithin1YearAnd1DayOfMarriageDate() {

        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minusYears(1))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field", true);

        assertThat(response).isEqualTo(List.of(SUBMITTED_DATE_IS_NULL, "field" + LESS_THAN_ONE_YEAR_AGO));
    }

    @Test
    public void shouldNotReturnErrorWhenApplicationSubmissionDateIsNullAndMarriageDateMoreThan1YearAnd1DayFromCurrentDate() {

        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minusYears(1).minusDays(1))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field", true);

        assertThat(response).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnErrorWhenDateIsLessThanOneYearSinceApplicationSubmission() {

        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .dateSubmitted(LocalDateTime.now())
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minusYears(1))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field", true);

        assertThat(response).isEqualTo(List.of("field" + LESS_THAN_ONE_YEAR_SINCE_SUBMISSION));
    }

    @Test
    public void shouldNotReturnErrorsWhenDateIsMoreThanOneYearSinceApplicationSubmission() {

        CaseData caseData = CaseData.builder()
            .application(
                Application.builder()
                    .dateSubmitted(LocalDateTime.now())
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minusYears(1).minusDays(1))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field", true);

        assertThat(response).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnNoErrorsWhenDateIsLessThanOneYearAgoAndJudicialSeparationCase() {

        CaseData caseData = CaseData.builder()
            .supplementaryCaseType(SupplementaryCaseType.JUDICIAL_SEPARATION)
            .divorceOrDissolution(DIVORCE)
            .application(
                Application.builder()
                    .marriageDetails(MarriageDetails.builder()
                        .date(LocalDate.now().minus(360, DAYS))
                        .build())
                    .build()
            )
            .build();

        List<String> response = validateMarriageDate(caseData, "field");

        assertThat(response).isEqualTo(emptyList());
    }

    @Test
    public void shouldReturnTrueWhenCaseHasAwaitingDocuments() {
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplication().setApplicant1WantsToHavePapersServedAnotherWay(YES);
        assertTrue(caseData.getApplication().hasAwaitingApplicant1Documents());
    }

    @Test
    public void shouldReturnFalseWhenCaseDoesNotHaveAwaitingDocuments() {
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        assertFalse(caseData.getApplication().hasAwaitingApplicant1Documents());
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameAndPlaceOfMarriageAreMissing() {
        CaseData caseData = new CaseData();
        caseData.setDivorceOrDissolution(DIVORCE);
        List<String> errors = validateCaseFieldsForIssueApplication(caseData.getApplication().getMarriageDetails());

        assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null",
            "PlaceOfMarriage cannot be empty or null"
        );
    }

    @Test
    public void shouldReturnErrorWhenApp2MarriageCertNameIsMissing() {
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setPlaceOfMarriage("London");
        List<String> errors = validateCaseFieldsForIssueApplication(marriageDetails);

        assertThat(errors).containsExactlyInAnyOrder(
            "MarriageApplicant2Name cannot be empty or null"
        );
    }

    @Test
    public void shouldNotReturnErrorWhenBothWhenApp2MarriageCertNameAndPlaceOfMarriageArePresent() {
        MarriageDetails marriageDetails = new MarriageDetails();
        marriageDetails.setPlaceOfMarriage("London");
        marriageDetails.setApplicant2Name("TestFname TestMname  TestLname");
        List<String> errors = validateCaseFieldsForIssueApplication(marriageDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateJurisdictionConnectionsForCitizenApplication() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplication(Application.builder()
            .solSignStatementOfTruth(NO)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_RESIDENT_JOINT));

        final List<String> errors = validateJurisdictionConnections(caseData);

        assertThat(errors).contains(CONNECTION + APP_1_RESIDENT_JOINT + CANNOT_EXIST);
    }

    @Test
    public void shouldOnlyValidateEmptyJurisdictionConnectionsWhenApplicant1Represented() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(YES)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Collections.emptySet());

        List<String> errors = validateJurisdictionConnections(caseData);

        assertThat(errors).containsOnly("JurisdictionConnections" + ValidationUtil.EMPTY);
    }

    @Test
    public void shouldReturnEmptyListForNonEmptyJurisdictionConnectionsWhenApplicant1Represented() {
        final CaseData caseData = caseData();
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(YES)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_APP_2_RESIDENT));

        List<String> errors = validateJurisdictionConnections(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateJurisdictionConnectionsWhenApplicant1IsNotRepresented() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setApplicant1(Applicant.builder()
            .solicitorRepresented(NO)
            .build());

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_RESIDENT_JOINT));

        List<String> errors = validateJurisdictionConnections(caseData);

        assertThat(errors).contains(CONNECTION + APP_1_RESIDENT_JOINT + CANNOT_EXIST);
    }

    @Test
    public void shouldNotReturnErrorsWhenJurisdictionConnectionsIsNotEmptyAndIsPaperCase() {
        final CaseData caseData = caseData();
        caseData.getApplication().setNewPaperCase(YES);

        caseData.getApplication().getJurisdiction().setConnections(Set.of(APP_1_APP_2_RESIDENT));

        List<String> errors = validateJurisdictionConnections(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldReturnErrorsWhenJurisdictionConnectionsIsEmptyAndIsPaperCase() {
        final CaseData caseData = caseData();
        caseData.getApplication().setNewPaperCase(YES);

        List<String> errors = validateJurisdictionConnections(caseData);

        assertThat(errors).containsExactly("JurisdictionConnections cannot be empty or null");
    }


    @Test
    public void shouldValidateNoCasesAdded() {
        final BulkActionCaseData caseData = bulkActionCaseData();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("23456")
            .build();
        final CaseLink caseLink3 = CaseLink.builder()
            .caseReference("34567")
            .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();
        final ListValue<CaseLink> caseLinkListValue2 =
            ListValue.<CaseLink>builder()
                .value(caseLink2)
                .build();
        final ListValue<CaseLink> caseLinkListValue3 =
            ListValue.<CaseLink>builder()
                .value(caseLink3)
                .build();
        caseData.setCasesAcceptedToListForHearing(
            List.of(caseLinkListValue1, caseLinkListValue2, caseLinkListValue3));

        List<String> errors = validateCasesAcceptedToListForHearing(caseData);

        assertThat(errors).contains("You can only remove cases from the list of cases accepted to list for hearing.");
    }

    @Test
    public void shouldValidateNoDuplicateCases() {
        final BulkActionCaseData caseData = bulkActionCaseData();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();
        final ListValue<CaseLink> caseLinkListValue2 =
            ListValue.<CaseLink>builder()
                .value(caseLink2)
                .build();
        caseData.setCasesAcceptedToListForHearing(List.of(caseLinkListValue1, caseLinkListValue2));

        List<String> errors = validateCasesAcceptedToListForHearing(caseData);

        assertThat(errors).contains("You can only remove cases from the list of cases accepted to list for hearing.");
    }

    @Test
    public void shouldValidateBasicPaperCaseAndReturnNoErrorWhenApplicant2GenderIsNotSet() {
        CaseData caseData = new CaseData();
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        caseData.setApplicant2(
            Applicant.builder().email("respondent@test.com").build()
        );

        caseData.getApplication().setNewPaperCase(YES);
        List<String> errors = validateBasicCase(caseData);
        assertThat(errors).hasSize(11);
        assertThat(errors).containsExactly(
            "ApplicationType cannot be empty or null",
            "Applicant1FirstName cannot be empty or null",
            "Applicant1LastName cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null",
            "Applicant1FinancialOrder cannot be empty or null",
            "MarriageApplicant1Name cannot be empty or null",
            "Applicant1ContactDetailsType cannot be empty or null",
            "Statement of truth must be accepted by the person making the application",
            "MarriageDate cannot be empty or null",
            "JurisdictionConnections cannot be empty or null"
        );
    }

    @Test
    public void shouldValidateBasicDigitalCaseAndReturnErrorWhenApplicant2GenderIsNotSet() {
        CaseData caseData = new CaseData();
        Applicant applicant1 = Applicant.builder().offline(YES).build();
        caseData.setApplicant1(applicant1);

        caseData.setApplicant2(
            Applicant.builder().email("respondent@test.com").build()
        );

        List<String> errors = validateBasicCase(caseData);
        assertThat(errors).hasSize(12);
        assertThat(errors).containsExactly(
            "ApplicationType cannot be empty or null",
            "Applicant1FirstName cannot be empty or null",
            "Applicant1LastName cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null",
            "Applicant1FinancialOrder cannot be empty or null",
            "Applicant2Gender cannot be empty or null",
            "MarriageApplicant1Name cannot be empty or null",
            "Applicant1ContactDetailsType cannot be empty or null",
            "Statement of truth must be accepted by the person making the application",
            "MarriageDate cannot be empty or null",
            "JurisdictionConnections cannot be empty or null"
        );
    }

    @Test
    public void validateCitizenResendInviteSuccess() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .caseInvite(CaseInvite.builder().accessCode("12345").build()).build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseDetails.setState(State.AwaitingApplicant2Response);

        List<String> errors = validateCitizenResendInvite(caseDetails);
        assertThat(errors).hasSize(0);
    }

    @Test
    public void validateCitizenResendInviteFailsWhenStateIsWrong() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .caseInvite(CaseInvite.builder().accessCode("12345").build()).build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseDetails.setState(State.Submitted);

        List<String> errors = validateCitizenResendInvite(caseDetails);
        assertThat(errors).hasSize(1);
        assertThat(errors).containsExactly("Not possible to update applicant 2 invite email address");
    }

    @Test
    public void validateCitizenResendInviteFailsWhenApplicationTypeIsWrong() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .caseInvite(CaseInvite.builder().accessCode("12345").build()).build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseDetails.setState(State.AwaitingApplicant2Response);

        List<String> errors = validateCitizenResendInvite(caseDetails);
        assertThat(errors).hasSize(1);
        assertThat(errors).containsExactly("Not possible to update applicant 2 invite email address");
    }

    @Test
    public void validateCitizenResendInviteFailsWhenAccessCodeIsWrong() {
        CaseData caseData = CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .caseInvite(CaseInvite.builder().accessCode(null).build()).build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseDetails.setState(State.AwaitingApplicant2Response);

        List<String> errors = validateCitizenResendInvite(caseDetails);
        assertThat(errors).hasSize(1);
        assertThat(errors).containsExactly("Not possible to update applicant 2 invite email address");
    }

    private BulkActionCaseData bulkActionCaseData() {
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("98765")
            .build();

        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink1)
                    .build())
                .build();

        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink2)
                    .build())
                .build();

        return BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2))
            .build();
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentConfidentialAndPersonalService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).contains("You may not select Solicitor Service or Personal Service if the respondent is confidential.");
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentConfidentialAndSolicitorService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).contains("You may not select Solicitor Service or Personal Service"
            + " if the respondent is confidential.");
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentNotConfidentialOverseasAndCourtServiceSoleApp() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);
        applicant2.setAddressOverseas(YES);

        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).contains("You may not select court service if respondent has an international address.");
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentNotConfidentialOverseasAndCourtServiceJointApp() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);
        applicant2.setAddressOverseas(YES);

        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentConfidentialOverseasAndCourtService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);
        applicant2.setAddressOverseas(YES);

        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentNotAndPersonalService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    public void shouldValidateChangeServiceRequestWhenRespondentNotConfidentialAndSolicitorService() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        List<String> errors = validateChangeServiceRequest(caseData);

        assertThat(errors).isEmpty();
    }
}
