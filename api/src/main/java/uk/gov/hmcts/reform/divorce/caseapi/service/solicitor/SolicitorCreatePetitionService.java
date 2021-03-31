package uk.gov.hmcts.reform.divorce.caseapi.service.solicitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
    private MiniPetitionDraft miniPetitionDraft;

    @Autowired
    private SolicitorOrganisationPolicyReference solicitorOrganisationPolicyReference;

    public CaseData aboutToSubmit(final CaseData caseData) {

        final List<Handler<CaseData>> handlers = asList(
            claimsCost,
            solicitorCourtDetails,
            miniPetitionDraft,
            solicitorOrganisationPolicyReference);

        handlers.forEach(caseDataHandler -> caseDataHandler.handle(caseData));

        return caseData;
    }
}
