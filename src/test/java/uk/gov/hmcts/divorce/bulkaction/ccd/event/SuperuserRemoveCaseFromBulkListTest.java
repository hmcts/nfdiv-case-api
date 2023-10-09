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
import uk.gov.hmcts.divorce.bulkaction.service.PronouncementListDocService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.SuperuserRemoveCaseFromBulkList.SUPERUSER_REMOVE_CASE_BULK_LIST;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SuperuserRemoveCaseFromBulkListTest {

    @Mock
    private PronouncementListDocService pronouncementListDocService;

    @InjectMocks
    private SuperuserRemoveCaseFromBulkList superuserRemoveCaseFromBulkList;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        superuserRemoveCaseFromBulkList.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUPERUSER_REMOVE_CASE_BULK_LIST);
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
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            superuserRemoveCaseFromBulkList.aboutToStart(details);

        assertThat(result.getData().getCasesAcceptedToListForHearing()).isNotNull();
        assertThat(result.getData().getCasesAcceptedToListForHearing()).hasSize(1);
        assertThat(result.getData().getCasesAcceptedToListForHearing().get(0).getValue().getCaseReference()).isEqualTo("12345");
    }

    @Test
    void shouldReturnEmptyListInAboutToStartIfBulkListCaseDetailIsNull() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            superuserRemoveCaseFromBulkList.aboutToStart(details);

        assertThat(result.getData().getCasesAcceptedToListForHearing()).isNotNull();
        assertThat(result.getData().getCasesAcceptedToListForHearing()).hasSize(0);
    }

    @Test
    void shouldReturnEmptyListInAboutToStartIfBulkListCaseDetailIsEmpty() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.getData().setBulkListCaseDetails(emptyList());
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            superuserRemoveCaseFromBulkList.aboutToStart(details);

        assertThat(result.getData().getCasesAcceptedToListForHearing()).isNotNull();
        assertThat(result.getData().getCasesAcceptedToListForHearing()).hasSize(0);
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
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            superuserRemoveCaseFromBulkList.midEvent(details, details);

        assertThat(result.getErrors()).isNotNull();
        assertThat(result.getErrors()).hasSize(1);
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
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            superuserRemoveCaseFromBulkList.midEvent(details, details);

        assertThat(result.getErrors()).isNotNull();
        assertThat(result.getErrors()).hasSize(1);
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
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> result =
            superuserRemoveCaseFromBulkList.midEvent(details, details);

        assertThat(result.getErrors()).isNull();
    }

    @Test
    void shouldUpdateBulkCaseAndRegeneratePronouncementListDocumentAfterBulkTriggerForAboutToSubmitCallback() {
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

        details.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2))
            .erroredCaseDetails(List.of(bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2))
            .casesAcceptedToListForHearing(singletonList(caseLinkListValue1))
            .pronouncementListDocument(DivorceDocument.builder()
                .documentType(DocumentType.PRONOUNCEMENT_LIST)
                .build())
            .build());
        details.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response =
            superuserRemoveCaseFromBulkList.aboutToSubmit(details, details);

        assertThat(response.getData().getBulkListCaseDetails()).hasSize(1);
        assertThat(response.getData().getBulkListCaseDetails()).contains(bulkListCaseDetailsListValue1);

        assertThat(response.getData().getErroredCaseDetails()).hasSize(1);
        assertThat(response.getData().getErroredCaseDetails()).contains(bulkListCaseDetailsListValue1);

        verify(pronouncementListDocService).generateDocument(details);
    }
}
