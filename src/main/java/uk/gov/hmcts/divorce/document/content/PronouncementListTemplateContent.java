package uk.gov.hmcts.divorce.document.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

@Component
@Slf4j
public class PronouncementListTemplateContent {

    @Autowired
    private CcdSearchService ccdSearchService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdamService idamService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;


    public Map<String, Object> apply(final BulkActionCaseData caseData,
                                     final Long bulkListCaseId,
                                     final LocalDate createdDate) {

        final DateTimeFormatter hearingDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.UK);
        final DateTimeFormatter hearingTimeFormat = DateTimeFormatter.ofPattern("hh:mma", Locale.UK);

        final Map<String, Object> templateContent = new HashMap<>();
        final List<Map<String,Object>> bulkList = new ArrayList<>();
        final Map<String, Object> caseLinkMap = new HashMap<>();
        Long mainCaseReference;

        log.info("For ccd bulk case id {} title {}", bulkListCaseId, caseData.getCaseTitle());

        for (CaseDetails mainCase : retrieveBulkListCases(bulkListCaseId)) {

            mainCaseReference = mainCase.getId();
            log.info("Main Case Id {}", mainCaseReference);

            var mainCaseData = objectMapper.convertValue(mainCase.getData(), CaseData.class);

            caseLinkMap.put("caseReference", mainCaseReference);
            caseLinkMap.put("applicant", format("%s %s", mainCaseData.getApplicant1().getFirstName(),
                mainCaseData.getApplicant1().getLastName()));
            caseLinkMap.put("respondent", format("%s %s", mainCaseData.getApplicant2().getFirstName(),
                mainCaseData.getApplicant2().getLastName()));
            caseLinkMap.put("costsGranted", mainCaseData.getConditionalOrder().getClaimsGranted());
            bulkList.add(caseLinkMap);
        }

        templateContent.put("pronouncementJudge", caseData.getPronouncementJudge());
        templateContent.put("courtName", caseData.getCourtName());
        templateContent.put("dateOfHearing", caseData.getDateAndTimeOfHearing().format(hearingDateFormat));
        templateContent.put("timeOfHearing", caseData.getDateAndTimeOfHearing().format(hearingTimeFormat));
        templateContent.put("bulkList", bulkList);

        return templateContent;
    }

    private List<CaseDetails> retrieveBulkListCases(Long bulkListCaseReference) {

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        final BoolQueryBuilder query =
            boolQuery()
                .must(matchQuery("data.bulkListCaseReference", bulkListCaseReference));

        final List<CaseDetails> bulkListCases =
            ccdSearchService.searchForAllCasesWithQuery(State.AwaitingPronouncement, query, user, serviceAuthorization);

        log.info("Starting to search for cases in BulkList {}", bulkListCaseReference);
        for (final CaseDetails caseDetails : bulkListCases) {
            final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
            log.info("Found case in Bulk List {} Name {}", caseDetails.getId(), caseData.getApplicant1().getLastName());
        }
        return bulkListCases;
    }
}
