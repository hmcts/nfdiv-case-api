package uk.gov.hmcts.divorce.citizen.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.document.DocumentRemovalService;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getListOfDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class InterimApplicationOptionsServiceTest {

    @Mock
    private DocumentRemovalService documentRemovalService;

    @InjectMocks
    private InterimApplicationOptionsService interimApplicationOptionsService;

    @Test
    void shouldClearAnyDocumentsUploadedAsD11PartnerAgreesEvidence() {
        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(2);

        GeneralApplicationD11JourneyOptions generalApplicationD11JourneyOptions = GeneralApplicationD11JourneyOptions.builder()
            .partnerAgreesDocs(docs)
            .type(GeneralApplicationType.AMEND_APPLICATION)
            .build();

        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(InterimApplicationOptions.builder()
                .interimAppsHaveHwfReference(YesOrNo.YES)
                .interimAppsHwfRefNumber("test number")
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .interimAppsCannotUploadDocs(YesOrNo.YES)
                .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
                .generalApplicationD11JourneyOptions(generalApplicationD11JourneyOptions)
                .build())
            .build();

        interimApplicationOptionsService.resetInterimApplicationOptions(applicant);

        verify(documentRemovalService).deleteDocument(docs);
        assertThat(applicant.getInterimApplicationOptions().getInterimAppsUseHelpWithFees()).isNull();
    }

    @Test
    void shouldClearAnyDocumentsUploadedAsEvidence() {
        List<ListValue<DivorceDocument>> docs = getListOfDivorceDocumentListValue(2);

        GeneralApplicationD11JourneyOptions generalApplicationD11JourneyOptions = GeneralApplicationD11JourneyOptions.builder()
            .type(GeneralApplicationType.AMEND_APPLICATION)
            .build();

        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(InterimApplicationOptions.builder()
                .interimAppsHaveHwfReference(YesOrNo.YES)
                .interimAppsHwfRefNumber("test number")
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .interimAppsCannotUploadDocs(YesOrNo.YES)
                .interimAppsEvidenceDocs(docs)
                .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
                .generalApplicationD11JourneyOptions(generalApplicationD11JourneyOptions)
                .build())
            .build();

        interimApplicationOptionsService.resetInterimApplicationOptions(applicant);

        verify(documentRemovalService).deleteDocument(docs);
    }

    @Test
    void shouldClearInterimApplicationOptions() {
        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(InterimApplicationOptions.builder()
                .interimAppsHaveHwfReference(YesOrNo.YES)
                .interimAppsHwfRefNumber("test number")
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .interimAppsCannotUploadDocs(YesOrNo.YES)
                .interimApplicationType(InterimApplicationType.DEEMED_SERVICE)
                .build())
            .build();

        interimApplicationOptionsService.resetInterimApplicationOptions(applicant);

        assertThat(applicant.getInterimApplicationOptions().getInterimAppsUseHelpWithFees()).isNull();
        assertThat(applicant.getInterimApplicationOptions().getInterimAppsHaveHwfReference()).isNull();
        assertThat(applicant.getInterimApplicationOptions().getInterimAppsHwfRefNumber()).isNull();
        assertThat(applicant.getInterimApplicationOptions().getInterimAppsCanUploadEvidence()).isNull();
        assertThat(applicant.getInterimApplicationOptions().getInterimAppsCannotUploadDocs()).isNull();
    }
}
