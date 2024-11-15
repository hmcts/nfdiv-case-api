package uk.gov.hmcts.divorce.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            .as("Should return exactly 1 match on about-to-start event")
            .hasSize(1);
        verify(ccdSearchService).searchForAllCasesWithQuery(any(), any(), any());
    }


    @Test
    void shouldNotAddDuplicateReferenceMatches() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetailsWithExistingMatch();
        CaseData caseData = caseDetails.getData();

        mockDependencies(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerFindMatches.aboutToStart(caseDetails);

        assertThat(response.getData().getCaseMatches())
            .as("Should not add duplicate matches but include distinct match")
            .hasSize(1);

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
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(mock(User.class));
        when(authTokenGenerator.generate()).thenReturn("serviceAuthToken");
        when(ccdSearchService.searchForAllCasesWithQuery(any(), any(), any())).thenReturn(mockCaseMatchDetails());
        when(objectMapper.convertValue(any(Map.class), eq(CaseData.class))).thenReturn(caseData);
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
        mockCaseData.put("marriageDate", "2000-01-01");
        return mockCaseData;
    }

    // Test input trying to match all possibles encountered in prod
    static List<String> provideTestInputs() {
        return List.of(
            "Willy Wonka ",                        // Trailing space
            "Willy Wonka (name changed by Deed Poll)", // Parentheses with trailing space
            "Willy Wonka.=",                      // Illegal characters at the end
            "Willy Wonka***",                     // Trailing asterisks
            "Willy Wonka!",                       // Exclamation mark at the end
            "Willy Wonka (formerly waldo)",          // Parentheses at the end
            "Willy Wonka.=",                  // Mix of illegal characters
            "_Willy Wonka_",                  // Underscores around the name
            "Willy Wonka / Mr. Ritchie "
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestInputs")
    void testRegexPatternAfterClean(String input) {
        // Assert that all inputs match to "Willy Wonka"
        String[] cleanedName = caseworkerFindMatches.normalizeAndSplit(input);
        for (int i = 0; i < cleanedName.length; i++) {
            String regexPattern = caseworkerFindMatches.generateRegexPattern(cleanedName[i]);
            Pattern pattern = Pattern.compile(regexPattern);
            // Verify that the regex matches the cleaned name
            String expectedName = (i == 0) ? "Willy Wonka" : "Mr Ritchie";
            assertTrue(pattern.matcher(expectedName).matches(), cleanedName[i]);
        }
    }
}
