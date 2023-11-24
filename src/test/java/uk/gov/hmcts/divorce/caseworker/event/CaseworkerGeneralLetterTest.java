package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.caseworker.service.print.GeneralLetterDocumentPack;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.LetterPrinter;
import uk.gov.hmcts.divorce.testutil.ConfigTestUtil;

import java.time.Clock;
import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerGeneralLetter.CASEWORKER_CREATE_GENERAL_LETTER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.OTHER;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.RESPONDENT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerGeneralLetterTest {

    @Mock
    private GeneralLetterDocumentPack generalLetterDocumentPack;

    @Mock
    private LetterPrinter letterPrinter;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @Mock
    private Clock clock;

    @InjectMocks
    private CaseworkerGeneralLetter generalLetter;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = ConfigTestUtil.createCaseDataConfigBuilder();

        generalLetter.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getId)
                .containsExactly(CASEWORKER_CREATE_GENERAL_LETTER);
    }

    @Test
    void shouldNotReturnErrorsIfDocumentLinkProvidedWitbGeneralLetterAttachments() {
        ListValue<DivorceDocument> generalLetterAttachment = new ListValue<>(
                "1",
                DivorceDocument
                        .builder()
                        .documentLink(
                                Document.builder().build()
                        )
                        .build()
        );
        final CaseData caseData = caseData();
        caseData.setGeneralLetter(
                GeneralLetter
                        .builder()
                        .generalLetterParties(APPLICANT)
                        .generalLetterDetails("some details")
                        .generalLetterAttachments(singletonList(generalLetterAttachment))
                        .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalLetter.midEvent(details, details);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldReturnErrorIfDocumentLinkNotProvidedGeneralLetterAttachments() {
        ListValue<DivorceDocument> generalLetterAttachment = new ListValue<>(
                "1",
                DivorceDocument
                        .builder()
                        .build()
        );
        final CaseData caseData = caseData();
        Applicant applicant = Applicant
                .builder()
                .firstName("firstName")
                .middleName("middleName")
                .lastName("lastName")
                .build();

        caseData.setGeneralLetter(
                GeneralLetter
                        .builder()
                        .generalLetterParties(OTHER)
                        .generalLetterDetails("some details")
                        .otherRecipientAddress(AddressGlobalUK.builder().country("UK").build())
                        .otherRecipientName("firstName middleName lastName")
                        .generalLetterAttachments(singletonList(generalLetterAttachment))
                        .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalLetter.midEvent(details, details);

        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please ensure all General Letter attachments have been uploaded before continuing");
    }

    @Test
    void shouldGenerateGeneralLetter() {

        final CaseData caseData = caseData();
        caseData.setGeneralLetter(
                GeneralLetter
                        .builder()
                        .generalLetterParties(RESPONDENT)
                        .generalLetterDetails("some details")
                        .build()
        );

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalLetter.aboutToSubmit(details, details);
        verify(generalLetterDocumentPack).getDocumentPack(caseData, caseData.getApplicant2());
        assertThat(response.getData().getGeneralLetter()).isNull();
    }

    @Test
    void shouldSendGeneratedGeneralLetter() {


        final CaseData caseData = caseData();
        caseData.setGeneralLetter(
                GeneralLetter
                        .builder()
                        .generalLetterParties(OTHER)
                        .generalLetterDetails("some details")
                        .build()
        );

        var documentPackInfo = generalLetterDocumentPack.getDocumentPack(caseData, null);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse response = generalLetter.aboutToSubmit(details, details);
        verify(letterPrinter).sendLetters(caseData, details.getId(), null,
                documentPackInfo, generalLetterDocumentPack.getLetterId());
    }

    @Test
    void shouldNotReturnErrorsIfDocumentLinkProvidedWitHGeneralLetterAttachmentsConfidential() {

        setMockClock(clock);

        ListValue<DivorceDocument> generalLetterAttachment = new ListValue<>(
                "1",
                DivorceDocument
                        .builder()
                        .documentLink(
                                Document.builder().build()
                        ).documentType(DocumentType.GENERAL_LETTER)
                        .build()
        );

        ListValue<ConfidentialDivorceDocument> confidentialLetterAttachment = new ListValue<>(
                "1",
                ConfidentialDivorceDocument
                        .builder()
                        .documentLink(
                                Document.builder().build()
                        ).confidentialDocumentsReceived(ConfidentialDocumentsReceived.GENERAL_LETTER)
                        .build()
        );

        final CaseData caseData = caseData();
        caseData.setGeneralLetter(
                GeneralLetter
                        .builder()
                        .generalLetterParties(APPLICANT)
                        .generalLetterDetails("some details")
                        .otherRecipientAddress(AddressGlobalUK.builder().country("UK").build())
                        .otherRecipientName("firstName middleName lastName")
                        .generalLetterAttachments(singletonList(generalLetterAttachment))
                        .build()
        );

        caseData.getDocuments().setConfidentialDocumentsGenerated(new ArrayList<>());
        caseData.getDocuments().getConfidentialDocumentsGenerated().add(confidentialLetterAttachment);

        caseData.getApplicant1().setContactDetailsType(ContactDetailsType.PRIVATE);
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = generalLetter.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isNull();
        assertThat(caseData.getGeneralLetters()).isNotEmpty();
    }
}