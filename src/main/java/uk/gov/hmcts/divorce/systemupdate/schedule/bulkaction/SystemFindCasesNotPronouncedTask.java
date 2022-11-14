package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState.Pronounced;

@Component
@Slf4j
public class SystemFindCasesNotPronouncedTask implements Runnable {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;


    @Override
    public void run() {

        log.info("SystemFindCasesNotPronouncedTask scheduled task started");

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuth = authTokenGenerator.generate();


        List<String> caseReferences = new ArrayList<>();
        try {

            caseReferences.add("1666695210705679");
            List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetails = ccdSearchService.searchForCases(caseReferences, user, serviceAuth);

            System.out.println("caseDetails size " + caseDetails.size());
            for (uk.gov.hmcts.reform.ccd.client.model.CaseDetails cd : caseDetails) {
                if (!"ConditionalOrderPronounced".equals(cd.getState())) {
                    System.out.println("NFD Case Id " + cd.getId() + "not in co pronounced state");
                }
            }


        } catch (final CcdSearchCaseException e) {
            log.error("SystemFindCasesNotPronouncedTask schedule task, stopped after search error", e);
        }
    }
}
