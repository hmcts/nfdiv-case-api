package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.task.UpdateCourtHearingDetailsTask;
import uk.gov.hmcts.divorce.bulkaction.task.UpdatePronouncementJudgeDetailsTask;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
@Slf4j
public class ScheduleCaseService {

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamService idamService;

    @Autowired
    private BulkCaseProcessingService bulkCaseProcessingService;

    @Autowired
    private UpdateCourtHearingDetailsTask updateCourtHearingDetailsTask;

    @Autowired
    private UpdatePronouncementJudgeDetailsTask updatePronouncementJudgeDetailsTask;

    @Async
    public void updateCourtHearingDetailsForCasesInBulk(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails
    ) {

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        bulkCaseProcessingService.updateBulkCase(
            bulkCaseDetails,
            updateCourtHearingDetailsTask,
            user,
            serviceAuth);
    }

    @Async
    public void updatePronouncementJudgeDetailsForCasesInBulk(final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails
    ) {

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();

        bulkCaseProcessingService.updateBulkCase(
            bulkCaseDetails,
            updatePronouncementJudgeDetailsTask,
            user,
            serviceAuth);
    }
}
