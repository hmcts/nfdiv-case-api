package uk.gov.hmcts.divorce.common.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoSApplicant2;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoSApplicant2IfNo;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewApplicant2;
import uk.gov.hmcts.divorce.common.event.page.WithdrawingJointApplicationApplicant2;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Component
public class UpdateJointConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String UPDATE_JOINT_CONDITIONAL_ORDER = "update-joint-conditional-order";

    private final List<CcdPageConfiguration> pages = asList(
        new ConditionalOrderReviewAoSApplicant2(),
        new WithdrawingJointApplicationApplicant2(),
        new ConditionalOrderReviewAoSApplicant2IfNo(),
        new ConditionalOrderReviewApplicant2()
    );

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(UPDATE_JOINT_CONDITIONAL_ORDER)
            .forStates(ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Update conditional order")
            .description("Update joint conditional order")
            .endButtonLabel("Save conditional order")
            .showCondition("applicationType=\"jointApplication\" AND coApplicant2IsDrafted=\"Yes\" AND coApplicant2IsSubmitted!=\"Yes\"")
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }
}
