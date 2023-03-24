package uk.gov.hmcts.divorce.common.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoSApplicant2;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoSApplicant2IfNo;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewApplicant2;
import uk.gov.hmcts.divorce.common.event.page.WithdrawingJointApplicationApplicant2;
import uk.gov.hmcts.divorce.common.service.task.SetLatestBailiffApplicationStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressDraftConditionalOrderState;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Slf4j
@Component
public class DraftJointConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String DRAFT_JOINT_CONDITIONAL_ORDER = "draft-joint-conditional-order";

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    @Autowired
    private ProgressDraftConditionalOrderState progressDraftConditionalOrderState;

    @Autowired
    private SetLatestBailiffApplicationStatus setLatestBailiffApplicationStatus;

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
            .event(DRAFT_JOINT_CONDITIONAL_ORDER)
            .forStates(AwaitingConditionalOrder, ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Draft conditional order")
            .description("Draft conditional order")
            .showSummary()
            .endButtonLabel("Save conditional order")
            .showCondition("applicationType=\"jointApplication\" AND coApplicant2IsDrafted!=\"Yes\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE, APPLICANT_2_SOLICITOR, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Draft joint conditional order about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        final ConditionalOrder conditionalOrder = data.getConditionalOrder();

        if (!data.getApplicationType().isSole()
            && NO.equals(conditionalOrder.getConditionalOrderApplicant2Questions().getApplyForConditionalOrder())
            && YES.equals(conditionalOrder.getConditionalOrderApplicant2Questions().getApplyForConditionalOrderIfNo())) {

            conditionalOrder.getConditionalOrderApplicant2Questions().setApplyForConditionalOrder(YES);
            conditionalOrder.getConditionalOrderApplicant2Questions().setApplyForConditionalOrderIfNo(null);
        }

        data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsDrafted(YES);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .state(caseTasks(progressDraftConditionalOrderState)
                .run(details)
                .getState())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Draft joint conditional order about to start callback invoked for Case Id: {}", details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseTasks(addMiniApplicationLink, setLatestBailiffApplicationStatus)
                .run(details)
                .getData())
            .build();
    }
}
