package uk.gov.hmcts.divorce.document;


import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class DocumentGenerationUtilTest {

    @Mock
    private DocumentGenerator documentGenerator;
    @InjectMocks
    private DocumentGenerationUtil documentGenerationUtil;

    @Test
    void shouldRemoveGeneratedDocuments() {

        final CaseData caseData = buildCaseDataWithDocuments();

        documentGenerationUtil.removeExistingDocuments(caseData,
                List.of(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1, CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2));

        assertEquals(1, caseData.getDocuments().getDocumentsGenerated().size());
        assertEquals(CERTIFICATE_OF_ENTITLEMENT, caseData.getDocuments().getDocumentsGenerated().get(0).getValue().getDocumentType());
    }

    @Test
    void shouldGenerateCertificateOfEntitlementDocument() {
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData()).id(TEST_CASE_ID).build();

        final CaseData caseData = buildCaseDataWithDocuments();

        caseDetails.setData(caseData);

        documentGenerationUtil.generateCertificateOfEntitlement(caseDetails);

        verify(documentGenerator).generateAndStoreCaseDocument(eq(CERTIFICATE_OF_ENTITLEMENT),
                eq(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID),
                eq(CERTIFICATE_OF_ENTITLEMENT_NAME),
                any(),
                anyLong());

        assertEquals(CERTIFICATE_OF_ENTITLEMENT, caseData.getConditionalOrder().getCertificateOfEntitlementDocument().getDocumentType());
    }

    private CaseData buildCaseDataWithDocuments() {
        final CaseData caseData = caseData();
        caseData.setDocuments(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(
                        ListValue.<DivorceDocument>builder()
                                .id("1")
                                .value(DivorceDocument.builder()
                                        .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1)
                                        .build())
                                .build(),
                        ListValue.<DivorceDocument>builder()
                                .id("2")
                                .value(DivorceDocument.builder()
                                        .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2)
                                        .build())
                                .build(),
                        ListValue.<DivorceDocument>builder()
                                .id("3")
                                .value(DivorceDocument.builder()
                                        .documentType(CERTIFICATE_OF_ENTITLEMENT)
                                        .build()).build()
                ))
                .build());
        return caseData;
    }
}