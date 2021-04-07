package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataContext;
import uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdater;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;

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
    private SolicitorOrganisationPolicyReference solicitorOrganisationPolicyReference;

    @Autowired
    private uk.gov.hmcts.reform.divorce.caseapi.util.CaseDataUpdaterChainFactory caseDataUpdaterChainFactory;

    public CaseData aboutToSubmit(final CaseData caseData) {

        final List<CaseDataUpdater> caseDataUpdaters = asList(
            claimsCost,
            solicitorCourtDetails,
            solicitorOrganisationPolicyReference);

        final CaseDataContext caseDataContext = CaseDataContext.builder()
            .caseData(caseData)
            .build();

        return caseDataUpdaterChainFactory
            .createWith(caseDataUpdaters)
            .processNext(caseDataContext)
            .getCaseData();
    }
}
