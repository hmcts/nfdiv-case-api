package uk.gov.hmcts.divorce.caseworker.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseMatch;
import uk.gov.hmcts.divorce.divorcecase.model.MarriageDetails;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CaseworkerFindMatchesTest {

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
    void shouldNotAddDuplicateMatches() {
        CaseDetails<CaseData, State> caseDetails = buildCaseDetailsWithExistingMatch();
        CaseData caseData = caseDetails.getData();

        mockDependencies(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerFindMatches.aboutToStart(caseDetails);

        assertThat(response.getData().getCaseMatches())
            .as("Should not add duplicate matches but include 2 distinct matches")
            .hasSize(2);

    }

    @Test
    void testAddMatches() {
        CaseData caseData = buildEmptyCaseData();

        List<CaseMatch> newMatches = List.of(
            CaseMatch.builder()
                .applicant1Name("John Doe")
                .applicant2Name("Jane Doe")
                .date(LocalDate.of(2000, 1, 1))
                .caseLink(CaseLink.builder().caseReference("67890").build())
                .build()
        );

        caseworkerFindMatches.addMatches(caseData, newMatches);

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
                    .applicant1Name("John Doe")
                    .applicant2Name("Jane Doe")
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
        mockCaseDetails.setData(mockCaseData());
        return List.of(mockCaseDetails);
    }

    private Map<String, Object> mockCaseData() {
        Map<String, Object> mockCaseData = new HashMap<>();
        mockCaseData.put("marriageApplicant1Name", "John Doe");
        mockCaseData.put("marriageApplicant2Name", "Jane Doe");
        mockCaseData.put("marriageDate", "2000-01-01");
        return mockCaseData;
    }
}
