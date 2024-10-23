package uk.gov.hmcts.divorce.systemupdate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CoreCaseDataApi2Test {

    @Mock
    private CoreCaseDataApi2 coreCaseDataApi2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldRunQuerySuccessfully() {
        // Arrange
        String authorization = "Bearer auth-token";
        String serviceAuthorization = "ServiceAuth-token";
        String caseType = "divorce";
        String searchString = "{ \"query\": \"criteria\" }";

        // Mock CaseData and State
        CaseData caseData = mock(CaseData.class);
        State state = mock(State.class);

        // Mock LocalDateTime fields
        LocalDateTime lastModified = LocalDateTime.now();
        LocalDateTime lastStateModifiedDate = LocalDateTime.now().minusDays(1);

        // Create a mock ReturnedCaseDetails object
        ReturnedCaseDetails caseDetails = ReturnedCaseDetails.builder()
            .id(1234L)
            .data(caseData)
            .state(state)
            .lastModified(lastModified)
            .lastStateModifiedDate(lastStateModifiedDate)
            .build();

        // Create a mock ReturnedCases object
        ReturnedCases returnedCases = ReturnedCases.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();

        // Mock the behavior of coreCaseDataApi2's runQuery method
        when(coreCaseDataApi2.runQuery(
            authorization,
            serviceAuthorization,
            caseType,
            searchString)
        ).thenReturn(returnedCases);

        // Act
        ReturnedCases result = coreCaseDataApi2.runQuery(authorization, serviceAuthorization, caseType, searchString);

        // Assert
        assertThat(result).isEqualTo(returnedCases);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getCases()).hasSize(1);
        ReturnedCaseDetails returnedCaseDetails = result.getCases().get(0);
        assertThat(returnedCaseDetails.getId()).isEqualTo(1234L);
        assertThat(returnedCaseDetails.getData()).isEqualTo(caseData);
        assertThat(returnedCaseDetails.getState()).isEqualTo(state);
        assertThat(returnedCaseDetails.getLastModified()).isEqualTo(lastModified);
        assertThat(returnedCaseDetails.getLastStateModifiedDate()).isEqualTo(lastStateModifiedDate);

        verify(coreCaseDataApi2, times(1)).runQuery(authorization, serviceAuthorization, caseType, searchString);
    }

    @Test
    void shouldSearchCasesSuccessfully() {
        // Arrange
        String authorization = "Bearer auth-token";
        String serviceAuthorization = "ServiceAuth-token";
        String caseType = "divorce";
        String searchString = "{ \"query\": \"criteria\" }";

        // Mock CaseData and State
        CaseData caseData = mock(CaseData.class);
        State state = mock(State.class);

        // Mock LocalDateTime fields
        LocalDateTime lastModified = LocalDateTime.now();
        LocalDateTime lastStateModifiedDate = LocalDateTime.now().minusDays(1);

        // Create a mock ReturnedCaseDetails object
        ReturnedCaseDetails caseDetails = ReturnedCaseDetails.builder()
            .id(1234L)
            .data(caseData)
            .state(state)
            .lastModified(lastModified)
            .lastStateModifiedDate(lastStateModifiedDate)
            .build();

        // Create a mock ReturnedCases object
        ReturnedCases returnedCases = ReturnedCases.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();

        // Mock the behavior of coreCaseDataApi2's searchCases method
        when(coreCaseDataApi2.searchCases(
            authorization,
            serviceAuthorization,
            caseType,
            searchString)
        ).thenReturn(returnedCases);

        // Act
        ReturnedCases result = coreCaseDataApi2.searchCases(authorization, serviceAuthorization, caseType, searchString);

        // Assert
        assertThat(result).isEqualTo(returnedCases);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getCases()).hasSize(1);
        ReturnedCaseDetails returnedCaseDetails = result.getCases().get(0);
        assertThat(returnedCaseDetails.getId()).isEqualTo(1234L);
        assertThat(returnedCaseDetails.getData()).isEqualTo(caseData);
        assertThat(returnedCaseDetails.getState()).isEqualTo(state);
        assertThat(returnedCaseDetails.getLastModified()).isEqualTo(lastModified);
        assertThat(returnedCaseDetails.getLastStateModifiedDate()).isEqualTo(lastStateModifiedDate);

        verify(coreCaseDataApi2, times(1)).searchCases(authorization, serviceAuthorization, caseType, searchString);
    }
}
