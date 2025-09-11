package uk.gov.hmcts.divorce.document.content;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.BailiffServiceJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.BAILIFF_SERVICE_APPLICATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_APPLICATION_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_DISSOLUTION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.END_CIVIL_PARTNERSHIP_CY;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SERVICE_APPLICATION_RECEIVED_DATE;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.notification.FormatUtil.getDateTimeFormatterForPreferredLanguage;

@Component
@RequiredArgsConstructor
@Slf4j
public class BailiffServiceApplicationTemplateContent implements TemplateContent {

    private final DocmosisCommonContent docmosisCommonContent;

    public static final String BAILIFF_KNOW_PARTNERS_PHONE = "bailiffKnowPartnersPhone";
    public static final String BAILIFF_PARTNERS_PHONE = "bailiffPartnersPhone";
    public static final String BAILIFF_KNOW_PARTNERS_DATE_OF_BIRTH = "bailiffKnowPartnersDateOfBirth";
    public static final String BAILIFF_PARTNERS_DATE_OF_BIRTH = "bailiffPartnersDateOfBirth";
    public static final String BAILIFF_PARTNERS_APPROXIMATE_AGE = "bailiffPartnersApproximateAge";
    public static final String BAILIFF_PARTNERS_HEIGHT = "bailiffPartnersHeight";
    public static final String BAILIFF_PARTNERS_HAIR_COLOUR = "bailiffPartnersHairColour";
    public static final String BAILIFF_PARTNERS_EYE_COLOUR = "bailiffPartnersEyeColour";
    public static final String BAILIFF_PARTNERS_ETHNIC_GROUP = "bailiffPartnersEthnicGroup";
    public static final String BAILIFF_PARTNERS_DISTINGUISHING_FEATURES = "bailiffPartnersDistinguishingFeatures";
    public static final String EVIDENCE_UPLOADED = "evidenceUploaded";
    public static final String BAILIFF_BEST_TIME_TO_SERVE = "bailiffBestTimeToServe";
    public static final String BAILIFF_PARTNER_IN_A_REFUGE = "bailiffPartnerInARefuge";
    public static final String BAILIFF_DOES_PARTNER_HAVE_VEHICLE = "bailiffDoesPartnerHaveVehicle";
    public static final String BAILIFF_PARTNER_VEHICLE_MODEL = "bailiffPartnerVehicleModel";
    public static final String BAILIFF_PARTNER_VEHICLE_COLOUR = "bailiffPartnerVehicleColour";
    public static final String BAILIFF_PARTNER_VEHICLE_REGISTRATION = "bailiffPartnerVehicleRegistration";
    public static final String BAILIFF_PARTNER_VEHICLE_OTHER_DETAILS = "bailiffPartnerVehicleOtherDetails";
    public static final String BAILIFF_HAS_PARTNER_BEEN_VIOLENT = "bailiffHasPartnerBeenViolent";
    public static final String BAILIFF_PARTNER_VIOLENCE_DETAILS = "bailiffPartnerViolenceDetails";
    public static final String BAILIFF_HAS_PARTNER_MADE_THREATS = "bailiffHasPartnerMadeThreats";
    public static final String BAILIFF_PARTNER_THREATS_DETAILS = "bailiffPartnerThreatsDetails";
    public static final String BAILIFF_HAVE_POLICE_BEEN_INVOLVED = "bailiffHavePoliceBeenInvolved";
    public static final String BAILIFF_POLICE_INVOLVED_DETAILS = "bailiffPoliceInvolvedDetails";
    public static final String BAILIFF_HAVE_SOCIAL_SERVICES_BEEN_INVOLVED = "bailiffHaveSocialServicesBeenInvolved";
    public static final String BAILIFF_SOCIAL_SERVICES_INVOLVED_DETAILS = "bailiffSocialServicesInvolvedDetails";
    public static final String BAILIFF_ARE_THERE_DANGEROUS_ANIMALS = "bailiffAreThereDangerousAnimals";
    public static final String BAILIFF_DANGEROUS_ANIMALS_DETAILS = "bailiffDangerousAnimalsDetails";
    public static final String BAILIFF_DOES_PARTNER_HAVE_MENTAL_ISSUES = "bailiffDoesPartnerHaveMentalIssues";
    public static final String BAILIFF_PARTNER_MENTAL_ISSUES_DETAILS = "bailiffPartnerMentalIssuesDetails";
    public static final String BAILIFF_DOES_PARTNER_HOLD_FIREARMS_LICENSE = "bailiffDoesPartnerHoldFirearmsLicense";
    public static final String BAILIFF_PARTNER_FIREARMS_LICENSE_DETAILS = "bailiffPartnerFirearmsLicenseDetails";

