package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.citizen.service.SwitchToSoleService;
import uk.gov.hmcts.divorce.common.notification.SwitchedToSoleFoNotification;
import uk.gov.hmcts.divorce.common.service.GeneralReferralService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SwitchedToSoleFinalOrderOffline.SWITCH_TO_SOLE_FO_OFFLINE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.OfflineDocumentReceived.FO_D36;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineApplicationType.SWITCH_TO_SOLE;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.OfflineWhoApplying.APPLICANT_2;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validJointApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class SwitchedToSoleFinalOrderOfflineTest {

    @Mock
    private SwitchToSoleService switchToSoleService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private SwitchedToSoleFoNotification switchedToSoleFoNotification;

    @Mock
    private GeneralReferralService generalReferralService;

    @InjectMocks
    private SwitchedToSoleFinalOrderOffline switchedToSoleFinalOrderOffline;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        switchedToSoleFinalOrderOffline.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SWITCH_TO_SOLE_FO_OFFLINE);
    }

    @Test
    void shouldSwitchUserDataAndRolesIfApplicant2TriggeredD36SwitchToSole() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build());
        caseData.setFinalOrder(FinalOrder.builder()
            .d36ApplicationType(SWITCH_TO_SOLE)
            .d36WhoApplying(APPLICANT_2)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleFinalOrderOffline.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleService).switchUserRoles(caseData, caseId);
        verify(switchToSoleService).switchApplicantData(caseData);
    }

    @Test
    void shouldNotSwitchRolesIfApplicant2TriggeredD36SwitchToSoleAndIsNewPaperCase() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.getApplication().setNewPaperCase(YES);
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build());
        caseData.setFinalOrder(FinalOrder.builder()
            .d36ApplicationType(SWITCH_TO_SOLE)
            .d36WhoApplying(APPLICANT_2)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleFinalOrderOffline.aboutToSubmit(caseDetails, caseDetails);

        verify(switchToSoleService).switchApplicantData(caseData);
        verifyNoMoreInteractions(switchToSoleService);
    }

    @Test
    void shouldNotSwitchUserDataOrRolesIfApplicant1TriggeredD36SwitchToSole() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        caseData.setDocuments(CaseDocuments.builder().typeOfDocumentAttached(FO_D36).build());
        caseData.setFinalOrder(FinalOrder.builder()
            .d36ApplicationType(SWITCH_TO_SOLE)
            .d36WhoApplying(APPLICANT_1)
            .build()
        );
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleFinalOrderOffline.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(switchToSoleService);
    }

    @Test
    void shouldSendNotificationsInSubmittedCallback() {
        final long caseId = TEST_CASE_ID;
        CaseData caseData = validJointApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .id(caseId)
            .data(caseData)
            .build();

        switchedToSoleFinalOrderOffline.submitted(caseDetails, caseDetails);

        verify(notificationDispatcher).send(switchedToSoleFoNotification, caseDetails.getData(), caseId);
    }

    @Test
    void shouldPassCaseDetailsToGeneralReferralService() {
        final CaseData caseData = CaseData.builder().applicationType(SOLE_APPLICATION).build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().id(TEST_CASE_ID).data(caseData).build();

        switchedToSoleFinalOrderOffline.submitted(caseDetails, null);

        verify(generalReferralService).caseWorkerGeneralReferral(same(caseDetails));
    }
}
