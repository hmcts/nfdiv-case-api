package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerNoticeOfChange.CASEWORKER_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant.APPLICANT_1;
import static uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerNoticeOfChangeTest {

    @Mock
    private CcdAccessService caseAccessService;

    @InjectMocks
    private CaseworkerNoticeOfChange noticeOfChange;

    @Test
    public void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        noticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_NOTICE_OF_CHANGE);
    }


    @Test
    public void testApp1NowCitizen() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_1)
            .areTheyRepresented(NO)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant1().getSolicitor()).isNull();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(NO);

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_1_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testSoleApplicationApp1NowOfflineSolicitor() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(NO)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant1().getSolicitor()).isNotNull();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isNull();
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_1_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testSoleApplicationApp1NowOnlineSolicitor() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant1().getSolicitor()).isNotNull();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isNull();
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_1_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testJointApplicationApp1NowOnlineSolicitor() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setApplicationType(JOINT_APPLICATION);
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_1)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant1().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant1().getSolicitor()).isNotNull();
        assertThat(result.getData().getApplicant1().getSolicitorRepresented()).isEqualTo(YES);

        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isEqualTo(NO);

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                CREATOR.getRole(),
                APPLICANT_1_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testApp2NowCitizen() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_2)
            .areTheyRepresented(NO)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant2().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant2().getSolicitor()).isNull();
        assertThat(result.getData().getApplicant2().getSolicitorRepresented()).isEqualTo(NO);

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                UserRole.APPLICANT_2.getRole(),
                APPLICANT_2_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testSoleApp2NowOfflineSolicitor() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_2)
            .areTheyRepresented(YES)
            .areTheyDigital(NO)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant2().isApplicantOffline()).isTrue();
        assertThat(result.getData().getApplicant2().getSolicitor()).isNotNull();
        assertThat(result.getData().getApplicant2().getSolicitorRepresented()).isEqualTo(YES);

        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isNull();
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isNull();

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                UserRole.APPLICANT_2.getRole(),
                APPLICANT_2_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testSoleApplicationApp2NowOnlineSolicitor() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_2)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant2().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant2().getSolicitor()).isNotNull();
        assertThat(result.getData().getApplicant2().getSolicitorRepresented()).isEqualTo(YES);

        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isNull();
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isNull();


        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                UserRole.APPLICANT_2.getRole(),
                APPLICANT_2_SOLICITOR.getRole()
            )
        ));
    }

    @Test
    public void testJointApplicationApp2NowOnlineSolicitor() {
        var caseDetails = getCaseDetails();
        caseDetails.getData().setApplicationType(JOINT_APPLICATION);
        caseDetails.getData().setNoticeOfChange(NoticeOfChange.builder()
            .whichApplicant(APPLICANT_2)
            .areTheyRepresented(YES)
            .areTheyDigital(YES)
            .build());

        var result = noticeOfChange.aboutToSubmit(caseDetails, caseDetails);

        assertThat(result.getData().getApplicant2().isApplicantOffline()).isFalse();
        assertThat(result.getData().getApplicant2().getSolicitor()).isNotNull();
        assertThat(result.getData().getApplicant2().getSolicitorRepresented()).isEqualTo(YES);

        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsSubmitted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted()).isEqualTo(NO);
        assertThat(result.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsSubmitted()).isEqualTo(NO);


        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(
            List.of(
                UserRole.APPLICANT_2.getRole(),
                APPLICANT_2_SOLICITOR.getRole()
            )
        ));
    }

    private CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant1(applicantRepresentedBySolicitor());
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplicant1().setOffline(NO);
        data.getApplicant2().setOffline(NO);
        details.setData(data);
        details.setId(1L);

        return details;
    }
}
