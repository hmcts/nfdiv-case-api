package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.interimapplications.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.YesOrNoOrNotKnown;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.divorce.document.content.BailiffServiceApplicationTemplateContent.CONFIDENTIAL_ADDRESS_PLACEHOLDER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_APPLICANT_1_ADDRESS_LINE_1;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;

@ExtendWith(MockitoExtension.class)
class BailiffServiceApplicationTemplateContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private BailiffServiceApplicationTemplateContent templateContent;

    private static final String TEST_PHONE = "07700111222";
    private static final String TEST_VEHICLE_MODEL = "Ford Focus";
    private static final String TEST_VEHICLE_COLOUR = "Blue";
    private static final String TEST_VEHICLE_REG = "AB12 CDE";
    private static final String TEST_VEHICLE_OTHER = "Scratched on side";
    private static final String TEST_VIOLENCE_DETAILS = "Violent behavior in the past";
    private static final String TEST_THREATS_DETAILS = "Threatened family";
    private static final String TEST_POLICE_DETAILS = "Reported in 2020";
    private static final String TEST_SOCIAL_DETAILS = "Child services involved";
    private static final String TEST_ANIMAL_DETAILS = "Dog is aggressive";
    private static final String TEST_MENTAL_DETAILS = "History of depression";
    private static final String TEST_FIREARMS_DETAILS = "Holds license since 2018";
    private static final int TEST_PARTNER_AGE = 35;
    private static final String TEST_PARTNER_HEIGHT = "180cm";
    private static final String TEST_PARTNER_HAIR = "Black";
    private static final String TEST_PARTNER_EYES = "Brown";
    private static final String TEST_PARTNER_ETHNICITY = "Asian";
    private static final String TEST_PARTNER_FEATURES = "Tattoo on arm";
    private static final String TEST_BEST_TIME_TO_SERVE = "Evening";

    @Test
    void shouldReturnTemplateContent() {
        CaseData caseData = buildTestData();
        Applicant applicant = caseData.getApplicant1();
        applicant.setLanguagePreferenceWelsh(YesOrNo.NO);

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(LanguagePreference.ENGLISH))
            .thenReturn(new HashMap<>());

        Map<String, Object> result = templateContent.getTemplateContent(caseData, TEST_CASE_ID, applicant);

        Map<String, Object> expectedEntries = Map.ofEntries(
            entry("ccdCaseReference", formatId(TEST_CASE_ID)),
            entry("applicant1FullName", applicant.getFullName()),
            entry("applicant2FullName", caseData.getApplicant2().getFullName()),
            entry("divorceOrDissolution", "divorce application"),
            entry("serviceApplicationReceivedDate", "1 January 2023"),
            entry("evidenceUploaded", YesOrNo.YES),
            entry("recipientAddress", CONFIDENTIAL_ADDRESS_PLACEHOLDER),
            entry("bailiffKnowPartnersPhone", true),
            entry("bailiffPartnersPhone", TEST_PHONE),
            entry("bailiffKnowPartnersDateOfBirth", true),
            entry("bailiffPartnersDateOfBirth", "1 January 1990"),
            entry("bailiffPartnersApproximateAge", TEST_PARTNER_AGE),
            entry("bailiffPartnersHeight", TEST_PARTNER_HEIGHT),
            entry("bailiffPartnersHairColour", TEST_PARTNER_HAIR),
            entry("bailiffPartnersEyeColour", TEST_PARTNER_EYES),
            entry("bailiffPartnersEthnicGroup", TEST_PARTNER_ETHNICITY),
            entry("bailiffPartnersDistinguishingFeatures", TEST_PARTNER_FEATURES),
            entry("bailiffBestTimeToServe", TEST_BEST_TIME_TO_SERVE),
            entry("bailiffPartnerInARefuge", YesOrNoOrNotKnown.NO),
            entry("bailiffDoesPartnerHaveVehicle", YesOrNoOrNotKnown.YES),
            entry("bailiffPartnerVehicleModel", TEST_VEHICLE_MODEL),
            entry("bailiffPartnerVehicleColour", TEST_VEHICLE_COLOUR),
            entry("bailiffPartnerVehicleRegistration", TEST_VEHICLE_REG),
            entry("bailiffPartnerVehicleOtherDetails", TEST_VEHICLE_OTHER),
            entry("bailiffHasPartnerBeenViolent", YesOrNoOrNotKnown.YES),
            entry("bailiffPartnerViolenceDetails", TEST_VIOLENCE_DETAILS),
            entry("bailiffHasPartnerMadeThreats", YesOrNoOrNotKnown.YES),
            entry("bailiffPartnerThreatsDetails", TEST_THREATS_DETAILS),
            entry("bailiffHavePoliceBeenInvolved", YesOrNoOrNotKnown.YES),
            entry("bailiffPoliceInvolvedDetails", TEST_POLICE_DETAILS),
            entry("bailiffHaveSocialServicesBeenInvolved", YesOrNoOrNotKnown.YES),
            entry("bailiffSocialServicesInvolvedDetails", TEST_SOCIAL_DETAILS),
            entry("bailiffAreThereDangerousAnimals", YesOrNoOrNotKnown.YES),
            entry("bailiffDangerousAnimalsDetails", TEST_ANIMAL_DETAILS),
            entry("bailiffDoesPartnerHaveMentalIssues", YesOrNoOrNotKnown.YES),
            entry("bailiffPartnerMentalIssuesDetails", TEST_MENTAL_DETAILS),
            entry("bailiffDoesPartnerHoldFirearmsLicense", YesOrNoOrNotKnown.YES),
            entry("bailiffPartnerFirearmsLicenseDetails", TEST_FIREARMS_DETAILS)
        );

        assertThat(result).containsExactlyInAnyOrderEntriesOf(expectedEntries);
    }

    private CaseData buildTestData() {
        Applicant applicant1 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .languagePreferenceWelsh(YesOrNo.YES)
            .build();

        applicant1.setInterimApplicationOptions(
            InterimApplicationOptions.builder()
                .interimAppsCanUploadEvidence(YesOrNo.YES)
                .interimAppsCannotUploadDocs(YesOrNo.NO)
                .bailiffServiceJourneyOptions(
                    BailiffServiceJourneyOptions.builder()
                        .bailiffKnowPartnersPhone(YesOrNo.YES)
                        .bailiffPartnersPhone(TEST_PHONE)
                        .bailiffKnowPartnersDateOfBirth(YesOrNo.YES)
                        .bailiffPartnersDateOfBirth(LocalDate.of(1990, 1, 1))
                        .bailiffPartnersApproximateAge(35)
                        .bailiffPartnersHeight(TEST_PARTNER_HEIGHT)
                        .bailiffPartnersHairColour(TEST_PARTNER_HAIR)
                        .bailiffPartnersEyeColour(TEST_PARTNER_EYES)
                        .bailiffPartnersEthnicGroup(TEST_PARTNER_ETHNICITY)
                        .bailiffPartnersDistinguishingFeatures(TEST_PARTNER_FEATURES)
                        .bailiffBestTimeToServe(TEST_BEST_TIME_TO_SERVE)
                        .bailiffPartnerInARefuge(YesOrNoOrNotKnown.NO)
                        .bailiffDoesPartnerHaveVehicle(YesOrNoOrNotKnown.YES)
                        .bailiffPartnerVehicleModel(TEST_VEHICLE_MODEL)
                        .bailiffPartnerVehicleColour(TEST_VEHICLE_COLOUR)
                        .bailiffPartnerVehicleRegistration(TEST_VEHICLE_REG)
                        .bailiffPartnerVehicleOtherDetails(TEST_VEHICLE_OTHER)
                        .bailiffHasPartnerBeenViolent(YesOrNoOrNotKnown.YES)
                        .bailiffPartnerViolenceDetails(TEST_VIOLENCE_DETAILS)
                        .bailiffHasPartnerMadeThreats(YesOrNoOrNotKnown.YES)
                        .bailiffPartnerThreatsDetails(TEST_THREATS_DETAILS)
                        .bailiffHavePoliceBeenInvolved(YesOrNoOrNotKnown.YES)
                        .bailiffPoliceInvolvedDetails(TEST_POLICE_DETAILS)
                        .bailiffHaveSocialServicesBeenInvolved(YesOrNoOrNotKnown.YES)
                        .bailiffSocialServicesInvolvedDetails(TEST_SOCIAL_DETAILS)
                        .bailiffAreThereDangerousAnimals(YesOrNoOrNotKnown.YES)
                        .bailiffDangerousAnimalsDetails(TEST_ANIMAL_DETAILS)
                        .bailiffDoesPartnerHaveMentalIssues(YesOrNoOrNotKnown.YES)
                        .bailiffPartnerMentalIssuesDetails(TEST_MENTAL_DETAILS)
                        .bailiffDoesPartnerHoldFirearmsLicense(YesOrNoOrNotKnown.YES)
                        .bailiffPartnerFirearmsLicenseDetails(TEST_FIREARMS_DETAILS)
                        .build()
                )
                .build()
        );

        Applicant applicant2 = Applicant.builder()
            .firstName(TEST_FIRST_NAME)
            .lastName(TEST_LAST_NAME)
            .contactDetailsType(ContactDetailsType.PRIVATE)
            .address(AddressGlobalUK.builder()
                .addressLine1(TEST_APPLICANT_1_ADDRESS_LINE_1)
                .build())
            .build();

        return CaseData.builder()
            .applicant1(applicant1)
            .applicant2(applicant2)
            .alternativeService(
                AlternativeService.builder()
                    .receivedServiceApplicationDate(LocalDate.of(
                        2023, 1, 1
                    )).build()
            )
            .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
            .build();
    }
}
