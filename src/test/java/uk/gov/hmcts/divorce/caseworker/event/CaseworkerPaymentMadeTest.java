package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPaymentMade.CASEWORKER_PAYMENT_MADE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseworkerPaymentMadeTest {

    @InjectMocks
    private CaseworkerPaymentMade caseworkerPaymentMade;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerPaymentMade.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PAYMENT_MADE);
    }

    @Test
    void shouldSetCaseStatusToAwaitingDocumentsWhenSoleCaseAndApplicantWantToServeByAlternativeMeansToRespondent() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

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

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerPaymentMade.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
    }

    @Test
    void shouldSetCaseStatusToSubmittedWhenSoleCaseAndRespondentAddressIsKnown() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

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

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerPaymentMade.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
    }

    @Test
    void shouldSetCaseStatusToSubmittedWhenJointCase() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData
            .builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .build();

        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerPaymentMade.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
    }
}
