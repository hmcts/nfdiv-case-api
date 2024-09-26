package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.task.SetDefaultOrganisationPolicies;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerHwfApplicationAndPaymentHelperTest {

    @Mock
    private Clock clock;

    @Mock
    private SetDefaultOrganisationPolicies setDefaultOrganisationPolicies;

    @InjectMocks
    private CaseworkerHwfApplicationAndPaymentHelper caseworkerHwfApplicationAndPaymentHelper;

    @Test
    void shouldSetCaseStatusToAwaitingDocumentsWhenSoleCaseAndApplicantWantToServeByAlternativeMeansToRespondent() {
        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .application(
                Application
                    .builder()
                    .applicant1WantsToHavePapersServedAnotherWay(YesOrNo.YES)
                    .applicant1KnowsApplicant2Address(YesOrNo.NO)
                    .build()
            )
            .build();

        final State response = caseworkerHwfApplicationAndPaymentHelper.getState(caseData);

        assertThat(response).isEqualTo(AwaitingDocuments);
    }

    @Test
    void shouldSetCaseStatusToSubmittedWhenSoleCaseAndRespondentAddressIsKnown() {
        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .applicant2(Applicant.builder()
                .address(AddressGlobalUK.builder()
                    .addressLine1("line1")
                    .country("UK")
                    .build())
                .build())
            .application(
                Application
                    .builder()
                    .applicant1WantsToHavePapersServedAnotherWay(YesOrNo.NO)
                    .build()
            )
            .build();

        final State response = caseworkerHwfApplicationAndPaymentHelper.getState(caseData);

        assertThat(response).isEqualTo(Submitted);
    }

    @Test
    void shouldSetCaseStatusToSubmittedWhenJointCase() {
        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .build();

        final CaseData response = caseworkerHwfApplicationAndPaymentHelper.setDateSubmittedAndDueDate(caseData);

        assertThat(response.getApplication().getDateSubmitted()).isNotNull();
        assertThat(response.getDueDate()).isEqualTo(response.getApplication().getDateSubmitted().plusDays(28).toLocalDate());
    }

    @Test
    void shouldNotSetDateSubmittedIfDateSubmittedAlreadySetButSetDueDateIfNull() {
        final LocalDateTime submittedDateTime = LocalDateTime.now();

        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .application(
            Application
                .builder()
                .dateSubmitted(submittedDateTime)
                .build()
            )
            .build();

        final CaseData response = caseworkerHwfApplicationAndPaymentHelper.setDateSubmittedAndDueDate(caseData);

        assertThat(response.getApplication().getDateSubmitted()).isEqualTo(submittedDateTime);
        assertThat(response.getDueDate()).isEqualTo(submittedDateTime.plusDays(28).toLocalDate());
    }

    @Test
    void shouldNotSetDueDateIfDueDateAlreadySetButSetDateSubmittedIfNull() {
        setMockClock(clock);

        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .application(
                Application
                    .builder()
                    .build()
            )
            .dueDate(LocalDate.now())
            .build();

        final CaseData response = caseworkerHwfApplicationAndPaymentHelper.setDateSubmittedAndDueDate(caseData);

        assertThat(response.getApplication().getDateSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(response.getDueDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldSetDefaultOrgPoliciesByDelegatingToCaseTask() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(setDefaultOrganisationPolicies.apply(caseDetails)).thenReturn(caseDetails);

        caseworkerHwfApplicationAndPaymentHelper.setRequiredCaseFieldsForPostSubmissionCase(caseDetails);

        verify(setDefaultOrganisationPolicies).apply(caseDetails);
    }
}
