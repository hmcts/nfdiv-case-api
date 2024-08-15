package uk.gov.hmcts.divorce.systemupdate.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.service.task.ResendJSCitizenAOSResponseLetters;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.Collections;

import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;
import static uk.gov.hmcts.divorce.document.model.DocumentType.AOS_RESPONSE_LETTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class SystemRegenerateJsCitizenAosResponseCoverLetter implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER =
        "system-regen-js-citizen-aos-response-cover-letter";
    public static final String REGEN_JS_CITIZEN_AOS_RESPONSE_LETTER =
        "Regen JS Citizen AoS Response";
    public static final String RESPONSE_ALREADY_SENT_ERROR = "Not resending js citizen aos response pack to bulk print as already resent.";
    public static final String NOT_JS_ERROR = "Not a JS Case.";
    public static final String APP1_ONLINE_ERROR = "Not resending js citizen aos response pack to bulk print as applicant1 is not offline.";
    public static final String NO_RESPONSE_PACK_ERROR = "No JS Citizen AoS Response letter pack to Regenerate.";

    private final ResendJSCitizenAOSResponseLetters resendJSCitizenAOSResponseLetters;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER)
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
        log.info("{} callback invoked for Case Id: {}", SYSTEM_REGEN_JS_CITIZEN_AOS_RESPONSE_COVER_LETTER, details.getId());

        var caseData = details.getData();

        if (YES.equals(caseData.getApplication().getJsCitizenAosResponseLettersResent())) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(RESPONSE_ALREADY_SENT_ERROR))
                .data(caseData)
                .build();
        }

        if (caseData.isJudicialSeparationCase()) {
            if (caseData.getApplicant1().isApplicantOffline()) {
                if (caseData.getDocuments().getDocumentGeneratedWithType(AOS_RESPONSE_LETTER).isPresent()) {
                    log.info("Regenerating JS Citizen AoS Response letter pack for Case Id: {}", details.getId());

                    caseTasks(
                        resendJSCitizenAOSResponseLetters
                    ).run(details);
                } else {
                    return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                        .errors(Collections.singletonList(NO_RESPONSE_PACK_ERROR))
                        .data(caseData)
                        .build();
                }
            } else {
                return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                    .errors(Collections.singletonList(APP1_ONLINE_ERROR))
                    .data(caseData)
                    .build();
            }
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(Collections.singletonList(NOT_JS_ERROR))
                .data(caseData)
                .build();
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
