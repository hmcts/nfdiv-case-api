package uk.gov.hmcts.divorce.document.content;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.NoFaultDivorce;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Court;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BULK_LIST;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PRONOUNCEMENT_JUDGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBulkListCaseDetailsListValue;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getConditionalOrder;
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

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    public void shouldUpdateTemplateContentMapWithExpectedData() {

        final LocalDateTime dateAndTimeOfHearing = LocalDateTime.of(2021, 11, 10, 12, 45, 0);
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(dateAndTimeOfHearing)
            .courtName(Court.SERVICE_CENTRE)
            .pronouncementJudge("District Judge")
            .bulkListCaseDetails(List.of(getBulkListCaseDetailsListValue(TEST_CASE_ID.toString())))
            .build();

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .id(TEST_CASE_ID)
                .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
                .data(Map.of("bulkListCaseDetails", bulkActionCaseData))
                .build();

        final List<CaseDetails> caseDetailsList = mockCaseDetailsList();

        when(pronouncementListTemplateContentService.retrieveBulkListCases(bulkActionCaseDetails.getId()))
            .thenReturn(caseDetailsList);

        Map<String, Object> templateContent = pronouncementListTemplateContentService
            .apply(bulkActionCaseData,
                bulkActionCaseDetails.getId(),
                LOCAL_DATE);

        assertThat(templateContent).contains(
            entry(PRONOUNCEMENT_JUDGE, "District Judge"),
            entry(COURT_NAME, Court.SERVICE_CENTRE),
            entry(DATE_OF_HEARING, "10 November 2021"),
            entry(TIME_OF_HEARING, "12:45 pm"));

        assertThat(templateContent).containsKey(BULK_LIST);

    }

    private List<CaseDetails> mockCaseDetailsList() {

        final CaseData caseData = CaseData.builder()
            .applicant1(getApplicant())
            .applicant2(respondent())
            .conditionalOrder(getConditionalOrder())
            .build();

        final CaseDetails mainCaseDetails =
            CaseDetails
                .builder()
                .caseTypeId(NoFaultDivorce.CASE_TYPE)
                .data(Map.of("",caseData))
                .build();

        when(objectMapper.convertValue(mainCaseDetails.getData(), CaseData.class)).thenReturn(caseData);

        return List.of(mainCaseDetails);
    }
}
