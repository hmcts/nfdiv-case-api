package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniApplicationRemover;
import uk.gov.hmcts.divorce.solicitor.service.updater.MiniPetitionDraft;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;

@Service
@Slf4j
public class SolicitorUpdateApplicationService {

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    @Autowired
    private MiniApplicationRemover miniApplicationRemover;

    @Autowired
    private MiniPetitionDraft miniPetitionDraft;

    public CaseData aboutToSubmit(final CaseData caseData,
                                  final Long caseId,
                                  final LocalDate createdDate,
                                  final String idamAuthToken) {

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            miniApplicationRemover,
            miniPetitionDraft
        );

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .createdDate(createdDate)
            .userAuthToken(idamAuthToken)
            .build();

        return caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();
    }
}
