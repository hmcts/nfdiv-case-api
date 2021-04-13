package uk.gov.hmcts.divorce.api.service.solicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.util.CaseDataContext;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdater;
import uk.gov.hmcts.divorce.api.util.CaseDataUpdaterChainFactory;

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
    private CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    public CaseData aboutToSubmit(
        final CaseData caseData,
        final Long caseId,
        final String idamAuthToken
    ) {

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            claimsCost,
            solicitorCourtDetails);

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
