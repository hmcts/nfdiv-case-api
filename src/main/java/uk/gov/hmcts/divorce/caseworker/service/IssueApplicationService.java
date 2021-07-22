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
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChainFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private GenerateMiniApplication generateMiniApplication;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

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

        CaseData updatedCaseData = caseDataUpdaterChainFactory
            .createWith(issueApplicationUpdaters(caseData))
            .processNext(caseDataContext)
            .getCaseData();

        updatedCaseData.getApplication().setIssueDate(LocalDate.now(clock));

        return updatedCaseData;
    }

    private List<CaseDataUpdater> issueApplicationUpdaters(final CaseData caseData) {

        final ArrayList<CaseDataUpdater> caseDataUpdaters = new ArrayList<>();

        if (caseData.getApplicant2().isRepresented()) {
            caseDataUpdaters.add(generateRespondentSolicitorAosInvitation);
        }

        caseDataUpdaters.add(generateMiniApplication);
        caseDataUpdaters.add(sendAosPack);
        caseDataUpdaters.add(sendAosNotifications);

        return caseDataUpdaters;
    }
}
