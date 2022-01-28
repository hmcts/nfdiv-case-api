package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.time.Clock;
import java.time.LocalDate;

import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffReferral;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingBailiffService;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.READ;

@Component
@Slf4j
public class CaseworkerNoticeOfChange implements CCDConfig<CaseData, State, UserRole> {
    public static final String CASEWORKER_NOTICE_OF_CHANGE = "caseworker-notice-of-change";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CASEWORKER_NOTICE_OF_CHANGE)
            .forAllStates()
            .name("Notice of change")
            .description("Change applicant representation")
            .showSummary()
            .showEventNotes()
            .aboutToSubmitCallback(this::aboutToSubmit)
            .grant(CREATE_READ, CASE_WORKER, SUPER_USER)
            .grant(READ, LEGAL_ADVISOR))
            .page("changeRepresentation-1")
            .pageLabel("Which applicant")
            .complex(CaseData::getNoticeOfChange)
                .mandatory(NoticeOfChange::getWhichApplicant)
                .done()
            .page("changeRepresentation-2")
            .showCondition("nocWhichApplicant=\"applicant1\"")
            .pageLabel("Change applicant 1 representation")
            .complex(CaseData::getNoticeOfChange)
                .mandatory(NoticeOfChange::getAreTheyRepresented)
                .done()
            .complex(CaseData::getApplicant1)
                .complex(Applicant::getSolicitor)
                    .optional(Solicitor::getName, "nocAreTheyRepresented=\"Yes\"")
                    .optional(Solicitor::getPhone, "nocAreTheyRepresented=\"Yes\"")
                    .optional(Solicitor::getEmail, "nocAreTheyRepresented=\"Yes\"")
                    .optional(Solicitor::getAddress, "nocAreTheyRepresented=\"Yes\"")
                    .done()
                .label("nocWhereSentLabel", "Where should documents be sent?")
                .optional(Applicant::getCorrespondenceAddress, "nocAreTheyRepresented=\"No\"")
                .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        log.info("Caseworker notice of change aboutToSubmit callback started");
        // TODO set offline, remove org policy, remove case access

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .build();
    }
}
