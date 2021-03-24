package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.reform.divorce.ccd.Permissions.READ;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CITIZEN;

public class PatchCase implements CcdConfiguration {

    public static final String PATCH_CASE = "patch-case";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(PATCH_CASE)
            .forState(Draft)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .retries(120, 120)
            .grant(CREATE_READ_UPDATE, CITIZEN)
            .grant(READ,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN,
                CASEWORKER_DIVORCE_SOLICITOR,
                CASEWORKER_DIVORCE_SUPERUSER,
                CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields()
            .optional(CaseData::getDivorceOrDissolution)
            .optional(CaseData::getD8HelpWithFeesReferenceNumber)
            .optional(CaseData::getD8InferredPetitionerGender)
            .optional(CaseData::getD8InferredRespondentGender)
            .optional(CaseData::getD8MarriageDate)
            .optional(CaseData::getD8MarriageIsSameSexCouple)
            .optional(CaseData::getD8ScreenHasMarriageBroken)
            .optional(CaseData::getD8HelpWithFeesNeedHelp)
            .optional(CaseData::getD8ScreenHasMarriageCert)
            .optional(CaseData::getD8HelpWithFeesAppliedForFees)
            .optional(CaseData::getD8MarriedInUk)
            .optional(CaseData::getD8CertificateInEnglish)
            .optional(CaseData::getD8CertifiedTranslation)
            .optional(CaseData::getD8CountryName)
            .optional(CaseData::getD8MarriagePlaceOfMarriage)
            .optional(CaseData::getD8PetitionerFirstName)
            .optional(CaseData::getD8PetitionerLastName)
            .optional(CaseData::getD8PetitionerEmail)
            .optional(CaseData::getPetitionerAgreedToReceiveEmails)
            .optional(CaseData::getD8PetitionerPhoneNumber)
            .optional(CaseData::getJurisdictionPetitionerResidence)
            .optional(CaseData::getJurisdictionRespondentResidence)
            .optional(CaseData::getJurisdictionPetitionerDomicile)
            .optional(CaseData::getJurisdictionRespondentDomicile)
            .optional(CaseData::getJurisdictionPetHabituallyResLastTwelveMonths)
            .optional(CaseData::getJurisdictionPetHabituallyResLastSixMonths)
            .optional(CaseData::getJurisdictionBothLastHabituallyResident)
            .optional(CaseData::getJurisdictionResidualEligible)
            .optional(CaseData::getLanguagePreferenceWelsh)
            .optional(CaseData::getD8MarriagePetitionerName)
            .optional(CaseData::getLastNameChangedWhenMarried)
            .optional(CaseData::getD8MarriageRespondentName)
            .optional(CaseData::getJurisdictionConnections);
    }
}
