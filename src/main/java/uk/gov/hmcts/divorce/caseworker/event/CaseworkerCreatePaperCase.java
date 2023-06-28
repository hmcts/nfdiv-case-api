package uk.gov.hmcts.divorce.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.NewPaperCase;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER_BULK_SCAN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CaseworkerCreatePaperCase implements CCDConfig<CaseData, State, UserRole> {

    public static final String CREATE_PAPER_CASE = "create-paper-case";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        new PageBuilder(configBuilder
            .event(CREATE_PAPER_CASE)
            .initialState(NewPaperCase)
            .aboutToSubmitCallback(this::aboutToSubmit)
            .name("Create paper case")
            .description("Create paper case")
            .grant(CREATE_READ_UPDATE, CASE_WORKER, CASE_WORKER_BULK_SCAN, SYSTEMUPDATE)
            .grantHistoryOnly(SUPER_USER, JUDGE));
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> details,
                                                                       final CaseDetails<CaseData, State> beforeDetails) {
        log.info("Bulk scan create paper case about to submit callback invoked for Case Id: {}", details.getId());

        CaseData data = details.getData();
        data.getApplicant1().setOffline(YES);
        data.setHyphenatedCaseRef(data.formatCaseRef(details.getId()));
        data.getApplication().setNewPaperCase(YES);
        data.getLabelContent().setApplicationType(data.getApplicationType());
        data.getLabelContent().setUnionType(data.getDivorceOrDissolution());

        var applicant2 = data.getApplicant2();
        if (data.isJudicialSeparationCase() || JOINT_APPLICATION.equals(data.getApplicationType())) {
            applicant2.setOffline(YES);
        } else if (SOLE_APPLICATION.equals(data.getApplicationType()) && isBlank(applicant2.getEmail())) {
            applicant2.setOffline(YES);
        } else {
            applicant2.setOffline(NO);
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }
}
