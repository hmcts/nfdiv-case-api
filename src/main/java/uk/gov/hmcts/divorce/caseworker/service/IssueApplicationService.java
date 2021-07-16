package uk.gov.hmcts.divorce.caseworker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.caseworker.service.updater.MiniApplication;
import uk.gov.hmcts.divorce.caseworker.service.updater.RespondentSolicitorAosInvitation;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataContext;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.divorcecase.updater.CaseDataUpdaterChainFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

@Service
public class IssueApplicationService {

    @Autowired
    private MiniApplication miniApplication;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private RespondentSolicitorAosInvitation respondentSolicitorAosInvitation;

    @Autowired
    private Clock clock;

    public CaseData aboutToSubmit(final CaseData caseData,
                                  final Long caseId,
                                  final LocalDate createdDate,
                                  final String idamAuthToken) {

        List<CaseDataUpdater> caseDataUpdaters;

        if (caseData.getApplicant2().getSolicitorRepresented().toBoolean()) {
            caseDataUpdaters = asList(
                miniApplication,
                respondentSolicitorAosInvitation
            );
        } else {
            caseDataUpdaters = singletonList(miniApplication);
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
