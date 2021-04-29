package uk.gov.hmcts.divorce.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChainFactory;
import uk.gov.hmcts.divorce.solicitor.service.updater.ClaimsCost;
import uk.gov.hmcts.divorce.solicitor.service.updater.SolicitorCourtDetails;

import java.util.List;

import static java.util.Arrays.asList;

@Service
@Slf4j
public class SolicitorCreatePetitionService {

    @Autowired
    private ClaimsCost claimsCost;

    @Autowired
    private SolicitorCourtDetails solicitorCourtDetails;

    @Autowired
    private MiniPetitionDraft miniPetitionDraft;

    @Autowired
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    public CaseData aboutToSubmit(
        final CaseData caseData,
        final Long caseId,
        final String idamAuthToken
    ) {

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            claimsCost,
            solicitorCourtDetails,
            miniPetitionDraft);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .caseId(caseId)
            .userAuthToken(idamAuthToken)
            .build();

        return caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();
    }
}
