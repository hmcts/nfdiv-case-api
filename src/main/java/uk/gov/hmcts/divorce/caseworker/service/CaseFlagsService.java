package uk.gov.hmcts.divorce.caseworker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
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

    public static final  String VISIBILITY_INTERNAL = "Internal";
    public static final String TEXT_APPLICANT1_FLAGS_ROLE = "applicant or applicant1";
    public static final String TEXT_APPLICANT2_FLAGS_ROLE = "respondent or applicant2";
    public static final String TEXT_APPLICANT1_SOL_FLAGS_ROLE = "applicant or applicant1 solicitor";
    public static final String TEXT_APPLICANT2_SOL_FLAGS_ROLE = "respondent or applicant2 solicitor";

    public enum PartyFlagType {
        APPLICANT_1,
        APPLICANT_2,
        APPLICANT_1_SOLICITOR,
        APPLICANT_2_SOLICITOR
    }

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
                .visibility(VISIBILITY_INTERNAL)
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
                .visibility(VISIBILITY_INTERNAL)
                .build());
        }

        if (caseData.getPartyFlags().getApplicant2Flags() == null) {
            caseData.getPartyFlags().setApplicant2Flags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant2().getFullName())
                .roleOnCase(TEXT_APPLICANT2_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant2GroupId()))
                .visibility(VISIBILITY_INTERNAL)
                .build());
        }

        if (caseData.getApplicant1().isRepresented()
            && caseData.getPartyFlags().getApplicant1SolicitorFlags() == null) {
            caseData.getPartyFlags().setApplicant1SolicitorFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant1().getSolicitor().getName())
                .roleOnCase(TEXT_APPLICANT1_SOL_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant1SolicitorGroupId()))
                .visibility(VISIBILITY_INTERNAL)
                .build());
        }

        if (caseData.getApplicant2().isRepresented()
            && caseData.getPartyFlags().getApplicant2SolicitorFlags() == null) {
            caseData.getPartyFlags().setApplicant2SolicitorFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(caseData.getApplicant2().getSolicitor().getName())
                .roleOnCase(TEXT_APPLICANT2_SOL_FLAGS_ROLE)
                .groupId(UUID.fromString(caseData.getPartyFlags().getApplicant2SolicitorGroupId()))
                .visibility(VISIBILITY_INTERNAL)
                .build());
        }
    }

    public void updatePartyNameInCaseFlags(final CaseData caseData, PartyFlagType type) {
        if (type == PartyFlagType.APPLICANT_1) {
            if (caseData.getPartyFlags().getApplicant1Flags() != null) {
                caseData.getPartyFlags().getApplicant1Flags().setPartyName(caseData.getApplicant1().getFullName());
            }
        } else if (type == PartyFlagType.APPLICANT_2) {
            if (caseData.getPartyFlags().getApplicant2Flags() != null) {
                caseData.getPartyFlags().getApplicant2Flags().setPartyName(caseData.getApplicant2().getFullName());
            }
        } else if (type == PartyFlagType.APPLICANT_1_SOLICITOR) {
            if (caseData.getPartyFlags().getApplicant1SolicitorFlags() != null) {
                caseData.getPartyFlags().getApplicant1SolicitorFlags().setPartyName(caseData.getApplicant1().getSolicitor().getName());
            }
        } else if (type == PartyFlagType.APPLICANT_2_SOLICITOR) {
            if (caseData.getPartyFlags().getApplicant2SolicitorFlags() != null) {
                caseData.getPartyFlags().getApplicant2SolicitorFlags().setPartyName(caseData.getApplicant2().getSolicitor().getName());
            }
        }
    }

    public void switchCaseFlags(final CaseData caseData) {
        Flags app1Flags = caseData.getPartyFlags().getApplicant1Flags();
        Flags app2Flags = caseData.getPartyFlags().getApplicant2Flags();

        caseData.getPartyFlags().setApplicant1Flags(app2Flags);
        if (app2Flags != null) {
            caseData.getPartyFlags().getApplicant1Flags().setRoleOnCase(TEXT_APPLICANT1_FLAGS_ROLE);
            caseData.getPartyFlags().getApplicant1Flags().setGroupId(UUID.fromString(caseData.getPartyFlags().getApplicant1GroupId()));
        }

        caseData.getPartyFlags().setApplicant2Flags(app1Flags);
        if (app1Flags != null) {
            caseData.getPartyFlags().getApplicant2Flags().setRoleOnCase(TEXT_APPLICANT2_FLAGS_ROLE);
            caseData.getPartyFlags().getApplicant2Flags().setGroupId(UUID.fromString(caseData.getPartyFlags().getApplicant2GroupId()));
        }

        Flags app1SolFlags = caseData.getPartyFlags().getApplicant1SolicitorFlags();
        Flags app2SolFlags = caseData.getPartyFlags().getApplicant2SolicitorFlags();

        caseData.getPartyFlags().setApplicant1SolicitorFlags(app2SolFlags);
        if (app2SolFlags != null) {
            caseData.getPartyFlags().getApplicant1SolicitorFlags().setRoleOnCase(TEXT_APPLICANT1_SOL_FLAGS_ROLE);
            caseData.getPartyFlags().getApplicant1SolicitorFlags().setGroupId(
                UUID.fromString(caseData.getPartyFlags().getApplicant1SolicitorGroupId()));
        }

        caseData.getPartyFlags().setApplicant2SolicitorFlags(app1SolFlags);
        if (app1SolFlags != null) {
            caseData.getPartyFlags().getApplicant2SolicitorFlags().setRoleOnCase(TEXT_APPLICANT2_SOL_FLAGS_ROLE);
            caseData.getPartyFlags().getApplicant2SolicitorFlags().setGroupId(
                UUID.fromString(caseData.getPartyFlags().getApplicant2SolicitorGroupId()));
        }
    }

    public void resetSolicitorCaseFlags(final CaseData caseData, boolean isApplicant) {
        if (isApplicant) {
            caseData.getPartyFlags().setApplicant1SolicitorFlags(null);
        } else {
            caseData.getPartyFlags().setApplicant2SolicitorFlags(null);
        }
    }
}
