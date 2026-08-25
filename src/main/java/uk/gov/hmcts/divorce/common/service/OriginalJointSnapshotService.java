package uk.gov.hmcts.divorce.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.OriginalJointPartySnapshot;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;

@Service
@RequiredArgsConstructor
public class OriginalJointSnapshotService {
    private final Clock clock;

    public void captureIfAbsent(CaseData caseData) {
        if (caseData == null || caseData.getApplication() == null) {
            return;
        }

        if (caseData.getApplication().getOriginalJointPartySnapshot() != null) {
            return;
        }

        ApplicationType appType = caseData.getApplicationType();
        if (appType != JOINT_APPLICATION) {
            return;
        }

        OriginalJointPartySnapshot snapshot = OriginalJointPartySnapshot.builder()
            .originalApplicationType(JOINT_APPLICATION)
            .originalApplicant1FullName(caseData.getApplicant1().getFullName())
            .originalApplicant2FullName(caseData.getApplicant2().getFullName())
            .capturedDate(LocalDate.now(clock))
            .build();

        caseData.getApplication().setOriginalJointPartySnapshot(snapshot);
    }
}
