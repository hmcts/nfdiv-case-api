package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService.PartyFlagType;
import static uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService.TEXT_APPLICANT1_FLAGS_ROLE;
import static uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService.TEXT_APPLICANT1_SOL_FLAGS_ROLE;
import static uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService.TEXT_APPLICANT2_FLAGS_ROLE;
import static uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService.TEXT_APPLICANT2_SOL_FLAGS_ROLE;
import static uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService.VISIBILITY_INTERNAL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class CaseFlagsServiceTest {

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseFlagsService caseFlagsService;

    @Test
    void shouldSetSupplementaryDataForCaseFlags() {

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(new User(TEST_SERVICE_AUTH_TOKEN, null));
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        caseFlagsService.setSupplementaryDataForCaseFlags(TEST_CASE_ID);

        verify(ccdUpdateService).submitSupplementaryDataToCcdForServiceID(
            TEST_CASE_ID.toString(),
            TEST_SERVICE_AUTH_TOKEN,
            SERVICE_AUTHORIZATION
        );
    }

    @Test
    void shouldInitialiseCaseLevelFlagsWhenNotInitialisedAlready() {
        CaseData caseData = validApplicant1CaseData();

        caseFlagsService.initialiseCaseFlags(caseData);

        assertThat(caseData.getCaseFlags()).isNotNull();
        assertThat(caseData.getCaseFlags().getVisibility()).isEqualTo(VISIBILITY_INTERNAL);
    }

    @Test
    void shouldNotInitialiseCaseLevelFlagsWhenInitialisedAlready() {
        CaseData caseData = getCaseDataForTest(true,false,true,false, false);

        caseFlagsService.initialiseCaseFlags(caseData);

        assertThat(caseData.getCaseFlags().getDetails().size()).isNotEqualTo(0);
    }

    @Test
    void shouldInitialiseGroupIdsWhenNotInitialisedAlready() {
        CaseData caseData = validApplicant1CaseData();

        caseFlagsService.initialisePartyGroupIds(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1GroupId()).isNotNull();
        assertThat(caseData.getPartyFlags().getApplicant2GroupId()).isNotNull();
        assertThat(caseData.getPartyFlags().getApplicant1SolicitorGroupId()).isNotNull();
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorGroupId()).isNotNull();
    }

    @Test
    void shouldNotInitialiseGroupIdsWhenInitialisedAlready() {
        CaseData caseData = validApplicant1CaseData();
        String groupID = "Test Group Id";
        caseData.getPartyFlags().setApplicant1GroupId(groupID);
        caseData.getPartyFlags().setApplicant2GroupId(groupID);
        caseData.getPartyFlags().setApplicant1SolicitorGroupId(groupID);
        caseData.getPartyFlags().setApplicant2SolicitorGroupId(groupID);

        caseFlagsService.initialisePartyGroupIds(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1GroupId()).isEqualTo(groupID);
        assertThat(caseData.getPartyFlags().getApplicant2GroupId()).isEqualTo(groupID);
        assertThat(caseData.getPartyFlags().getApplicant1SolicitorGroupId()).isEqualTo(groupID);
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorGroupId()).isEqualTo(groupID);
    }

    @Test
    void shouldInitialiseApplicant1FlagsWhenNotInitialisedAlready() {
        CaseData caseData = validApplicant1CaseData();

        setGroupIds(caseData);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1Flags()).isNotNull();
        assertThat(caseData.getPartyFlags().getApplicant1Flags().getVisibility()).isEqualTo(VISIBILITY_INTERNAL);
    }

    @Test
    void shouldNotInitialiseApplicant1FlagsWhenInitialisedAlready() {
        CaseData caseData = getCaseDataForTest(true, false, false, true, false);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1Flags().getDetails().size()).isNotEqualTo(0);
    }

    @Test
    void shouldInitialiseApplicant2FlagsWhenNotInitialisedAlready() {
        CaseData caseData = validApplicant1CaseData();

        setGroupIds(caseData);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant2Flags()).isNotNull();
        assertThat(caseData.getPartyFlags().getApplicant2Flags().getVisibility()).isEqualTo(VISIBILITY_INTERNAL);
    }

    @Test
    void shouldNotInitialiseApplicant2FlagsWhenInitialisedAlready() {
        CaseData caseData = getCaseDataForTest(false, false, false, true, false);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant2Flags().getDetails().size()).isNotEqualTo(0);
    }

    @Test
    void shouldInitialiseApplicant1SolicitorFlagsWhenNotInitialisedAlreadyAndApplicant1Represented() {
        CaseData caseData = getCaseDataForTest(true, true, false, true, false);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags()).isNotNull();
    }

    @Test
    void shouldNotInitialiseApplicant1SolicitorFlagsWhenInitialisedAlreadyAndApplicant1Represented() {
        CaseData caseData = getCaseDataForTest(true, true, false, true, true);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags().getDetails().size()).isNotEqualTo(0);
    }

    @Test
    void shouldNotInitialiseApplicant1SolicitorFlagsWhenApplicant1NotRepresented() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant1().setSolicitorRepresented(YesOrNo.NO);

        setGroupIds(caseData);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags()).isNull();
    }

    @Test
    void shouldInitialiseApplicant2SolicitorFlagsWhenNotInitialisedAlreadyAndApplicant2Represented() {
        CaseData caseData = getCaseDataForTest(false, true, false, true, false);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags()).isNotNull();
    }

    @Test
    void shouldNotInitialiseApplicant2SolicitorFlagsWhenInitialisedAlreadyAndApplicant2Represented() {
        CaseData caseData = getCaseDataForTest(false, true, false, true, true);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags().getDetails().size()).isNotEqualTo(0);
    }

    @Test
    void shouldNotInitialiseApplicant2SolicitorFlagsWhenApplicant2NotRepresented() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplicant2().setSolicitorRepresented(YesOrNo.NO);

        setGroupIds(caseData);

        caseFlagsService.initialiseAllInternalPartyFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags()).isNull();
    }

    @Test
    void shouldUpdateApplicant1NameInFlags() {
        CaseData caseData = getCaseDataForTest(true, false, false, true, false);
        caseData.getApplicant1().setFirstName("New");
        caseData.getApplicant1().setLastName("Name");
        caseFlagsService.updatePartyNameInCaseFlags(caseData, PartyFlagType.APPLICANT_1);

        assertThat(caseData.getPartyFlags().getApplicant1Flags().getPartyName()).isEqualTo("New Name");
    }

    @Test
    void shouldUpdateApplicant2NameInFlags() {
        CaseData caseData = getCaseDataForTest(false, false, false, true, false);
        caseData.getApplicant2().setFirstName("New");
        caseData.getApplicant2().setLastName("Name");
        caseFlagsService.updatePartyNameInCaseFlags(caseData, PartyFlagType.APPLICANT_2);

        assertThat(caseData.getPartyFlags().getApplicant2Flags().getPartyName()).isEqualTo("New Name");
    }

    @Test
    void shouldUpdateApplicant1SolicitorNameInFlags() {
        CaseData caseData = getCaseDataForTest(true, true, false, true, true);
        caseData.getApplicant1().getSolicitor().setName("New Name");
        caseFlagsService.updatePartyNameInCaseFlags(caseData, PartyFlagType.APPLICANT_1_SOLICITOR);

        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags().getPartyName()).isEqualTo("New Name");
    }

    @Test
    void shouldUpdateApplicant2SolicitorNameInFlags() {
        CaseData caseData = getCaseDataForTest(false, true, false, true, true);
        caseData.getApplicant2().getSolicitor().setName("New Name");
        caseFlagsService.updatePartyNameInCaseFlags(caseData, PartyFlagType.APPLICANT_2_SOLICITOR);

        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags().getPartyName()).isEqualTo("New Name");
    }

    @Test
    void shouldResetApplicant1SolicitorFlags() {
        CaseData caseData = getCaseDataForTest(true, true, false, true, true);
        caseFlagsService.resetSolicitorCaseFlags(caseData, true);
        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags()).isNull();
    }

    @Test
    void shouldResetApplicant2SolicitorFlags() {
        CaseData caseData = getCaseDataForTest(false, true, false, true, true);
        caseFlagsService.resetSolicitorCaseFlags(caseData, false);
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags()).isNull();
    }

    @Test
    void shouldSwitchApplicantCaseFlagsIfPresent() {
        CaseData caseData = getCaseDataWithAllPartyFlagsSet();

        caseFlagsService.switchCaseFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1Flags().getPartyName()).isEqualTo("Applicant2 User");
        assertThat(caseData.getPartyFlags().getApplicant1Flags().getDetails().get(0).getValue().getName()).isEqualTo("App2 Flag");
        assertThat(caseData.getPartyFlags().getApplicant1Flags().getRoleOnCase()).isEqualTo(TEXT_APPLICANT1_FLAGS_ROLE);
        assertThat(caseData.getPartyFlags().getApplicant1Flags().getGroupId().toString())
            .isEqualTo(caseData.getPartyFlags().getApplicant1GroupId());
        assertThat(caseData.getPartyFlags().getApplicant2Flags().getPartyName()).isEqualTo("Applicant1 User");
        assertThat(caseData.getPartyFlags().getApplicant2Flags().getDetails().get(0).getValue().getName()).isEqualTo("App1 Flag");
        assertThat(caseData.getPartyFlags().getApplicant2Flags().getRoleOnCase()).isEqualTo(TEXT_APPLICANT2_FLAGS_ROLE);
        assertThat(caseData.getPartyFlags().getApplicant2Flags().getGroupId().toString())
            .isEqualTo(caseData.getPartyFlags().getApplicant2GroupId());
    }

    @Test
    void shouldSwitchSolicitorCaseFlagsIfPresent() {
        CaseData caseData = getCaseDataWithAllPartyFlagsSet();

        caseFlagsService.switchCaseFlags(caseData);

        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags().getPartyName()).isEqualTo("Applicant2 Solicitor");
        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags().getDetails().get(0).getValue().getName()).isEqualTo("Sol2 Flag");
        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags().getRoleOnCase()).isEqualTo(TEXT_APPLICANT1_SOL_FLAGS_ROLE);
        assertThat(caseData.getPartyFlags().getApplicant1SolicitorFlags().getGroupId().toString())
            .isEqualTo(caseData.getPartyFlags().getApplicant1SolicitorGroupId());
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags().getPartyName()).isEqualTo("Applicant1 Solicitor");
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags().getDetails().get(0).getValue().getName()).isEqualTo("Sol1 Flag");
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags().getRoleOnCase()).isEqualTo(TEXT_APPLICANT2_SOL_FLAGS_ROLE);
        assertThat(caseData.getPartyFlags().getApplicant2SolicitorFlags().getGroupId().toString())
            .isEqualTo(caseData.getPartyFlags().getApplicant2SolicitorGroupId());
    }

    private CaseData getCaseDataForTest(boolean applicant1,
                                        boolean isRepresented,
                                        boolean hasCaseFlags,
                                        boolean hasApplicantFlags,
                                        boolean hasSolicitorFlags) {
        CaseData caseData = CaseData.builder().build();

        Solicitor solicitor = Solicitor.builder().name("Test Solicitor").build();
        Applicant applicant = Applicant.builder().build();

        if (isRepresented) {
            applicant.setSolicitorRepresented(YesOrNo.YES);
            applicant.setSolicitor(solicitor);
        }

        if (applicant1) {
            caseData.setApplicant1(applicant);
        } else {
            caseData.setApplicant2(applicant);
        }

        if (!hasCaseFlags && !hasApplicantFlags && !hasSolicitorFlags) {
            return caseData;
        }

        setGroupIds(caseData);

        List<ListValue<FlagDetail>> list = getFlagListWithFlag("Test Flag");

        Flags caseFlags = Flags.builder().visibility(VISIBILITY_INTERNAL).details(list).build();
        Flags applicantFlags = Flags.builder().partyName("Applicant").visibility(VISIBILITY_INTERNAL).details(list).build();
        Flags solicitorFlags = Flags.builder().partyName("Solicitor").visibility(VISIBILITY_INTERNAL).details(list).build();

        if (hasCaseFlags) {
            caseData.setCaseFlags(caseFlags);
        }
        if (applicant1 && hasApplicantFlags) {
            applicantFlags.setRoleOnCase(TEXT_APPLICANT1_FLAGS_ROLE);
            caseData.getPartyFlags().setApplicant1Flags(applicantFlags);
            if (hasSolicitorFlags) {
                solicitorFlags.setRoleOnCase(TEXT_APPLICANT1_SOL_FLAGS_ROLE);
                caseData.getPartyFlags().setApplicant1SolicitorFlags(solicitorFlags);
            }
        }
        if (!applicant1 && hasApplicantFlags) {
            applicantFlags.setRoleOnCase(TEXT_APPLICANT2_FLAGS_ROLE);
            caseData.getPartyFlags().setApplicant2Flags(applicantFlags);
            if (hasSolicitorFlags) {
                solicitorFlags.setRoleOnCase(TEXT_APPLICANT2_SOL_FLAGS_ROLE);
                caseData.getPartyFlags().setApplicant2SolicitorFlags(solicitorFlags);
            }
        }
        return caseData;
    }

    private CaseData getCaseDataWithAllPartyFlagsSet() {
        final CaseData caseData = CaseData.builder().build();

        final Solicitor solicitor1 = Solicitor.builder().name("Applicant1 Solicitor").build();
        final Solicitor solicitor2 = Solicitor.builder().name("Applicant2 Solicitor").build();
        final Applicant applicant1 = Applicant.builder().firstName("Applicant1").lastName("User").build();
        final Applicant applicant2 = Applicant.builder().firstName("Applicant2").lastName("User").build();

        applicant1.setSolicitorRepresented(YesOrNo.YES);
        applicant1.setSolicitor(solicitor1);

        applicant2.setSolicitorRepresented(YesOrNo.YES);
        applicant2.setSolicitor(solicitor2);

        caseData.setApplicant1(applicant1);
        caseData.setApplicant2(applicant2);

        setGroupIds(caseData);

        final Flags caseFlags = Flags.builder().visibility(VISIBILITY_INTERNAL).details(getFlagListWithFlag("Case Flag 1")).build();
        final Flags applicant1Flags = Flags
            .builder()
            .partyName(applicant1.getFullName())
            .visibility(VISIBILITY_INTERNAL)
            .roleOnCase(TEXT_APPLICANT1_FLAGS_ROLE)
            .details(getFlagListWithFlag("App1 Flag"))
            .build();
        final Flags applicant2Flags = Flags
            .builder()
            .partyName(applicant2.getFullName())
            .visibility(VISIBILITY_INTERNAL)
            .roleOnCase(TEXT_APPLICANT2_FLAGS_ROLE)
            .details(getFlagListWithFlag("App2 Flag"))
            .build();
        final Flags solicitor1Flags = Flags
            .builder()
            .partyName(solicitor1.getName())
            .visibility(VISIBILITY_INTERNAL)
            .roleOnCase(TEXT_APPLICANT1_SOL_FLAGS_ROLE)
            .details(getFlagListWithFlag("Sol1 Flag"))
            .build();
        final Flags solicitor2Flags = Flags
            .builder()
            .partyName(solicitor2.getName())
            .visibility(VISIBILITY_INTERNAL)
            .roleOnCase(TEXT_APPLICANT2_SOL_FLAGS_ROLE)
            .details(getFlagListWithFlag("Sol2 Flag"))
            .build();

        caseData.setCaseFlags(caseFlags);
        caseData.getPartyFlags().setApplicant1Flags(applicant1Flags);
        caseData.getPartyFlags().setApplicant2Flags(applicant2Flags);
        caseData.getPartyFlags().setApplicant1SolicitorFlags(solicitor1Flags);
        caseData.getPartyFlags().setApplicant2SolicitorFlags(solicitor2Flags);

        return caseData;
    }

    List<ListValue<FlagDetail>> getFlagListWithFlag(String flagName) {
        FlagDetail flagDetail = FlagDetail.builder().name(flagName).build();
        ListValue<FlagDetail> listValue = ListValue.<FlagDetail>builder().id("testId").value(flagDetail).build();
        List<ListValue<FlagDetail>> list = List.of(listValue);
        return list;
    }

    private void setGroupIds(final CaseData caseData) {
        caseData.getPartyFlags().setApplicant1GroupId(UUID.randomUUID().toString());
        caseData.getPartyFlags().setApplicant2GroupId(UUID.randomUUID().toString());
        caseData.getPartyFlags().setApplicant1SolicitorGroupId(UUID.randomUUID().toString());
        caseData.getPartyFlags().setApplicant2SolicitorGroupId(UUID.randomUUID().toString());
    }
}
