package uk.gov.hmcts.divorce.caseworker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.updater.MiniApplication;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

@Service
public class IssueApplicationService {

    @Autowired
    private MiniApplication miniApplication;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseData caseData,
                                                                       final Long caseId,
                                                                       final LocalDate createdDate,
                                                                       final String idamAuthToken) {

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            miniApplication
        );

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .createdDate(createdDate)
            .userAuthToken(idamAuthToken)
            .build();

        final CaseData responseData = caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(responseData)
            .build();
    }
}
