package uk.gov.hmcts.divorce.document.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.NA;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BULK_LIST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RESPONDENT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLE_JOINT_HEADING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APPLICANT_2_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrderQuestions;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondent;

@ExtendWith(MockitoExtension.class)
public class PronouncementListTemplateContentTest {

    @InjectMocks
    PronouncementListTemplateContent pronouncementListTemplateContentService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldUpdateTemplateContentMapWithExpectedData() {

        final LocalDateTime dateAndTimeOfHearing = LocalDateTime.of(2021, 11, 10, 12, 45, 0);
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .court(BURY_ST_EDMUNDS)
            .pronouncementJudge("District Judge")
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue(TEST_CASE_ID.toString())))
            .build();

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .id(TEST_CASE_ID)
                .caseTypeId(BulkActionCaseTypeConfig.getCaseType())
                .data(Map.of("bulkListCaseDetails", bulkActionCaseData))
                .build();

        final List<CaseDetails> caseDetailsList = mockCaseDetailsList();

        when(pronouncementListTemplateContentService.retrieveBulkListCases(bulkActionCaseDetails.getId(), bulkActionCaseData))
            .thenReturn(caseDetailsList);

        Map<String, Object> templateContent = pronouncementListTemplateContentService
            .apply(bulkActionCaseData,
                bulkActionCaseDetails.getId()
            );

        assertThat(templateContent).contains(
            entry(PRONOUNCEMENT_JUDGE, "District Judge"),
            entry(COURT_NAME, BURY_ST_EDMUNDS.getLabel()),
            entry(DATE_OF_HEARING, "10 November 2021"),
            entry(TIME_OF_HEARING, "12:45 pm"));

        assertThat(templateContent).containsKey(BULK_LIST);

        List<Map<String, Object>> expectedBulkList = List.of(
            expectedValues(DIVORCE.getLabel()),
            expectedValues(DISSOLUTION.getLabel()),
            expectedValues(JUDICIAL_SEPARATION.getLabel()),
            expectedValues(SEPARATION.getLabel())
        );

        assertThat(templateContent).contains(
            entry(BULK_LIST, expectedBulkList)
        );

    }

    @Test
    public void retrieveBulkListCasesShouldExcludeTheCasesToBeRemoved() {
        final var bulkCase1 = getBulkListCaseDetailsListValue("1");

        final var bulkCase2 = getBulkListCaseDetailsListValue("2");

        final var bulkCaseToBeRemoved = getBulkListCaseDetailsListValue("3");

        final LocalDateTime dateAndTimeOfHearing = LocalDateTime.of(2021, 11, 10, 12, 45, 0);
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .court(BURY_ST_EDMUNDS)
            .pronouncementJudge("District Judge")
            .bulkListCaseDetails(Lists.newArrayList(bulkCase1, bulkCase2, bulkCaseToBeRemoved))
            .casesToBeRemoved(Lists.newArrayList(bulkCaseToBeRemoved))
            .build();

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .id(TEST_CASE_ID)
                .caseTypeId(BulkActionCaseTypeConfig.getCaseType())
                .data(Map.of("bulkListCaseDetails", bulkActionCaseData))
                .build();

        final List<CaseDetails> bulkListCases = Lists.newArrayList(
            mockCaseDetails(TEST_CASE_ID, mockCaseData(DIVORCE, NA)),
            mockCaseDetails(2L, mockCaseData(DIVORCE, NA)),
            mockCaseDetails(3L, mockCaseData(DIVORCE, NA))
        );

        when(ccdSearchService.searchForAllCasesWithQuery(
            any(BoolQueryBuilder.class), any(User.class), anyString(), eq(AwaitingPronouncement))).thenReturn(bulkListCases);

        List<CaseDetails> caseDetails = pronouncementListTemplateContentService
            .retrieveBulkListCases(bulkActionCaseDetails.getId(), bulkActionCaseData);

        assertThat(caseDetails.stream().map(CaseDetails::getId)).containsExactlyInAnyOrder(TEST_CASE_ID, 2L);
    }

    @Test
    public void retrieveBulkListCasesShouldReturnAllCasesWhenNoCasesToBeRemoved() {

        final LocalDateTime dateAndTimeOfHearing = LocalDateTime.of(2021, 11, 10, 12, 45, 0);
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .court(BURY_ST_EDMUNDS)
            .pronouncementJudge("District Judge")
            .bulkListCaseDetails(Lists.newArrayList(
                getBulkListCaseDetailsListValue("1"),
                getBulkListCaseDetailsListValue("2"),
                getBulkListCaseDetailsListValue("3")))
            .build();

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .id(TEST_CASE_ID)
                .caseTypeId(BulkActionCaseTypeConfig.getCaseType())
                .data(Map.of("bulkListCaseDetails", bulkActionCaseData))
                .build();

        final List<CaseDetails> bulkListCases = Lists.newArrayList(
            mockCaseDetails(TEST_CASE_ID, mockCaseData(DIVORCE, NA)),
            mockCaseDetails(2L, mockCaseData(DIVORCE, NA)),
            mockCaseDetails(3L, mockCaseData(DIVORCE, NA))
        );

        when(ccdSearchService.searchForAllCasesWithQuery(
            any(BoolQueryBuilder.class), any(User.class), anyString(), eq(AwaitingPronouncement))).thenReturn(bulkListCases);

        List<CaseDetails> caseDetails = pronouncementListTemplateContentService
            .retrieveBulkListCases(bulkActionCaseDetails.getId(), bulkActionCaseData);

        assertThat(caseDetails.stream().map(CaseDetails::getId)).containsExactlyInAnyOrder(TEST_CASE_ID, 2L, 3L);
    }

    private Map<String, Object> expectedValues(String applicationType) {
        Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put(CASE_REFERENCE, TEST_CASE_ID);
        expectedValues.put(APPLICANT_HEADING, format("%s %s", TEST_FIRST_NAME, TEST_LAST_NAME));
        expectedValues.put(RESPONDENT_HEADING, format("%s %s", APPLICANT_2_FIRST_NAME, TEST_LAST_NAME));
        expectedValues.put(SOLE_JOINT_HEADING, "Sole");
        expectedValues.put(DIVORCE_OR_DISSOLUTION, applicationType);

        return expectedValues;
    }

    private List<CaseDetails> mockCaseDetailsList() {
        final CaseData caseData = mockCaseData(DIVORCE, NA);
        final CaseDetails caseDetails = mockCaseDetails(TEST_CASE_ID, caseData);
        final CaseData dissCaseData = mockCaseData(DISSOLUTION, NA);
        final CaseDetails dissCaseDetails = mockCaseDetails(TEST_CASE_ID, dissCaseData);
        final CaseData jsCaseData = mockCaseData(DIVORCE, JUDICIAL_SEPARATION);
        final CaseDetails jsCaseDetails = mockCaseDetails(TEST_CASE_ID, jsCaseData);
        final CaseData sepCaseData = mockCaseData(DISSOLUTION, SEPARATION);
        final CaseDetails sepCaseDetails = mockCaseDetails(TEST_CASE_ID, sepCaseData);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(dissCaseDetails.getData(), CaseData.class)).thenReturn(dissCaseData);
        when(objectMapper.convertValue(jsCaseDetails.getData(), CaseData.class)).thenReturn(jsCaseData);
        when(objectMapper.convertValue(sepCaseDetails.getData(), CaseData.class)).thenReturn(sepCaseData);

        return List.of(caseDetails, dissCaseDetails, jsCaseDetails, sepCaseDetails);
    }

    private CaseData mockCaseData(DivorceOrDissolution divorceOrDissolution, SupplementaryCaseType supplementaryCaseType) {
        return CaseData.builder()
            .applicant1(getApplicant())
            .applicant2(respondent())
            .applicationType(ApplicationType.SOLE_APPLICATION)
            .divorceOrDissolution(divorceOrDissolution)
            .supplementaryCaseType(supplementaryCaseType)
            .conditionalOrder(
                ConditionalOrder.builder()
                    .conditionalOrderApplicant1Questions(getConditionalOrderQuestions())
                    .build())
            .build();
    }

    private CaseDetails mockCaseDetails(Long id, CaseData caseData) {
        return CaseDetails
            .builder()
            .id(id)
            .caseTypeId(NoFaultDivorce.getCaseType())
            .data(Map.of("", caseData))
            .build();
    }
}
