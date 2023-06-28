package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoS;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewAoSIfNo;
import uk.gov.hmcts.divorce.common.event.page.ConditionalOrderReviewApplicant1;
import uk.gov.hmcts.divorce.common.event.page.WithdrawingJointApplicationApplicant1;
import uk.gov.hmcts.divorce.common.service.task.SetLatestBailiffApplicationStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.task.AddLastAlternativeServiceDocumentLink;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;
import uk.gov.hmcts.divorce.solicitor.service.task.AddOfflineRespondentAnswersLink;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressDraftConditionalOrderState;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.sortByNewest;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingConditionalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPending;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.divorce.divorcecase.task.CaseTaskRunner.caseTasks;

@Slf4j
@Component
public class DraftConditionalOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String DRAFT_CONDITIONAL_ORDER = "draft-conditional-order";

    private final List<CcdPageConfiguration> pages = asList(
        new ConditionalOrderReviewAoS(),
        new WithdrawingJointApplicationApplicant1(),
        new ConditionalOrderReviewAoSIfNo(),
        new ConditionalOrderReviewApplicant1()
    );

    @Autowired
    private AddMiniApplicationLink addMiniApplicationLink;

    @Autowired
    private ProgressDraftConditionalOrderState progressDraftConditionalOrderState;

    @Autowired
    private SetLatestBailiffApplicationStatus setLatestBailiffApplicationStatus;

    @Autowired
    private AddLastAlternativeServiceDocumentLink addLastAlternativeServiceDocumentLink;

    @Autowired
    private AddOfflineRespondentAnswersLink addOfflineRespondentAnswersLink;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final PageBuilder pageBuilder = addEventConfig(configBuilder);
        pages.forEach(page -> page.addTo(pageBuilder));
    }

    private PageBuilder addEventConfig(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        return new PageBuilder(configBuilder
            .event(DRAFT_CONDITIONAL_ORDER)
            .forStates(AwaitingConditionalOrder, ConditionalOrderDrafted, ConditionalOrderPending)
            .name("Draft conditional order")
            .description("Draft conditional order")
            .showSummary()
            .endButtonLabel("Save conditional order")
            .showCondition("coApplicant1IsDrafted!=\"Yes\"")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .aboutToStartCallback(this::aboutToStart)
            .grant(CREATE_READ_UPDATE, APPLICANT_1_SOLICITOR, CREATOR, APPLICANT_2)
            .grantHistoryOnly(
                CASE_WORKER,
                SUPER_USER,
                LEGAL_ADVISOR,
                JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {

        log.info("Draft conditional order about to submit callback invoked for Case Id: {}", details.getId());

        final CaseData data = details.getData();
        final ConditionalOrder conditionalOrder = data.getConditionalOrder();

        if (ccdAccessService.isApplicant2(httpServletRequest.getHeader(AUTHORIZATION), details.getId())) {

            final ConditionalOrderQuestions applicant2Questions = conditionalOrder.getConditionalOrderApplicant2Questions();

            if (!data.getApplicationType().isSole()
                && NO.equals(applicant2Questions.getApplyForConditionalOrder())
                && YES.equals(applicant2Questions.getApplyForConditionalOrderIfNo())) {

                applicant2Questions.setApplyForConditionalOrder(YES);
                applicant2Questions.setApplyForConditionalOrderIfNo(null);
            }

            data.getConditionalOrder().getConditionalOrderApplicant2Questions().setIsDrafted(YES);
        } else {

            final ConditionalOrderQuestions applicant1Questions = conditionalOrder.getConditionalOrderApplicant1Questions();

            if (!data.getApplicationType().isSole()
                && NO.equals(applicant1Questions.getApplyForConditionalOrder())
                && YES.equals(applicant1Questions.getApplyForConditionalOrderIfNo())) {

                applicant1Questions.setApplyForConditionalOrder(YES);
                applicant1Questions.setApplyForConditionalOrderIfNo(null);
            }

            data.getConditionalOrder().getConditionalOrderApplicant1Questions().setIsDrafted(YES);
        }

        data.getConditionalOrder().setProofOfServiceUploadDocuments(sortByNewest(
            beforeDetails.getData().getConditionalOrder().getProofOfServiceUploadDocuments(),
            data.getConditionalOrder().getProofOfServiceUploadDocuments()
        ));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(details.getData())
            .state(caseTasks(progressDraftConditionalOrderState)
                .run(details)
                .getState())
            .build();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToStart(final CaseDetails<CaseData, State> details) {

        log.info("Draft conditional order about to start callback invoked for Case Id: {}", details.getId());

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseTasks(
                addMiniApplicationLink,
                addLastAlternativeServiceDocumentLink,
                setLatestBailiffApplicationStatus,
                addOfflineRespondentAnswersLink)
                .run(details)
                .getData())
            .build();
    }
}
