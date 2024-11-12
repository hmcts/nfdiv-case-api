package uk.gov.hmcts.divorce.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseInvite;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

import static java.lang.System.getenv;
import static java.util.Collections.singletonList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Draft;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CASE_WORKER;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.JUDGE;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.LEGAL_ADVISOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.divorce.divorcecase.model.access.Permissions.CREATE_READ_UPDATE;

@Slf4j
@Component
public class CreateTestCase implements CCDConfig<CaseData, State, UserRole> {
    private static final String ENVIRONMENT_AAT = "aat";
    private static final String TEST_CREATE = "create-test-application";
    private static final String SOLE_APPLICATION = "classpath:data/sole.json";
    private static final String JOINT_APPLICATION = "classpath:data/joint.json";
    public static volatile boolean submittedCallbackTriggered = false;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        var roles = new ArrayList<UserRole>();
        var env = getenv().getOrDefault("S2S_URL_BASE", "aat");

        if (env.contains(ENVIRONMENT_AAT)) {
            roles.add(SOLICITOR);
            roles.add(CASE_WORKER);
        }

        new PageBuilder(configBuilder
            .event(TEST_CREATE)
            .initialState(Draft)
            .name("Create test case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, roles.toArray(UserRole[]::new))
            .grantHistoryOnly(SUPER_USER, CASE_WORKER, LEGAL_ADVISOR, SOLICITOR, CITIZEN, JUDGE))
            .page("Create test case", this::midEvent)
            .mandatory(CaseData::getApplicationType)
            .complex(CaseData::getApplicant1)
                .mandatoryWithLabel(Applicant::getSolicitorRepresented, "Is applicant 1 represented")
                .done()
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getSolicitorRepresented, "Is applicant 2 represented")
                .done()
            .complex(CaseData::getCaseInvite)
                .label("userIdLabel", "<pre>Use ./bin/get-user-id-by-email.sh [email] to get an ID"
                    + ".\n\nTEST_SOLICITOR@mailinator.com is 93b108b7-4b26-41bf-ae8f-6e356efb11b3 in AAT.\n</pre>")
                .mandatoryWithLabel(CaseInvite::applicant2UserId, "Applicant 2 user ID")
                .done()
            .complex(CaseData::getApplication)
                .mandatoryWithLabel(Application::getStateToTransitionApplicationTo, "Case state")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        final CaseData data = details.getData();
        try {
            UUID uuid = UUID.fromString(data.getCaseInvite().applicant2UserId());
            log.info("{}", uuid);
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(data)
                .build();
        } catch (IllegalArgumentException e) {
            return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .errors(singletonList("User ID entered for applicant 2 is an invalid UUID"))
                .build();
        }
    }

    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        var file = details.getData().getApplicationType().isSole() ? SOLE_APPLICATION : JOINT_APPLICATION;
        var resourceLoader = new DefaultResourceLoader();
        var json = IOUtils.toString(resourceLoader.getResource(file).getInputStream(), Charset.defaultCharset());
        var fixture = objectMapper.readValue(json, CaseData.class);

        fixture.getApplicant1().setSolicitorRepresented(details.getData().getApplicant1().getSolicitorRepresented());
        fixture.getApplicant2().setSolicitorRepresented(details.getData().getApplicant2().getSolicitorRepresented());
        fixture.setCaseInvite(details.getData().getCaseInvite());
        fixture.setHyphenatedCaseRef(fixture.formatCaseRef(details.getId()));

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(fixture)
            .state(details.getData().getApplication().getStateToTransitionApplicationTo())
            .build();
    }


    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details, CaseDetails<CaseData, State> before) {
        submittedCallbackTriggered = true;
        var data = details.getData();
        var caseId = details.getId();
        var app2Id = data.getCaseInvite().applicant2UserId();
        var auth = httpServletRequest.getHeader(AUTHORIZATION);

        if (data.getApplicant1().isRepresented()) {
            var orgId = details
                .getData()
                .getApplicant1()
                .getSolicitor()
                .getOrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

            ccdAccessService.addApplicant1SolicitorRole(auth, caseId, orgId);
        }

        if (data.getCaseInvite().applicant2UserId() != null && data.getApplicant2().isRepresented()) {
            var orgId = details
                .getData()
                .getApplicant2()
                .getSolicitor()
                .getOrganisationPolicy()
                .getOrganisation()
                .getOrganisationId();

            ccdAccessService.addRoleToCase(app2Id, caseId, orgId, APPLICANT_1_SOLICITOR);
        } else if (data.getCaseInvite().applicant2UserId() != null) {
            ccdAccessService.linkRespondentToApplication(auth, caseId, app2Id);
        }

        return SubmittedCallbackResponse.builder().build();
    }
}
