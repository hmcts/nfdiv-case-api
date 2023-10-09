package uk.gov.hmcts.divorce.bulkaction.ccd.event;

import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.util.Lists;
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
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.PronouncementListDocService;
import uk.gov.hmcts.divorce.bulkaction.service.ScheduleCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerEditBulkCase.CASEWORKER_EDIT_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createBulkActionConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseworkerEditBulkCaseTest {

    @Mock
    private ScheduleCaseService scheduleCaseService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private PronouncementListDocService pronouncementListDocService;

    @InjectMocks
    private CaseworkerEditBulkCase caseworkerEditBulkCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<BulkActionCaseData, BulkActionState, UserRole> configBuilder = createBulkActionConfigBuilder();

        caseworkerEditBulkCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_BULK_CASE);
    }

    @Test
    void shouldSuccessfullyUpdateCasesInBulkWithCourtHearingDetails() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData.builder().build());
        details.setId(TEST_CASE_ID);

        doNothing().when(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);

        SubmittedCallbackResponse submittedCallbackResponse = caseworkerEditBulkCase.submitted(details, details);

        assertThat(submittedCallbackResponse).isNotNull();
        verify(scheduleCaseService).updateCourtHearingDetailsForCasesInBulk(details);
    }

    @Test
    void shouldNotPopulateErrorMessageWhenHearingDateIsInFutureAndAboutToSubmitIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.now().plusDays(5))
            .build()
        );
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = caseworkerEditBulkCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();

        verifyNoInteractions(pronouncementListDocService);
    }

    @Test
    void shouldPopulateErrorMessageWhenHearingDateIsInPastAndAboutToSubmitIsTriggered() {
        final CaseDetails<BulkActionCaseData, BulkActionState> details = new CaseDetails<>();
        details.setData(BulkActionCaseData
            .builder()
            .dateAndTimeOfHearing(LocalDateTime.now().minusHours(5))
            .build()
        );
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response = caseworkerEditBulkCase.aboutToSubmit(details, details);

        assertThat(response.getErrors()).containsExactly("Please enter a hearing date and time in the future");

        verifyNoInteractions(pronouncementListDocService);
    }

    @Test
    void shouldRegeneratePronouncementListDocumentWhenPresent() {
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
        final List<ListValue<BulkListCaseDetails>> bulkCaseList = Lists.newArrayList(
            bulkListCaseDetailsListValue1, bulkListCaseDetailsListValue2);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setData(BulkActionCaseData.builder()
                .dateAndTimeOfHearing(LocalDateTime.now().plusDays(5))
                .pronouncementListDocument(DivorceDocument.builder()
                    .documentType(DocumentType.PRONOUNCEMENT_LIST)
                    .documentLink(new Document())
                    .build())
                .bulkListCaseDetails(bulkCaseList)
            .build());
        bulkCaseDetails.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<BulkActionCaseData, BulkActionState> response
            = caseworkerEditBulkCase.aboutToSubmit(bulkCaseDetails, bulkCaseDetails);

        assertThat(response.getErrors()).isNull();

        verify(pronouncementListDocService).generateDocument(bulkCaseDetails);
    }
}
