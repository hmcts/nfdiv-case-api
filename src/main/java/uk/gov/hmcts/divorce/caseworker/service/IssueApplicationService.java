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

        if (caseData.getApplicant2().isRepresented()) {
            generateRespondentSolicitorAosInvitation.accept(caseDataContext);
        }

        generateMiniApplication.accept(caseDataContext);
        sendAosPack.accept(caseDataContext);
        sendAosNotifications.accept(caseDataContext);

        caseData.getApplication().setIssueDate(LocalDate.now(clock));

        return caseData;
    }
}
