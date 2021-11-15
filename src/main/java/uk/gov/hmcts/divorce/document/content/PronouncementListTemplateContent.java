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
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BULK_LIST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COSTS_GRANTED;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;

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

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, Object> apply(final BulkActionCaseData caseData,
                                     final Long bulkListCaseId,
                                     final LocalDate createdDate) {

        final DateTimeFormatter hearingDateFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.UK);
        final DateTimeFormatter hearingTimeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.UK);

        final Map<String, Object> templateContent = new HashMap<>();
        final List<Map<String, Object>> bulkList = new ArrayList<>();

        log.info("Creating document content for bulk case id {} title {}", bulkListCaseId, caseData.getCaseTitle());

        for (CaseDetails mainCase : retrieveBulkListCases(bulkListCaseId)) {

            Map<String, Object> caseLinkMap = new HashMap<>();

            var mainCaseData = objectMapper.convertValue(mainCase.getData(), CaseData.class);

            caseLinkMap.put(CASE_REFERENCE, mainCase.getId());
            caseLinkMap.put(APPLICANT_HEADING, format("%s %s", mainCaseData.getApplicant1().getFirstName(),
                mainCaseData.getApplicant1().getLastName()));
            caseLinkMap.put(RESPONDENT_HEADING, format("%s %s", mainCaseData.getApplicant2().getFirstName(),
                mainCaseData.getApplicant2().getLastName()));
            caseLinkMap.put(COSTS_GRANTED, mainCaseData.getConditionalOrder().getClaimsGranted());
            bulkList.add(caseLinkMap);
        }
        templateContent.put(PRONOUNCEMENT_JUDGE, caseData.getPronouncementJudge());
        templateContent.put(COURT_NAME, caseData.getCourt().getLabel());
        templateContent.put(DATE_OF_HEARING, caseData.getDateAndTimeOfHearing().format(hearingDateFormat));
        templateContent.put(TIME_OF_HEARING, caseData.getDateAndTimeOfHearing().format(hearingTimeFormat));
        templateContent.put(BULK_LIST, bulkList);

        return templateContent;
    }

    public List<CaseDetails> retrieveBulkListCases(Long bulkListCaseReference) {

        final User user = idamService.retrieveSystemUpdateUserDetails();
        final String serviceAuthorization = authTokenGenerator.generate();

        final BoolQueryBuilder query =
            boolQuery()
                .must(matchQuery("data.bulkListCaseReference", bulkListCaseReference));

        log.info("Starting to search for cases with BulkList reference {}", bulkListCaseReference);
        final List<CaseDetails> bulkListCases =
            ccdSearchService.searchForAllCasesWithQuery(State.AwaitingPronouncement, query, user, serviceAuthorization);

        for (final CaseDetails caseDetails : bulkListCases) {
            final CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);
            log.info("Found case in Bulk List {} Name {}", caseDetails.getId(), caseData.getApplicant1().getLastName());
        }
        return bulkListCases;
    }
}
