package uk.gov.hmcts.divorce.citizen.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.common.model.State.AwaitingApplicant2Response;
import static uk.gov.hmcts.divorce.common.model.UserRole.CASEWORKER_DIVORCE_SYSTEMUPDATE;
import static uk.gov.hmcts.divorce.common.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.common.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CitizenLinkApplication implements CCDConfig<CaseData, State, UserRole> {

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    public static final String CITIZEN_LINK_APPLICANT_2 = "citizen-link-applicant2";

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(CITIZEN_LINK_APPLICANT_2)
            .forState(AwaitingApplicant2Response)
            .name("Link Applicant 2 to case")
            .description("Link Applicant 2 to case to enable completion of joint application")
            .grant(CREATE_READ_UPDATE, CITIZEN, CASEWORKER_DIVORCE_SYSTEMUPDATE)
            .displayOrder(1)
            .retries(120, 120)
            .aboutToSubmitCallback(this::aboutToSubmit);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        CaseData data = details.getData();
        CaseData beforeData = beforeDetails.getData();

        if (data.getAccessCode().equals(beforeData.getAccessCode())) {
            log.info("Linking Applicant 2 to Case");
            ccdAccessService.linkRespondentToApplication(
                httpServletRequest.getHeader(AUTHORIZATION),
                details.getId(),
                beforeData.getRespondentUserId()
            );

            data.setAccessCode(null);

            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .state(AwaitingApplicant2Response)
                .build();
        } else {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .state(AwaitingApplicant2Response)
                .errors(singletonList("You have entered the wrong access code. Check your email and enter it again before continuing."))
                .build();
        }
    }
}
