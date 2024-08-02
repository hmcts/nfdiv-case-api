package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.SendRegeneratedCOPronouncedCoverLetters;
import uk.gov.hmcts.divorce.common.service.task.SendRegeneratedJSCitizenAOSResponseLetters;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateConditionalOrderPronouncedCoverLetter;
import uk.gov.hmcts.divorce.systemupdate.service.task.RegenerateJudicialSeparationCitizenAosResponseLetter;

import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRegenerateJsCitizenAosResponseLetter implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_LETTER =
        "system-regen-js-citizen-aos-response-letter";
    public static final String REGEN_JS_CITIZEN_AOS_RESPONSE_LETTER =
        "Regen JS Citizen AoS Response";

    private final RegenerateJudicialSeparationCitizenAosResponseLetter regenerateJudicialSeparationCitizenAosResponseLetter;
    private final SendRegeneratedJSCitizenAOSResponseLetters sendRegeneratedJSCitizenAOSResponseLetters;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_LETTER)
            .forAllStates()
            .name(REGEN_JS_CITIZEN_AOS_RESPONSE_LETTER)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ_UPDATE, SYSTEMUPDATE)
            .grantHistoryOnly(CASE_WORKER, LEGAL_ADVISOR, SOLICITOR));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("{} callback invoked for Case Id: {}", SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_LETTER, details.getId());

        var caseData = details.getData();

        if (caseData.isJudicialSeparationCase()) {
            if (caseData.getDocuments().getDocumentGeneratedWithType(AOS_RESPONSE_LETTER).isPresent()) {
                log.info("Regenerating JS Citizen AoS Response letter for Case Id: {}", details.getId());

                caseTasks(
                    regenerateJudicialSeparationCitizenAosResponseLetter,
                    sendRegeneratedJSCitizenAOSResponseLetters
                ).run(details);
            } else {
                log.info("No JS Citizen AoS Response letter to Regenerate on Case Id: {}", details.getId());
            }
        } else {
            log.info("Not a JS Case. Case Id: {}", details.getId());
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
