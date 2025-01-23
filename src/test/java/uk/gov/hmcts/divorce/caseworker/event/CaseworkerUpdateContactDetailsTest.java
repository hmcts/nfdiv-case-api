package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.event.page.UpdateContactDetails;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.ProcessConfidentialDocumentsService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateContactDetails.CASEWORKER_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateContactDetailsTest {

    @Mock
    private UpdateContactDetails updateContactDetails;

    @Mock
    private ProcessConfidentialDocumentsService processConfidentialDocumentsService;

    @Mock
    private CaseFlagsService caseFlagsService;

    @InjectMocks
    private CaseworkerUpdateContactDetails caseworkerUpdateContactDetails;

    @Test
    public void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateContactDetails.configure(configBuilder);

        verify(updateContactDetails).addTo(any(PageBuilder.class));

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_CONTACT_DETAILS);
    }

    @Test
    public void aboutToSubmitShouldCallProcessConfidentialDocuments() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setState(Submitted);
        final CaseData caseData = CaseData.builder().build();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        caseworkerUpdateContactDetails.aboutToSubmit(details, details);

        verify(processConfidentialDocumentsService).processDocuments(caseData, details.getId());
    }

    @Test
    public void shouldCallCaseFlagsServiceWhenApplicant1NameIsChanged() {
        final CaseData beforeCaseData = CaseData.builder().build();
        final CaseData afterCaseData = CaseData.builder().build();

        Applicant beforeApplicant = Applicant.builder().firstName("Old").lastName("Name").build();
        Applicant afterApplicant = Applicant.builder().firstName("New").lastName("Name").build();

        beforeCaseData.setApplicant1(beforeApplicant);
        afterCaseData.setApplicant1(afterApplicant);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> afterDetails = new CaseDetails<>();
        afterDetails.setId(TEST_CASE_ID);
        afterDetails.setData(afterCaseData);

        caseworkerUpdateContactDetails.aboutToSubmit(afterDetails, beforeDetails);

        verify(caseFlagsService).updatePartyNameInCaseFlags(afterCaseData, CaseFlagsService.PartyFlagType.APPLICANT_1);
    }

    @Test
    public void shouldCallCaseFlagsServiceWhenApplicant2NameIsChanged() {
        final CaseData beforeCaseData = CaseData.builder().build();
        final CaseData afterCaseData = CaseData.builder().build();

        Applicant beforeApplicant = Applicant.builder().firstName("Old").lastName("Name").build();
        Applicant afterApplicant = Applicant.builder().firstName("New").lastName("Name").build();

        beforeCaseData.setApplicant2(beforeApplicant);
        afterCaseData.setApplicant2(afterApplicant);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> afterDetails = new CaseDetails<>();
        afterDetails.setId(TEST_CASE_ID);
        afterDetails.setData(afterCaseData);

        caseworkerUpdateContactDetails.aboutToSubmit(afterDetails, beforeDetails);

        verify(caseFlagsService).updatePartyNameInCaseFlags(afterCaseData, CaseFlagsService.PartyFlagType.APPLICANT_2);
    }

    @Test
    public void shouldCallCaseFlagsServiceWhenApplicant1SolicitorNameIsChanged() {
        final CaseData beforeCaseData = CaseData.builder().build();
        final CaseData afterCaseData = CaseData.builder().build();

        Solicitor beforeSolicitor = Solicitor.builder().name("Old Name").build();
        Solicitor afterSolicitor = Solicitor.builder().name("New Name").build();

        Applicant beforeApplicant = Applicant.builder().firstName("Test").lastName("Name").solicitorRepresented(YesOrNo.YES).solicitor(beforeSolicitor).build();
        Applicant afterApplicant = Applicant.builder().firstName("Test").lastName("Name").solicitorRepresented(YesOrNo.YES).solicitor(afterSolicitor).build();

        beforeCaseData.setApplicant1(beforeApplicant);
        afterCaseData.setApplicant1(afterApplicant);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> afterDetails = new CaseDetails<>();
        afterDetails.setId(TEST_CASE_ID);
        afterDetails.setData(afterCaseData);

        caseworkerUpdateContactDetails.aboutToSubmit(afterDetails, beforeDetails);

        verify(caseFlagsService).updatePartyNameInCaseFlags(afterCaseData, CaseFlagsService.PartyFlagType.APPLICANT_1_SOLICITOR);
    }

    @Test
    public void shouldCallCaseFlagsServiceWhenApplicant2SolicitorNameIsChanged() {
        final CaseData beforeCaseData = CaseData.builder().build();
        final CaseData afterCaseData = CaseData.builder().build();

        Solicitor beforeSolicitor = Solicitor.builder().name("Old Name").build();
        Solicitor afterSolicitor = Solicitor.builder().name("New Name").build();

        Applicant beforeApplicant = Applicant.builder().firstName("Test").lastName("Name").solicitorRepresented(YesOrNo.YES).solicitor(beforeSolicitor).build();
        Applicant afterApplicant = Applicant.builder().firstName("Test").lastName("Name").solicitorRepresented(YesOrNo.YES).solicitor(afterSolicitor).build();

        beforeCaseData.setApplicant2(beforeApplicant);
        afterCaseData.setApplicant2(afterApplicant);

        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setId(TEST_CASE_ID);
        beforeDetails.setData(beforeCaseData);

        final CaseDetails<CaseData, State> afterDetails = new CaseDetails<>();
        afterDetails.setId(TEST_CASE_ID);
        afterDetails.setData(afterCaseData);

        caseworkerUpdateContactDetails.aboutToSubmit(afterDetails, beforeDetails);

        verify(caseFlagsService).updatePartyNameInCaseFlags(afterCaseData, CaseFlagsService.PartyFlagType.APPLICANT_2_SOLICITOR);
    }
}
