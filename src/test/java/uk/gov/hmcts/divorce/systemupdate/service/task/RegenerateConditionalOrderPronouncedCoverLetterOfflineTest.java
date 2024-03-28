package uk.gov.hmcts.divorce.systemupdate.service.task;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;

@ExtendWith(MockitoExtension.class)
class RegenerateConditionalOrderPronouncedCoverLetterOfflineTest {

    @Mock
    private ConditionalOrderPronouncedCoverLetterHelper coverLetterHelper;

    @InjectMocks
    private RegenerateConditionalOrderPronouncedCoverLetterOffline task;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void apply_shouldRegenerateConditionalOrderPronouncedCoversheetForApplicant2AndSetFlag() {
        // Given
        final CaseData caseData = createCaseDataWithApplicant2OfflineAndExistingCoverSheetApp2();
        final CaseDetails<CaseData, State> caseDetails = createCaseDetails(caseData);

        // When
        final CaseDetails<CaseData, State> result = task.apply(caseDetails);

        // Then
        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            caseData,
            1L,
            caseData.getApplicant2(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2
        );
        assertEquals(YES, caseData.getApplicant2().getCoPronouncedCoverLetterRegenerated());
        assertSame(caseDetails, result);
    }

    @Test
    void apply_shouldRegenerateConditionalOrderPronouncedCoversheetForApplicant2WhenRegeneratedAlready() {
        // Given
        final CaseData caseData = createCaseDataWithApplicant2OfflineAndExistingCoverSheetApp2();
        caseData.getApplicant2().setCoPronouncedCoverLetterRegenerated(YES);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setCoPronouncedCoverLetterRegenerated(YES);
        final CaseDetails<CaseData, State> caseDetails = createCaseDetails(caseData);

        // When
        final CaseDetails<CaseData, State> result = task.apply(caseDetails);

        // Then
        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            caseData,
            1L,
            caseData.getApplicant2(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2
        );
        assertSame(caseDetails, result);
    }

    // Helper method to create CaseData with applicant 2 offline and existing cover sheet App2
    private CaseData createCaseDataWithApplicant2OfflineAndExistingCoverSheetApp2() {
        return CaseData.builder()
            .applicationType(ApplicationType.JOINT_APPLICATION)
            .applicant1(Applicant.builder().offline(NO).build())
            .applicant2(Applicant.builder().offline(YES).contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(Lists.newArrayList(
                    ListValue.<ConfidentialDivorceDocument>builder()
                        .id("1")
                        .value(ConfidentialDivorceDocument.builder()
                            .confidentialDocumentsReceived(ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)
                            .build())
                        .build()
                ))
                .build())
            .build();
    }

    // Helper method to create CaseDetails with given CaseData and ID
    private CaseDetails<CaseData, State> createCaseDetails(CaseData caseData) {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);
        return caseDetails;
    }

}

