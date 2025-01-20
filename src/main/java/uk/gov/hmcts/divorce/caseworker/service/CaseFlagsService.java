package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.FlagVisibility;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseFlagsService {

    private final CcdUpdateService ccdUpdateService;
    private final IdamService idamService;
    private final AuthTokenGenerator authTokenGenerator;

    public static final String TEXT_APPLICANT1_FLAGS_ROLE = "applicant or applicant1";
    public static final String TEXT_APPLICANT2_FLAGS_ROLE = "respondent or applicant2";
    public static final String TEXT_APPLICANT1_SOL_FLAGS_ROLE = "applicant or applicant1 solicitor";
    public static final String TEXT_APPLICANT2_SOL_FLAGS_ROLE = "respondent or applicant2 solicitor";

    public void setSupplementaryDataForCaseFlags(Long caseId) {

        String sysUserToken = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        String s2sToken = authTokenGenerator.generate();

        ccdUpdateService.submitSupplementaryDataToCcdForServiceID(
            caseId.toString(),
            sysUserToken,
            s2sToken
        );
    }

    public void initialiseCaseFlags(final CaseData caseData) {

        if (caseData.getCaseFlags() == null) {
            caseData.setCaseFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(null)
                .roleOnCase(null)
                .visibility(FlagVisibility.INTERNAL)
                .build());
        }

        initialisePartyGroupIds(caseData);
        initialiseAllInternalPartyFlags(caseData);
    }

    public void initialisePartyGroupIds(final CaseData caseData) {
        if (StringUtils.isEmpty(caseData.getPartyFlags().getApplicant1GroupId())) {
            caseData.getPartyFlags().setApplicant1GroupId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(caseData.getPartyFlags().getApplicant2GroupId())) {
            caseData.getPartyFlags().setApplicant2GroupId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(caseData.getPartyFlags().getApplicant1SolicitorGroupId())) {
            caseData.getPartyFlags().setApplicant1SolicitorGroupId(UUID.randomUUID().toString());
        }
        if (StringUtils.isEmpty(caseData.getPartyFlags().getApplicant2SolicitorGroupId())) {
            caseData.getPartyFlags().setApplicant2SolicitorGroupId(UUID.randomUUID().toString());
        }
    }

    public void initialiseAllInternalPartyFlags(final CaseData caseData) {

        if (caseData.getPartyFlags().getApplicant1Flags() == null) {
            caseData.getPartyFlags().setApplicant1Flags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant1().getFullName())
                .roleOnCase(TEXT_APPLICANT1_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant1GroupId()))
                .visibility(FlagVisibility.INTERNAL)
                .build());
        }

        if (caseData.getPartyFlags().getApplicant2Flags() == null) {
            caseData.getPartyFlags().setApplicant2Flags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant2().getFullName())
                .roleOnCase(TEXT_APPLICANT2_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant2GroupId()))
                .visibility(FlagVisibility.INTERNAL)
                .build());
        }

        if (caseData.getApplicant1().isRepresented()
            && caseData.getPartyFlags().getApplicant1SolicitorFlags() == null) {
            caseData.getPartyFlags().setApplicant1SolicitorFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant1().getSolicitor().getName())
                .roleOnCase(TEXT_APPLICANT1_SOL_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant1SolicitorGroupId()))
                .visibility(FlagVisibility.INTERNAL)
                .build());
        }

        if (caseData.getApplicant2().isRepresented()
            && caseData.getPartyFlags().getApplicant2SolicitorFlags() == null) {
            caseData.getPartyFlags().setApplicant2SolicitorFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant2().getSolicitor().getName())
                .roleOnCase(TEXT_APPLICANT2_SOL_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant2SolicitorGroupId()))
                .visibility(FlagVisibility.INTERNAL)
                .build());
        }
    }
}
