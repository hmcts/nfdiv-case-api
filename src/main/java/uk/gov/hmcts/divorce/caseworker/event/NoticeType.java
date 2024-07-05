package uk.gov.hmcts.divorce.caseworker.event;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.List;

public enum NoticeType {
    NEW_DIGITAL_SOLICITOR_NEW_ORG() {
        @Override
        public void applyNoticeOfChange(Applicant applicant,
                                        Applicant applicantBefore,
                                        List<String> roles,
                                        String solicitorRole,
                                        CaseDetails<CaseData, State> details,
                                        NoticeOfChangeService noticeOfChangeService) {

            noticeOfChangeService.applyNocDecisionAndGrantAccessToNewSol(
                details.getId(),
                applicant,
                applicantBefore,
                roles,
                solicitorRole);

        }
    },
    NEW_DIGITAL_SOLICITOR_EXISTING_ORG() {
        @Override
        public void applyNoticeOfChange(Applicant applicant,
                                        Applicant applicantBefore,
                                        List<String> roles,
                                        String solicitorRole,
                                        CaseDetails<CaseData, State> details,
                                        NoticeOfChangeService noticeOfChangeService) {

            noticeOfChangeService.changeAccessWithinOrganisation(
                applicant.getSolicitor(),
                roles,
                solicitorRole,
                details.getId());
        }
    },
    ORG_REMOVED() {
        @Override
        public void applyNoticeOfChange(Applicant applicant,
                                        Applicant applicantBefore,
                                        List<String> roles,
                                        String solicitorRole,
                                        CaseDetails<CaseData, State> details,
                                        NoticeOfChangeService noticeOfChangeService) {

            noticeOfChangeService.revokeCaseAccess(details.getId(), applicantBefore, roles);
        }
    },
    OFFLINE_NOC() {
        @Override
        public void applyNoticeOfChange(Applicant applicant,
                                        Applicant applicantBefore,
                                        List<String> roles,
                                        String solicitorRole,
                                        CaseDetails<CaseData, State> details,
                                        NoticeOfChangeService noticeOfChangeService) {

        }
    };

    public abstract void applyNoticeOfChange(Applicant applicant,
                                             Applicant applicantBefore,
                                             List<String> roles,
                                             String solicitorRole,
                                             CaseDetails<CaseData, State> details,
                                             NoticeOfChangeService noticeOfChangeService);

}
