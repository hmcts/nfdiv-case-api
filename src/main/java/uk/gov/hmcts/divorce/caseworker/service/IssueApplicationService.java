package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.caseworker.service.updater.GenerateMiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.updater.GenerateRespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.updater.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.updater.SendAosPack;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Function;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private GenerateMiniApplication generateMiniApplication;

    @Autowired
    private GenerateRespondentSolicitorAosInvitation generateRespondentSolicitorAosInvitation;

    @Autowired
    private SendAosPack sendAosPack;

    @Autowired
    private SendAosNotifications sendAosNotifications;

    @Autowired
    private Clock clock;

    public CaseData aboutToSubmit(final CaseData caseData,
                                  final Long caseId,
                                  final LocalDate createdDate,
                                  final String idamAuthToken) {

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .createdDate(createdDate)
            .userAuthToken(idamAuthToken)
            .build();

        final CaseData updatedCaseData = issueApplicationActions(caseData)
            .apply(caseDataContext)
            .getCaseData();

        updatedCaseData.getApplication().setIssueDate(LocalDate.now(clock));

        return updatedCaseData;
    }

    private Function<CaseDataContext, CaseDataContext> issueApplicationActions(final CaseData caseData) {

        final ArrayList<Function<CaseDataContext, CaseDataContext>> caseDataUpdaters = new ArrayList<>();

        if (caseData.getApplicant2().isRepresented()) {
            caseDataUpdaters.add(generateRespondentSolicitorAosInvitation);
        }

        caseDataUpdaters.add(generateMiniApplication);
        caseDataUpdaters.add(sendAosPack);
        caseDataUpdaters.add(sendAosNotifications);

        return caseDataUpdaters.stream()
            .reduce(Function.identity(), Function::andThen);
    }
}
