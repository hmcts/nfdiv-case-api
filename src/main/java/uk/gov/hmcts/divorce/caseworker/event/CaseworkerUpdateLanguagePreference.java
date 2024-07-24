package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static uk.gov.hmcts.divorce.divorcecase.model.State.POST_SUBMISSION_STATES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
public class CaseworkerUpdateLanguagePreference implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_UPDATE_LANGUAGE_PREFERENCE = "caseworker-update-language-preference";
    private static final String CASEWORKER_UPDATE_LANGUAGE = "Update language preference";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_UPDATE_LANGUAGE_PREFERENCE)
            .forStates(POST_SUBMISSION_STATES)
            .name(CASEWORKER_UPDATE_LANGUAGE)
            .description(CASEWORKER_UPDATE_LANGUAGE)
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(
                SOLICITOR,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE))
            .page("updateLanguagePreference")
            .pageLabel(CASEWORKER_UPDATE_LANGUAGE)
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getLanguagePreferenceWelsh,
                    "Is the ${labelContentApplicantsOrApplicant1s} language preference Welsh?")
            .done()
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getLanguagePreferenceWelsh,
                    "Is the ${labelContentRespondentsOrApplicant2s} language preference Welsh?")
            .done();
    }
}