    public static final String CONFIDENTIAL_ADDRESS_EN = "Respondent’s address is confidential";
    public static final String CONFIDENTIAL_ADDRESS_CY = "Mae cyfeiriad yr Atebydd yn gyfrinachol";

    @Override
    public List<String> getSupportedTemplates() {
        return List.of(
            BAILIFF_SERVICE_APPLICATION_TEMPLATE_ID
        );
    }

    @Override
    public Map<String, Object> getTemplateContent(CaseData caseData, Long caseId, Applicant applicant) {
        Map<String, Object> templateContent = docmosisCommonContent
            .getBasicDocmosisTemplateContent(applicant.getLanguagePreference());

        AlternativeService alternativeService = caseData.getAlternativeService();
        InterimApplicationOptions applicationOptions = applicant.getInterimApplicationOptions();
        LanguagePreference languagePreference = applicant.getLanguagePreference();
        DateTimeFormatter dateTimeFormatter = getDateTimeFormatterForPreferredLanguage(languagePreference);
        Applicant applicant2 = caseData.getApplicant2();
        boolean partnerPhotoUploaded = YesOrNo.YES.equals(applicationOptions.getInterimAppsCanUploadEvidence())
            && !YesOrNo.YES.equals(applicationOptions.getInterimAppsCannotUploadDocs());


        templateContent.put(APPLICANT_1_FULL_NAME, applicant.getFullName());
        templateContent.put(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName());
        templateContent.put(CCD_CASE_REFERENCE, formatId(caseId));
        templateContent.put(
            SERVICE_APPLICATION_RECEIVED_DATE, dateTimeFormatter.format(alternativeService.getReceivedServiceApplicationDate())
        );
        templateContent.put(EVIDENCE_UPLOADED, YesOrNo.from(partnerPhotoUploaded));
        templateContent.put(
            RECIPIENT_ADDRESS,
            applicant2.isConfidentialContactDetails()
                ? getConfidentialAddressPlaceholder(languagePreference)
                : caseData.getApplicant2().getCorrespondenceAddress()
        );
        templateContent.put(DIVORCE_OR_DISSOLUTION, getApplicationType(languagePreference, caseData));

        BailiffServiceJourneyOptions applicationAnswers = applicant.getInterimApplicationOptions().getBailiffServiceJourneyOptions();
        return bailiffApplicationContent(templateContent, applicationAnswers, dateTimeFormatter);
    }

