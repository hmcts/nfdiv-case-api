package uk.gov.hmcts.divorce.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.caseworker.service.updater.MiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.updater.RespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.caseworker.service.updater.SendAosNotifications;
import uk.gov.hmcts.divorce.caseworker.service.updater.SendAosPack;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChainFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

@Service
@Slf4j
public class IssueApplicationService {

    @Autowired
    private MiniApplication miniApplication;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private RespondentSolicitorAosInvitation respondentSolicitorAosInvitation;

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

        List<CaseDataUpdater> caseDataUpdaters;

        if (caseData.getApplicant2().isRepresented()) {
            caseDataUpdaters = asList(
                miniApplication,
                respondentSolicitorAosInvitation,
                sendAosPack,
                sendAosNotifications
            );
        } else {
            //TODO: AosInvitation needs to be added for this branch, AoS will not be sent until available
            caseDataUpdaters = asList(
                miniApplication,
                sendAosPack,
                sendAosNotifications
            );
        }

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .createdDate(createdDate)
            .userAuthToken(idamAuthToken)
            .build();

        CaseData updatedCaseData = caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();

        updatedCaseData.getApplication().setIssueDate(LocalDate.now(clock));

        return updatedCaseData;
    }
}
