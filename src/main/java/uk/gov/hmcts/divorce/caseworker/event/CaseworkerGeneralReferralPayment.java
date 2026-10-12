package uk.gov.hmcts.divorce.caseworker.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralReferralPaymentConfirmation;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralReferralPaymentSummary;
import uk.gov.hmcts.divorce.caseworker.event.page.GeneralReferralSelectFee;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.EnumSet;
import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGenAppHWFEvidence;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGenAppHWFPartPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingGeneralReferralPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseworkerGeneralReferralPayment implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_GENERAL_REFERRAL_PAYMENT = "caseworker-general-referral-payment";

    private final GeneralReferralSelectFee generalReferralSelectFee;

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<CcdPageConfiguration> pages = asList(
            generalReferralSelectFee,
            new GeneralReferralPaymentConfirmation(),
            new GeneralReferralPaymentSummary()
        );

        var pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(CASEWORKER_GENERAL_REFERRAL_PAYMENT)
            .forStateTransition(EnumSet.of(AwaitingGeneralReferralPayment, AwaitingGenAppHWFPartPayment, AwaitingGenAppHWFEvidence),
                AwaitingGeneralConsideration)
            .name("General referral payment")
            .description("General referral payment")
            .showSummary()
            .showEventNotes()
            .grant(CREATE_READ_UPDATE, CASE_WORKER)
            .grantHistoryOnly(SUPER_USER, SOLICITOR, CITIZEN, LEGAL_ADVISOR));
    }
}
