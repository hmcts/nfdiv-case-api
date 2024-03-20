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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;

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
    void apply_shouldRegenerateConditionalOrderPronouncedCoversheetForApplicant1AndSetFlag() {
        // Given
        final CaseData caseData = createCaseDataWithApplicant1OfflineAndExistingCoverSheetApp1();
        final CaseDetails<CaseData, State> caseDetails = createCaseDetails(caseData);

        // When
        final CaseDetails<CaseData, State> result = task.apply(caseDetails);

        // Then
        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            caseData,
            1L,
            caseData.getApplicant1(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1
        );
        assertEquals(YES, caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated());
        assertSame(caseDetails, result);
    }

    @Test
    void apply_shouldRegenerateConditionalOrderPronouncedCoversheetForApplicant1WhenDocumentRemoved() {
        // Given
        final CaseData caseData = createCaseDataWithApplicant1OfflineAndExistingCoverSheetApp1();
        final CaseDetails<CaseData, State> caseDetails = createCaseDetails(caseData);
        final CaseDetails<CaseData, State> result = task.apply(caseDetails);

        // Then
        assertSame(YES, caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated());
        verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            caseData,
            1L,
            caseData.getApplicant1(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1
        );
        assertSame(caseDetails, result);
    }

    // Helper method to create CaseData with applicant 1 offline and existing cover sheet App1
    private CaseData createCaseDataWithApplicant1OfflineAndExistingCoverSheetApp1() {
        return CaseData.builder()
            .applicant1(Applicant.builder().offline(YES).build())
            .applicant2(Applicant.builder().offline(NO).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                    ListValue.<DivorceDocument>builder()
                        .id("1")
                        .value(DivorceDocument.builder()
                            .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                            .build())
                        .build(),
                    ListValue.<DivorceDocument>builder()
                        .id("2")
                        .value(DivorceDocument.builder()
                            .documentType(APPLICATION)
                            .build()).build()
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

