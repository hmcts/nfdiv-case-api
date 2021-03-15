package uk.gov.hmcts.reform.divorce.ccd.event;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.reform.divorce.ccd.CcdConfiguration;
import uk.gov.hmcts.reform.divorce.ccd.model.CaseData;
import uk.gov.hmcts.reform.divorce.ccd.model.State;
import uk.gov.hmcts.reform.divorce.ccd.model.UserRole;

import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.reform.divorce.ccd.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.ccd.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;

public class SolicitorCreate implements CcdConfiguration {

    public static final String SOLICITOR_CREATE = "solicitorCreate";

    @Override
    public void applyTo(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {

        configBuilder.event(SOLICITOR_CREATE)
            .initialState(SOTAgreementPayAndSubmitRequired)
            .name("Apply for a divorce")
            .description("Apply for a divorce")
            .displayOrder(1)
            .showSummary()
            .endButtonLabel("Save Petition")
            .explicitGrants()
            .grant("CRU", CASEWORKER_DIVORCE_SOLICITOR)
            .grant("RU", CASEWORKER_DIVORCE_SUPERUSER)
            .grant("R", CASEWORKER_DIVORCE_COURTADMIN_BETA, CASEWORKER_DIVORCE_COURTADMIN, CASEWORKER_DIVORCE_COURTADMIN_LA)
            .fields()

            .page("SolAboutTheSolicitor")
                .pageLabel("About the Solicitor")
                .field("LabelSolAboutEditingApplication-AboutSolicitor")
                    .readOnly()
                    .label("You can make changes at the end of your application.")
                    .showSummary(false)
                    .done()
                .field("LabelSolAboutTheSolPara-1")
                    .readOnly()
                    .label("Please note that the information provided will be used as evidence by the court to decide if "
                        + "the petitioner is entitled to legally end their marriage. **A copy of this form is sent to the "
                        + "respondent**")
                    .showSummary(false)
                    .done()
                .field(CaseData::getPetitionerSolicitorName, Mandatory, true)
                .field(CaseData::getD8SolicitorReference, Mandatory, true)
                .field(CaseData::getPetitionerSolicitorPhone, Mandatory, true)
                .field(CaseData::getPetitionerSolicitorEmail, Mandatory, true)
                .field(CaseData::getSolicitorAgreeToReceiveEmails, Mandatory, true)
                .field(CaseData::getDerivedPetitionerSolicitorAddr, Mandatory, true)

            .page("SolAboutThePetitioner")
                .pageLabel("About the petitioner")
                .field("LabelSolAboutEditingApplication-AboutPetitioner")
                    .readOnly()
                    .label("You can make changes at the end of your application.")
                    .showSummary(false)
                    .done()
                .field(CaseData::getD8PetitionerFirstName, Mandatory, true)
                .field(CaseData::getD8PetitionerLastName, Mandatory, true)
                .field(CaseData::getD8PetitionerNameDifferentToMarriageCert, Mandatory, true)
                .field("LabelSolAboutThePetPara-2")
                    .readOnly()
                    .label("About the petitioner")
                    .showCondition("D8PetitionerNameDifferentToMarriageCert=\"Yes\"")
                    .done()
                .field(CaseData::getD8PetitionerNameChangedHow)
                    .mandatory()
                    .showSummary()
                    .showCondition("D8PetitionerNameDifferentToMarriageCert=\"Yes\"")
                    .done()
                .field(CaseData::getD8PetitionerNameChangedHowOtherDetails)
                    .mandatory()
                    .showSummary()
                    .showCondition("D8PetitionerNameChangedHow=\"other\"")
                    .done()
                .field(CaseData::getD8DivorceWho, Mandatory, true)
                .field(CaseData::getD8InferredPetitionerGender, Mandatory, true)
                .field(CaseData::getD8MarriageIsSameSexCouple, Mandatory, true)
                .field(CaseData::getD8DerivedPetitionerHomeAddress, Mandatory, true)
                .field(CaseData::getD8PetitionerPhoneNumber, Optional, true)
                .field(CaseData::getD8PetitionerEmail, Optional, true)
                .field(CaseData::getD8PetitionerContactDetailsConfidential, Mandatory, true)

            .page("SolAboutTheRespondent")
                .pageLabel("About the respondent")
                .field("LabelSolAboutEditingApplication-AboutRespondent")
                    .readOnly()
                    .label("You can make changes at the end of your application.")
                    .showSummary(false)
                    .done()
                .field(CaseData::getD8RespondentFirstName, Mandatory, true)
                .field(CaseData::getD8RespondentLastName, Mandatory, true)
                .field(CaseData::getD8RespondentNameAsOnMarriageCertificate, Mandatory, true)
                .field(CaseData::getRespNameDifferentToMarriageCertExplain).optional().showSummary()
                    .showCondition("D8RespondentNameAsOnMarriageCertificate=\"Yes\"").done()
                .field(CaseData::getD8InferredRespondentGender, Mandatory, true);
    }
}
