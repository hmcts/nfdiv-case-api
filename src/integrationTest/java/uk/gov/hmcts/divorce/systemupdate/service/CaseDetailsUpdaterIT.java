package uk.gov.hmcts.divorce.systemupdate.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_SERVICE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CaseDetailsUpdaterIT {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CaseDetailsUpdater caseDetailsUpdater;

    @Test
    void shouldConvertReformCaseDetailsAndApplyUpdateTask() {

        final Document certificateOfEntitlementDocumentLink = Document.builder()
            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3")
            .filename("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3/binary")
            .build();
        final Document certificateOfServiceDocumentLink = Document.builder()
            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-0132253456567")
            .filename("certificateOfService-0132253456567-2022-01-11:13:06.pdf")
            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-0132253456567/binary")
            .build();

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder()
                .dateAndTimeOfHearing(LocalDateTime.now())
                .certificateOfEntitlementDocument(DivorceDocument.builder()
                    .documentLink(certificateOfEntitlementDocumentLink)
                    .documentType(CERTIFICATE_OF_ENTITLEMENT)
                    .documentFileName("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
                    .build())
                .build())
            .alternativeService(AlternativeService.builder()
                .bailiff(Bailiff.builder()
                    .certificateOfServiceDocument(DivorceDocument.builder()
                        .documentLink(certificateOfServiceDocumentLink)
                        .documentType(CERTIFICATE_OF_SERVICE)
                        .documentFileName("certificateOfService-0132253456567-2022-01-11:13:06.pdf")
                        .build())
                    .build())
                .build())
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails reformCaseDetails = objectMapper.convertValue(caseDetails, new TypeReference<>() {
        });

        uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails =
            new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();

        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .dateAndTimeOfHearing(LocalDateTime.now())
            .build();

        bulkCaseDetails.setData(bulkActionCaseData);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(reformCaseDetails)
            .build();

        final var updatedCaseDetails = caseDetailsUpdater.updateCaseData(
            caseDetailsIn -> caseDetailsIn,
            startEventResponse);

        final DivorceDocument certificateOfEntitlementDocument =
            updatedCaseDetails.getData().getConditionalOrder().getCertificateOfEntitlementDocument();
        assertThat(certificateOfEntitlementDocument.getDocumentFileName())
            .isEqualTo("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf");
        assertThat(certificateOfEntitlementDocument.getDocumentType())
            .isEqualTo(CERTIFICATE_OF_ENTITLEMENT);
        assertThat(certificateOfEntitlementDocument.getDocumentLink())
            .isEqualTo(certificateOfEntitlementDocumentLink);

        final DivorceDocument certificateOfServiceDocument =
            updatedCaseDetails.getData().getAlternativeService().getBailiff().getCertificateOfServiceDocument();
        assertThat(certificateOfServiceDocument.getDocumentFileName())
            .isEqualTo("certificateOfService-0132253456567-2022-01-11:13:06.pdf");
        assertThat(certificateOfServiceDocument.getDocumentLink()).isEqualTo(certificateOfServiceDocumentLink);
    }
}