    private Map<String, Object> bailiffApplicationContent(
        Map<String, Object> templateContent,
        BailiffServiceJourneyOptions applicationAnswers,
        DateTimeFormatter dateTimeFormatter
    ) {
        templateContent.put(BAILIFF_KNOW_PARTNERS_PHONE, applicationAnswers.getBailiffKnowPartnersPhone().toBoolean());
        templateContent.put(BAILIFF_PARTNERS_PHONE, applicationAnswers.getBailiffPartnersPhone());
        templateContent.put(BAILIFF_KNOW_PARTNERS_DATE_OF_BIRTH, applicationAnswers.getBailiffKnowPartnersDateOfBirth().toBoolean());
        if (applicationAnswers.getBailiffPartnersDateOfBirth() != null) {
            templateContent.put(
                BAILIFF_PARTNERS_DATE_OF_BIRTH,
                dateTimeFormatter.format(applicationAnswers.getBailiffPartnersDateOfBirth())
            );
        }
        templateContent.put(BAILIFF_PARTNERS_APPROXIMATE_AGE, applicationAnswers.getBailiffPartnersApproximateAge());
        templateContent.put(BAILIFF_PARTNERS_HEIGHT, applicationAnswers.getBailiffPartnersHeight());
        templateContent.put(BAILIFF_PARTNERS_HAIR_COLOUR, applicationAnswers.getBailiffPartnersHairColour());
        templateContent.put(BAILIFF_PARTNERS_EYE_COLOUR, applicationAnswers.getBailiffPartnersEyeColour());
        templateContent.put(BAILIFF_PARTNERS_ETHNIC_GROUP, applicationAnswers.getBailiffPartnersEthnicGroup());
        templateContent.put(BAILIFF_PARTNERS_DISTINGUISHING_FEATURES, applicationAnswers.getBailiffPartnersDistinguishingFeatures());
        templateContent.put(BAILIFF_BEST_TIME_TO_SERVE, applicationAnswers.getBailiffBestTimeToServe());
        templateContent.put(
            BAILIFF_PARTNER_IN_A_REFUGE,
            applicationAnswers.getBailiffPartnerInARefuge().getValue()
        );
        templateContent.put(
            BAILIFF_DOES_PARTNER_HAVE_VEHICLE,
            applicationAnswers.getBailiffDoesPartnerHaveVehicle().getValue()
        );
        if (applicationAnswers.getBailiffDoesPartnerHaveVehicle().toBoolean()) {
            templateContent.put(BAILIFF_PARTNER_VEHICLE_MODEL, applicationAnswers.getBailiffPartnerVehicleModel());
            templateContent.put(BAILIFF_PARTNER_VEHICLE_COLOUR, applicationAnswers.getBailiffPartnerVehicleColour());
            templateContent.put(BAILIFF_PARTNER_VEHICLE_REGISTRATION, applicationAnswers.getBailiffPartnerVehicleRegistration());
            templateContent.put(BAILIFF_PARTNER_VEHICLE_OTHER_DETAILS, applicationAnswers.getBailiffPartnerVehicleOtherDetails());
        }
        templateContent.put(
            BAILIFF_HAS_PARTNER_BEEN_VIOLENT,
            applicationAnswers.getBailiffHasPartnerBeenViolent().getValue()
        );
        templateContent.put(BAILIFF_PARTNER_VIOLENCE_DETAILS, applicationAnswers.getBailiffPartnerViolenceDetails());
        templateContent.put(
            BAILIFF_HAS_PARTNER_MADE_THREATS,
            applicationAnswers.getBailiffHasPartnerMadeThreats().getValue()
        );
        templateContent.put(BAILIFF_PARTNER_THREATS_DETAILS, applicationAnswers.getBailiffPartnerThreatsDetails());
        templateContent.put(
            BAILIFF_HAVE_POLICE_BEEN_INVOLVED,
            applicationAnswers.getBailiffHavePoliceBeenInvolved().getValue()
        );
        templateContent.put(BAILIFF_POLICE_INVOLVED_DETAILS, applicationAnswers.getBailiffPoliceInvolvedDetails());
        templateContent.put(
            BAILIFF_HAVE_SOCIAL_SERVICES_BEEN_INVOLVED,
            applicationAnswers.getBailiffHaveSocialServicesBeenInvolved().getValue()
        );
        templateContent.put(BAILIFF_SOCIAL_SERVICES_INVOLVED_DETAILS, applicationAnswers.getBailiffSocialServicesInvolvedDetails());
        templateContent.put(
            BAILIFF_ARE_THERE_DANGEROUS_ANIMALS,
            applicationAnswers.getBailiffAreThereDangerousAnimals().getValue()
        );
        templateContent.put(BAILIFF_DANGEROUS_ANIMALS_DETAILS, applicationAnswers.getBailiffDangerousAnimalsDetails());
        templateContent.put(
            BAILIFF_DOES_PARTNER_HAVE_MENTAL_ISSUES,
            applicationAnswers.getBailiffDoesPartnerHaveMentalIssues().getValue()
        );
        templateContent.put(BAILIFF_PARTNER_MENTAL_ISSUES_DETAILS, applicationAnswers.getBailiffPartnerMentalIssuesDetails());
        templateContent.put(
            BAILIFF_DOES_PARTNER_HOLD_FIREARMS_LICENSE,
            applicationAnswers.getBailiffDoesPartnerHoldFirearmsLicense().getValue()
        );
        templateContent.put(BAILIFF_PARTNER_FIREARMS_LICENSE_DETAILS, applicationAnswers.getBailiffPartnerFirearmsLicenseDetails());

        return templateContent;
    }

    private String getApplicationType(LanguagePreference languagePreference, CaseData caseData) {
        return LanguagePreference.WELSH.equals(languagePreference)
            ? caseData.isDivorce() ? DIVORCE_APPLICATION_CY : END_CIVIL_PARTNERSHIP_CY
            : caseData.isDivorce() ? DIVORCE_APPLICATION : END_CIVIL_PARTNERSHIP;
    }

    private String getConfidentialAddressPlaceholder(LanguagePreference languagePreference) {
        return LanguagePreference.WELSH.equals(languagePreference)
            ? CONFIDENTIAL_ADDRESS_CY
            : CONFIDENTIAL_ADDRESS_EN;
    }
}
