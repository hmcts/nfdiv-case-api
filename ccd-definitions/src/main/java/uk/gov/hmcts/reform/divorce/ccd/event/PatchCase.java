package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.types.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdBuilder;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.reform.divorce.ccd.model.CaseEvent.PATCH_CASE;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.Draft;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

public class PatchCase implements CcdBuilder {

    @Override
    public void buildWith(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder
            .event(PATCH_CASE.name)
            .forState(Draft)
            .name("Patch case")
            .description("Patch a divorce or dissolution")
            .displayOrder(2)
            .retries(120, 120)
            .grant("CRU", UserRole.CITIZEN)
            .grant("R",
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
            .optional(CaseData::getD8CertifiedTranslation);
    }
}
