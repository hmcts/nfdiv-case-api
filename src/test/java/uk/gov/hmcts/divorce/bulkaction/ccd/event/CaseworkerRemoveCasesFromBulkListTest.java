package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseRemovalService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerRemoveCasesFromBulkList.CASEWORKER_REMOVE_CASES_BULK_LIST;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.CASEWORKER_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRemoveCasesFromBulkListTest {

    @Mock
    private CaseRemovalService caseRemovalService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CaseworkerRemoveCasesFromBulkList caseworkerRemoveCasesFromBulkList;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        caseworkerRemoveCasesFromBulkList.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REMOVE_CASES_BULK_LIST);
    }

    @Test
    void shouldPopulateCasesAcceptedToListForHearingInAboutToStart() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        final ListValue<BulkListCaseDetails> listValue =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(
                        CaseLink.builder()
                            .caseReference("12345")
                            .build()
                    ).build())
                .build();

        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(singletonList(listValue));
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            caseworkerRemoveCasesFromBulkList.aboutToStart(details);

        assertThat(result.getData().getCasesAcceptedToListForHearing()).isNotNull();
        assertThat(result.getData().getCasesAcceptedToListForHearing().size(), is(1));
        assertThat(result.getData().getCasesAcceptedToListForHearing().get(0).getValue().getCaseReference(), is("12345"));
    }

    @Test
    void shouldReturnEmptyListInAboutToStartIfBulkListCaseDetailIsNull() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            caseworkerRemoveCasesFromBulkList.aboutToStart(details);

        assertThat(result.getData().getCasesAcceptedToListForHearing()).isNotNull();
        assertThat(result.getData().getCasesAcceptedToListForHearing().size(), is(0));
    }

    @Test
    void shouldReturnEmptyListInAboutToStartIfBulkListCaseDetailIsEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(emptyList());
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            caseworkerRemoveCasesFromBulkList.aboutToStart(details);

        assertThat(result.getData().getCasesAcceptedToListForHearing()).isNotNull();
        assertThat(result.getData().getCasesAcceptedToListForHearing().size(), is(0));
    }

    @Test
    void shouldReturnErrorIfCaseIsAddedToCasesAcceptedToListForHearingList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("98765")
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink1)
                    .build())
                .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();
        final ListValue<CaseLink> caseLinkListValue2 =
            ListValue.<CaseLink>builder()
                .value(caseLink2)
                .build();

        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(singletonList(bulkListCaseDetailsListValue));
        details.getData().setCasesAcceptedToListForHearing(List.of(caseLinkListValue1, caseLinkListValue2));
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            caseworkerRemoveCasesFromBulkList.midEvent(details, details);

        assertThat(result.getErrors()).isNotNull();
        assertThat(result.getErrors().size(), is(1));
    }

    @Test
    void shouldReturnErrorIfDuplicateCaseIsAddedToCasesAcceptedToListForHearingList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink3 = CaseLink.builder()
            .caseReference("98765")
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink1)
                    .build())
                .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink3)
                    .build())
                .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();
        final ListValue<CaseLink> caseLinkListValue2 =
            ListValue.<CaseLink>builder()
                .value(caseLink2)
                .build();

        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2));
        details.getData().setCasesAcceptedToListForHearing(List.of(caseLinkListValue1, caseLinkListValue2));
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            caseworkerRemoveCasesFromBulkList.midEvent(details, details);

        assertThat(result.getErrors()).isNotNull();
        assertThat(result.getErrors().size(), is(1));
    }

    @Test
    void shouldNotReturnErrorIfNoCasesAreAddedToCasesAcceptedToListForHearingList() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        final CaseLink caseLink = CaseLink.builder()
            .caseReference("12345")
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink)
                    .build())
                .build();
        final ListValue<CaseLink> caseLinkListValue =
            ListValue.<CaseLink>builder()
                .value(caseLink)
                .build();

        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(singletonList(bulkListCaseDetailsListValue));
        details.getData().setCasesAcceptedToListForHearing(singletonList(caseLinkListValue));
        details.setId(1L);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            caseworkerRemoveCasesFromBulkList.midEvent(details, details);

        assertThat(result.getErrors()).isNull();
    }

    @Test
    void shouldUpdateBulkCaseAfterBulkTriggerForSubmittedCallback() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("98765")
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink1)
                    .build())
                .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink2)
                    .build())
                .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();

        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2));
        details.getData().setCasesAcceptedToListForHearing(singletonList(caseLinkListValue1));
        details.setId(1L);

        final List<String> casesToRemove = List.of("98765");

        when(request.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        when(caseRemovalService.removeCases(details, casesToRemove, CASEWORKER_AUTH_TOKEN)).thenReturn(emptyList());

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmitCallbackResponse =
            caseworkerRemoveCasesFromBulkList.aboutToSubmit(details, details);

        assertThat(aboutToSubmitCallbackResponse.getWarnings()).isNull();
        verify(caseRemovalService).removeCases(details, casesToRemove, CASEWORKER_AUTH_TOKEN);
    }

    @Test
    void shouldReturnWarningMessagesIfAnyCasesCannotBeRemoved() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        final CaseLink caseLink1 = CaseLink.builder()
            .caseReference("12345")
            .build();
        final CaseLink caseLink2 = CaseLink.builder()
            .caseReference("98765")
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue1 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink1)
                    .build())
                .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue2 =
            ListValue.<BulkListCaseDetails>builder()
                .value(BulkListCaseDetails.builder()
                    .caseReference(caseLink2)
                    .build())
                .build();
        final ListValue<CaseLink> caseLinkListValue1 =
            ListValue.<CaseLink>builder()
                .value(caseLink1)
                .build();

        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2));
        details.getData().setCasesAcceptedToListForHearing(singletonList(caseLinkListValue1));
        details.setId(1L);

        final List<String> casesToRemove = List.of("98765");

        when(request.getHeader(AUTHORIZATION)).thenReturn(CASEWORKER_AUTH_TOKEN);
        when(caseRemovalService.removeCases(details, casesToRemove, CASEWORKER_AUTH_TOKEN)).thenReturn(casesToRemove);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> aboutToSubmitCallbackResponse =
            caseworkerRemoveCasesFromBulkList.aboutToSubmit(details, details);

        assertThat(aboutToSubmitCallbackResponse.getWarnings()).isNotNull();
        assertThat(aboutToSubmitCallbackResponse.getWarnings()).hasSize(1);
        assertThat(aboutToSubmitCallbackResponse.getWarnings()).contains("Case could not be removed from Bulk case: 98765");
        verify(caseRemovalService).removeCases(details, casesToRemove, CASEWORKER_AUTH_TOKEN);
    }
}
