package uk.gov.hmcts.divorce.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDataOldDivorce;
import uk.gov.hmcts.divorce.divorcecase.model.CaseMatch;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerFindMatches.FIND_MATCHES;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

class CaseworkerFindMatchesTest {

    public static final String NAME_ONE = "John Doe";
    public static final String NAME_TWO = "Jane Doe";
    public static final String POSTCODE_1 = "AB1 2CD";
    public static final String POSTCODE_2 = "EF3 4GH";
    public static final String PETITIONER_TOWN = "PetitionerTown";
    public static final String RESPONDENT_TOWN = "RespondentTown";
    public static final String EXPECTED = "12345";
    public static final String MARRIAGE_DATE = "2000-01-01";
    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseworkerFindMatches caseworkerFindMatches;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        ConfigBuilder<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerFindMatches.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(FIND_MATCHES);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getName)
            .contains("Find matches");

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getDescription)
            .contains("Find matches");

    }

    @Test
    void shouldReturnMatchesOnAboutToStart() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetails();
        CaseData caseData = caseDetails.getData();

        mockDependencies(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerFindMatches.aboutToStart(caseDetails);

        assertThat(response.getData().getCaseMatches())
            .as("Should return exactly 2 match on about-to-start event")
            .hasSize(2);
        verify(ccdSearchService).searchForAllCasesWithQuery(any(), any(), any());
        verify(ccdSearchService).searchForOldDivorceCasesWithQuery(any(), any(), any());
    }


    @Test
    void shouldNotAddDuplicateReferenceMatches() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetailsWithExistingMatch();
        CaseData caseData = caseDetails.getData();

        mockDependencies(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerFindMatches.aboutToStart(caseDetails);

        assertThat(response.getData().getCaseMatches())
            .as("Should not add duplicate matches but include distinct match")
            .hasSize(2);

    }

    @Test
    void testAddMatches() {
        CaseData caseData = buildEmptyCaseData();

        List<CaseMatch> newMatches = List.of(
            CaseMatch.builder()
                .applicant1Name(NAME_ONE)
                .applicant2Name(NAME_TWO)
                .date(LocalDate.of(2000, 1, 1))
                .caseLink(CaseLink.builder().caseReference("67890").build())
                .build()
        );

        caseworkerFindMatches.setToNewMatches(caseData, newMatches);

        assertThat(caseData.getCaseMatches())
            .as("Should add exactly 1 new match to case data")
            .hasSize(1);
    }

    @Test
    void shouldSetCaseMatchesToNullWhenNewMatchesIsEmpty() {
        CaseData caseData = buildEmptyCaseData();

        ListValue<CaseMatch> existingMatch = ListValue.<CaseMatch>builder()
            .id("1")
            .value(CaseMatch.builder()
                .caseLink(CaseLink.builder().caseReference("123456").build())
                .build())
            .build();
        caseData.getCaseMatches().add(existingMatch);

        List<CaseMatch> newMatches = new ArrayList<>();

        caseworkerFindMatches.setToNewMatches(caseData, newMatches);

        assertThat(caseData.getCaseMatches())
            .as("Should set case matches to null when new matches list is empty")
            .isNull();
    }


    private CaseDetails<CaseData, State> buildCaseDetails() {
        CaseData caseData = buildEmptyCaseData();
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);
        return caseDetails;
    }

    private CaseData buildEmptyCaseData() {
        return CaseData.builder()
            .caseMatches(new ArrayList<>())
            .application(Application.builder()
                .marriageDetails(MarriageDetails.builder()
                    .applicant1Name(NAME_ONE)
                    .applicant2Name(NAME_TWO)
                    .date(LocalDate.of(2000, 1, 1))
                    .build())
                .build())
            .build();
    }

    private CaseDetails<CaseData, State> buildCaseDetailsWithExistingMatch() {
        CaseData caseData = buildEmptyCaseData();
        ListValue<CaseMatch> existingMatch = ListValue.<CaseMatch>builder()
            .id("1")
            .value(CaseMatch.builder()
                .caseLink(CaseLink.builder().caseReference("123456").build())
                .build())
            .build();
        caseData.getCaseMatches().add(existingMatch);

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(12345L);
        caseDetails.setData(caseData);
        caseDetails.setState(State.Holding);
        return caseDetails;
    }

    private void mockDependencies(CaseData caseData) {
        String petitionerName = caseData.getApplication().getMarriageDetails().getApplicant1Name();
        String respondentName = caseData.getApplication().getMarriageDetails().getApplicant2Name();
        String marriageDate = caseData.getApplication().getMarriageDetails().getDate().toString();

        CaseDataOldDivorce oldCaseData = mockOldDivorceCaseData(petitionerName, respondentName, marriageDate);

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(mock(User.class));
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");

        when(ccdSearchService.searchForAllCasesWithQuery(any(), any(), any()))
            .thenReturn(mockCaseMatchDetails());
        when(objectMapper.convertValue(any(Map.class), eq(CaseData.class)))
            .thenReturn(caseData);
        when(objectMapper.convertValue(any(Map.class), eq(CaseDataOldDivorce.class)))
            .thenReturn(oldCaseData);
        when(ccdSearchService.searchForOldDivorceCasesWithQuery(any(), any(), any()))
            .thenReturn(mockCaseMatchTwoDetails(oldCaseData));
    }

    private CaseDataOldDivorce mockOldDivorceCaseData(String petitionerName, String respondentName, String marriageDate) {
        CaseDataOldDivorce oldCaseData = new CaseDataOldDivorce();
        oldCaseData.setD8MarriagePetitionerName(petitionerName);
        oldCaseData.setD8MarriageRespondentName(respondentName);
        oldCaseData.setD8MarriageDate(marriageDate);
        oldCaseData.setD8PetitionerPostCode("OLD1 1AA");
        oldCaseData.setD8RespondentPostCode("OLD2 2BB");
        oldCaseData.setD8PetitionerPostTown("Old Petitioner Town");
        oldCaseData.setD8RespondentPostTown("Old Respondent Town");
        return oldCaseData;
    }

    private List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> mockCaseMatchTwoDetails(CaseDataOldDivorce oldCaseData) {
        Map<String, Object> mockDataMap = new HashMap<>();
        mockDataMap.put("D8MarriagePetitionerName", oldCaseData.getD8MarriagePetitionerName());
        mockDataMap.put("D8MarriageRespondentName", oldCaseData.getD8MarriageRespondentName());
        mockDataMap.put("D8MarriageDate", oldCaseData.getD8MarriageDate());
        mockDataMap.put("D8PetitionerPostCode", oldCaseData.getD8PetitionerPostCode());
        mockDataMap.put("D8RespondentPostCode", oldCaseData.getD8RespondentPostCode());
        mockDataMap.put("D8PetitionerPostTown", oldCaseData.getD8PetitionerPostTown());
        mockDataMap.put("D8RespondentPostTown", oldCaseData.getD8RespondentPostTown());

        uk.gov.hmcts.reform.ccd.client.model.CaseDetails mockCaseDetails =
            uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                .id(67894L)
                .state(State.Holding.name())
                .data(mockDataMap)
                .build();

        return List.of(mockCaseDetails);
    }


    private List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> mockCaseMatchDetails() {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails mockCaseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
            .build();
        mockCaseDetails.setId(67890L);
        mockCaseDetails.setState(State.Holding.name());
        mockCaseDetails.setData(mockCaseData());
        return List.of(mockCaseDetails);
    }

    private Map<String, Object> mockCaseData() {
        Map<String, Object> mockCaseData = new HashMap<>();
        mockCaseData.put("marriageApplicant1Name", NAME_ONE);
        mockCaseData.put("marriageApplicant2Name", NAME_TWO);
        mockCaseData.put("marriageDate", MARRIAGE_DATE);
        return mockCaseData;
    }

    @Test
    void shouldTransformOldCaseToMatchingCasesList() {
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetail = mock(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.class);
        Map<String, Object> mockData = Map.of(
            "D8MarriagePetitionerName", NAME_ONE,
            "D8MarriageRespondentName", NAME_TWO,
            "D8MarriageDate", "2000-01-01",
            "D8PetitionerPostCode", "AB1 2CD",
            "D8RespondentPostCode", "EF3 4GH",
            "D8PetitionerPostTown", "PetitionerTown",
            "D8RespondentPostTown", "RespondentTown"
        );
        when(caseDetail.getData()).thenReturn(mockData);
        when(caseDetail.getId()).thenReturn(12345L);

        CaseDataOldDivorce caseDataOldDivorce = new CaseDataOldDivorce();
        caseDataOldDivorce.setD8MarriagePetitionerName(NAME_ONE);
        caseDataOldDivorce.setD8MarriageRespondentName(NAME_TWO);
        caseDataOldDivorce.setD8MarriageDate(MARRIAGE_DATE);
        caseDataOldDivorce.setD8PetitionerPostCode(POSTCODE_1);
        caseDataOldDivorce.setD8RespondentPostCode(POSTCODE_2);
        caseDataOldDivorce.setD8PetitionerPostTown(PETITIONER_TOWN);
        caseDataOldDivorce.setD8RespondentPostTown(RESPONDENT_TOWN);

        when(objectMapper.convertValue(mockData, CaseDataOldDivorce.class)).thenReturn(caseDataOldDivorce);

        List<CaseMatch> caseMatches = caseworkerFindMatches.transformOldCaseToMatchingCasesList(Collections.singletonList(caseDetail));

        assertThat(caseMatches).hasSize(1);
        CaseMatch caseMatch = caseMatches.get(0);

        assertThat(caseMatch.getApplicant1Name()).isEqualTo(NAME_ONE);
        assertThat(caseMatch.getApplicant2Name()).isEqualTo(NAME_TWO);
        assertThat(caseMatch.getDate()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(caseMatch.getApplicant1Postcode()).isEqualTo(POSTCODE_1);
        assertThat(caseMatch.getApplicant2Postcode()).isEqualTo(POSTCODE_2);
        assertThat(caseMatch.getApplicant1Town()).isEqualTo(PETITIONER_TOWN);
        assertThat(caseMatch.getApplicant2Town()).isEqualTo(RESPONDENT_TOWN);
        assertThat(caseMatch.getCaseLink().getCaseReference()).isEqualTo(EXPECTED);
    }
}
