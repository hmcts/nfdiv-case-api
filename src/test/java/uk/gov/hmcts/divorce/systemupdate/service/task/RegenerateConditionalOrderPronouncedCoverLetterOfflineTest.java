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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
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
    void apply_shouldRegenerateConditionalOrderPronouncedCoversheetForApplicant1AndSetFlag() {

        final CaseData caseData = CaseData.builder()
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

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        // When
        final CaseDetails<CaseData, State> result = task.apply(caseDetails);


       verify(coverLetterHelper).generateConditionalOrderPronouncedCoversheet(
            caseData,
            1L,
            caseData.getApplicant1(),
            CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1
        );
        assertEquals(YES, caseData.getApplicant1().getCoPronouncedCoverLetterRegenerated());
        assertSame(caseDetails, result);
    }
}

