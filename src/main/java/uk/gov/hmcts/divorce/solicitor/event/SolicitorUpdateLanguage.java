package uk.gov.hmcts.divorce.solicitor.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import static uk.gov.hmcts.divorce.common.model.State.SOTAgreementPayAndSubmitRequired;
import static uk.gov.hmcts.divorce.common.model.State.Submitted;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_BETA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_COURTADMIN_LA;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SOLICITOR;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SUPERUSER;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.READ_UPDATE;
import static uk.gov.hmcts.divorce.solicitor.event.page.CommonFieldSettings.SOLICITOR_NFD_PREVIEW_BANNER;

@Component
public class SolicitorUpdateLanguage implements CCDConfig<CaseData, State, UserRole> {

    public static final String SOLICITOR_UPDATE_LANGUAGE = "solicitor-update-language";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SOLICITOR_UPDATE_LANGUAGE)
            .forStates(SOTAgreementPayAndSubmitRequired, Submitted)
            .name("Update Language")
            .description("Update Language")
            .displayOrder(3)
            .showSummary()
            .explicitGrants()
            .grant(CREATE_READ_UPDATE, CASEWORKER_DIVORCE_SOLICITOR)
            .grant(READ_UPDATE, CASEWORKER_DIVORCE_SUPERUSER)
            .grant(READ,
                CASEWORKER_DIVORCE_COURTADMIN_BETA,
                CASEWORKER_DIVORCE_COURTADMIN,
                CASEWORKER_DIVORCE_COURTADMIN_LA))
            .page("langPref")
            .pageLabel("Select Language")
            .label("LabelNFDBanner-MarriageIrretrievablyBroken", SOLICITOR_NFD_PREVIEW_BANNER)
            .mandatory(CaseData::getLanguagePreferenceWelsh);
    }
}
