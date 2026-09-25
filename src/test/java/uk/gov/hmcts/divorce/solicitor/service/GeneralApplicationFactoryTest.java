package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.service.CitizenGeneralApplicationSubmissionService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplication;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationFactoryTest {

    @InjectMocks
    private GeneralApplicationFactory generalApplicationFactory;

    @Mock
    private Clock clock;

    @Mock
    private CitizenGeneralApplicationSubmissionService submissionService;

    @Test
    void shouldCreateGeneralApplicationForApplicant1WithMappedTypeAndSupportingDocuments() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 9, 24, 10, 15);
        when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        DynamicList solType = DynamicList.builder()
            .value(DynamicListElement.builder().label(GeneralApplicationType.EXPEDITE.getLabel()).build())
            .build();

        GeneralApplicationD11JourneyOptions d11 = GeneralApplicationD11JourneyOptions.builder()
            .solType(solType)
            .build();

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .generalApplicationD11JourneyOptions(d11)
            .build();

        var supportingDocuments = List.<ListValue<DivorceDocument>>of();
        when(submissionService.collectSupportingDocuments(options)).thenReturn(supportingDocuments);

        GeneralApplication result = generalApplicationFactory.createFromJourneyOptions(
            options,
            true,
            ApplicationType.SOLE_APPLICATION
        );

        assertThat(result.getGeneralApplicationParty()).isEqualTo(GeneralParties.APPLICANT);
        assertThat(result.getGeneralApplicationReceivedDate()).isEqualTo(fixedNow);
        assertThat(result.getGeneralApplicationType()).isEqualTo(GeneralApplicationType.EXPEDITE);
        assertThat(result.getGeneralApplicationSubmittedOnline()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldCreateGeneralApplicationForApplicant2OnJointApplication() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 9, 24, 11, 0);
        when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        DynamicList solType = DynamicList.builder()
            .value(DynamicListElement.builder().label(GeneralApplicationType.AMEND_APPLICATION.getLabel()).build())
            .build();

        GeneralApplicationD11JourneyOptions d11 = GeneralApplicationD11JourneyOptions.builder()
            .solType(solType)
            .build();

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .generalApplicationD11JourneyOptions(d11)
            .build();

        when(submissionService.collectSupportingDocuments(options)).thenReturn(List.of());

        GeneralApplication result = generalApplicationFactory.createFromJourneyOptions(
            options,
            false,
            ApplicationType.JOINT_APPLICATION
        );

        assertThat(result.getGeneralApplicationParty()).isEqualTo(GeneralParties.APPLICANT2);
        assertThat(result.getGeneralApplicationType()).isEqualTo(GeneralApplicationType.AMEND_APPLICATION);
        assertThat(result.getGeneralApplicationReceivedDate()).isEqualTo(fixedNow);
    }

    @Test
    void shouldCreateGeneralApplicationForRespondentOnSoleApplicationWhenNotApplicant1() {
        LocalDateTime fixedNow = LocalDateTime.of(2026, 9, 24, 12, 0);
        when(clock.instant()).thenReturn(fixedNow.atZone(ZoneId.systemDefault()).toInstant());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        DynamicList solType = DynamicList.builder()
            .value(DynamicListElement.builder().label(GeneralApplicationType.DELAY.getLabel()).build())
            .build();

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder().solType(solType).build())
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .build();

        when(submissionService.collectSupportingDocuments(options)).thenReturn(List.of());

        GeneralApplication result = generalApplicationFactory.createFromJourneyOptions(
            options,
            false,
            ApplicationType.SOLE_APPLICATION
        );

        assertThat(result.getGeneralApplicationParty()).isEqualTo(GeneralParties.RESPONDENT);
        assertThat(result.getGeneralApplicationType()).isEqualTo(GeneralApplicationType.DELAY);
        assertThat(result.getGeneralApplicationReceivedDate()).isEqualTo(fixedNow);
    }

    @Test
    void shouldSetNullGeneralApplicationTypeWhenSelectedLabelDoesNotMatchAnyEnum() {
        when(clock.instant()).thenReturn(Instant.now());
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        DynamicList solType = DynamicList.builder()
            .value(DynamicListElement.builder().label("Unknown type").build())
            .build();

        InterimApplicationOptions options = InterimApplicationOptions.builder()
            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder().solType(solType).build())
            .interimApplicationType(InterimApplicationType.DIGITISED_GENERAL_APPLICATION_D11)
            .build();

        when(submissionService.collectSupportingDocuments(options)).thenReturn(List.of());

        GeneralApplication result = generalApplicationFactory.createFromJourneyOptions(
            options,
            true,
            ApplicationType.SOLE_APPLICATION
        );

        assertThat(result.getGeneralApplicationType()).isNull();
    }
}
