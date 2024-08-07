package uk.gov.hmcts.divorce.noticeofchange;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.ccd.sdk.type.ChangeOrganisationRequest;
import uk.gov.hmcts.ccd.sdk.type.DynamicListItem;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseRoleID;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.divorce.solicitor.client.organisation.FindUsersByOrganisationResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationClient;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationContactInformation;
import uk.gov.hmcts.divorce.solicitor.client.organisation.OrganisationsResponse;
import uk.gov.hmcts.divorce.solicitor.client.organisation.ProfessionalUser;
import uk.gov.hmcts.divorce.testutil.TestDataHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor.CASEWORKER_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor.SOLICITOR_NOTICE_OF_CHANGE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.APP_1_SOL_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SOLICITOR_USER_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
class ChangeOfRepresentativeServiceTest {
    private static final String TEST_ORGANISATION_NAME = "organisation_name";
    private static final String TEST_ORGANISATION_USER_ID = "user_id";
    private static final String TEST_ORGANISATION_ID = "organisation_id";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private IdamService idamService;

    @Mock
    private OrganisationClient organisationClient;

    @Mock
    HttpServletRequest httpServletRequest;

    @Mock
    private User systemUser;

    @InjectMocks
    private ChangeOfRepresentativeService changeOfRepresentativeService;

    void setup() {
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
    }

    @Test
    void shouldApplyNoticeOfChangeForApplicant2SolicitorWhenSolicitorNoticeOfChange() {
        setup();

        List<ProfessionalUser> professionalUsers = new ArrayList<>();
        professionalUsers.add(ProfessionalUser.builder().email(TEST_SOLICITOR_EMAIL).userIdentifier(TEST_ORGANISATION_USER_ID).build());
        FindUsersByOrganisationResponse findUsersByOrganisationResponse = FindUsersByOrganisationResponse
                .builder().users(professionalUsers).build();
        when(organisationClient.getOrganisationUsers(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_ORGANISATION_ID))
                .thenReturn(findUsersByOrganisationResponse);

        OrganisationsResponse organisationsResponse = OrganisationsResponse.builder()
            .name(TEST_ORGANISATION_NAME)
            .contactInformation(List.of(
                OrganisationContactInformation.builder().addressLine1(TEST_SOLICITOR_ADDRESS).build()
            ))
            .build();

        when(organisationClient.getOrganisationByUserId(TEST_AUTHORIZATION_TOKEN, TEST_SERVICE_AUTH_TOKEN, TEST_ORGANISATION_USER_ID))
                .thenReturn(organisationsResponse);
        CaseData applicant2CaseData = buildCaseDataApplicant2();

        changeOfRepresentativeService.buildChangeOfRepresentative(applicant2CaseData, null, SOLICITOR_NOTICE_OF_CHANGE.getValue(), false);

        var changeOfRepresentative =  Optional.ofNullable(applicant2CaseData.getChangeOfRepresentatives())
                .orElse(Collections.emptyList())
                .stream()
                .map(ListValue::getValue)
                .findFirst()
                .orElseThrow();

        assertEquals(TEST_ORGANISATION_NAME, changeOfRepresentative.getAddedRepresentative().getOrganisation().getOrganisationName());
        assertEquals(TEST_ORGANISATION_ID, changeOfRepresentative.getAddedRepresentative().getOrganisation().getOrganisationId());
        assertEquals(TEST_ORG_NAME, changeOfRepresentative.getRemovedRepresentative().getOrganisation().getOrganisationName());
        assertEquals(TEST_ORG_ID, changeOfRepresentative.getRemovedRepresentative().getOrganisation().getOrganisationId());
        assertEquals(TEST_SOLICITOR_EMAIL, applicant2CaseData.getApplicant2().getSolicitor().getEmail());
        assertEquals(TEST_ORGANISATION_NAME, applicant2CaseData.getApplicant2().getSolicitor().getFirmName());
        assertEquals(TEST_SOLICITOR_ADDRESS, applicant2CaseData.getApplicant2().getSolicitor().getAddress());
        assertEquals(null, applicant2CaseData.getApplicant2().getSolicitor().getAddressOverseas());
        assertEquals(null, applicant2CaseData.getApplicant2().getSolicitor().getPhone());
        assertEquals(null, applicant2CaseData.getApplicant2().getSolicitor().getReference());
        assertEquals(Collections.emptySet(), applicant2CaseData.getApplicant2().getSolicitor().getAgreeToReceiveEmailsCheckbox());

        assertTrue(applicant2CaseData.getApplicant2().isRepresented());
        assertFalse(applicant2CaseData.getApplicant2().isApplicantOffline());
        assertEquals("Respondent", changeOfRepresentative.getParty());
        assertEquals(SOLICITOR_NOTICE_OF_CHANGE.getValue(), changeOfRepresentative.getUpdatedVia());
    }

    @Test
    void shouldApplyNoticeOfChangeForApplicant2SolicitorWhenCaseworkerNoticeOfChange() {
        CaseData applicant2CaseData = buildCaseDataApplicant2();
        applicant2CaseData.setApplicationType(JOINT_APPLICATION);
        CaseData beforeData = buildCaseDataApplicant2();
        beforeData.getApplicant2().setSolicitor(Solicitor.builder().email(TEST_SOLICITOR_EMAIL).name(TEST_SOLICITOR_NAME)
                .organisationPolicy(OrganisationPolicy.<UserRole>builder()
                        .organisation(Organisation.builder()
                                .organisationId(TEST_ORGANISATION_ID).organisationName(TEST_ORGANISATION_NAME).build())
                        .build())
                .build());

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(APP_1_SOL_AUTH_TOKEN);
        when(idamService.retrieveUser(APP_1_SOL_AUTH_TOKEN)).thenReturn(solicitorUser());

        changeOfRepresentativeService.buildChangeOfRepresentative(applicant2CaseData,
                beforeData, CASEWORKER_NOTICE_OF_CHANGE.getValue(), false);

        var changeOfRepresentative =  Optional.ofNullable(applicant2CaseData.getChangeOfRepresentatives())
                .orElse(Collections.emptyList())
                .stream()
                .map(ListValue::getValue)
                .findFirst()
                .orElseThrow();

        assertEquals(TEST_ORG_NAME, changeOfRepresentative.getAddedRepresentative().getOrganisation().getOrganisationName());
        assertEquals(TEST_ORG_ID, changeOfRepresentative.getAddedRepresentative().getOrganisation().getOrganisationId());
        assertEquals(TEST_ORGANISATION_NAME, changeOfRepresentative.getRemovedRepresentative().getOrganisation().getOrganisationName());
        assertEquals(TEST_ORGANISATION_ID, changeOfRepresentative.getRemovedRepresentative().getOrganisation().getOrganisationId());
        assertEquals(TEST_SOLICITOR_EMAIL, applicant2CaseData.getApplicant2().getSolicitor().getEmail());
        assertEquals("Applicant2", changeOfRepresentative.getParty());
        assertEquals(CASEWORKER_NOTICE_OF_CHANGE.getValue(), changeOfRepresentative.getUpdatedVia());
    }

    private CaseData buildCaseDataApplicant1() {
        final Applicant applicant1 = TestDataHelper.applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPONESOLICITOR]",
                "APPLICANT_1_SOLICITOR");

        return CaseData.builder()
                .applicationType(SOLE_APPLICATION)
                .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
                .applicant1(applicant1)
                .changeOrganisationRequestField(changeOrganisationRequest)
                .build();
    }

    private CaseData buildCaseDataApplicant2() {
        final Applicant applicant2 = TestDataHelper.respondentWithDigitalSolicitor();
        final ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = getChangeOrganisationRequestField("[APPTWOSOLICITOR]",
                "APPLICANT_2_SOLICITOR");

        return CaseData.builder()
                .applicationType(SOLE_APPLICATION)
                .divorceOrDissolution(DivorceOrDissolution.DIVORCE)
                .applicant2(applicant2)
                .changeOrganisationRequestField(changeOrganisationRequest)
                .build();
    }

    private static ChangeOrganisationRequest<CaseRoleID> getChangeOrganisationRequestField(String role, String roleLabel) {
        DynamicListItem dynamicListItem = DynamicListItem.builder().label(
                roleLabel).code(role).build();
        List<DynamicListItem> dynamicListItemList = new ArrayList<>();
        dynamicListItemList.add(dynamicListItem);

        ChangeOrganisationRequest<CaseRoleID> changeOrganisationRequest = ChangeOrganisationRequest.<CaseRoleID>builder().build();
        changeOrganisationRequest.setCaseRoleId(CaseRoleID.builder().value(dynamicListItem).listItems(dynamicListItemList).build());
        changeOrganisationRequest.setCreatedBy(TEST_SOLICITOR_EMAIL);
        changeOrganisationRequest.setOrganisationToAdd(Organisation
                .builder().organisationId(TEST_ORGANISATION_ID).organisationName(TEST_ORG_NAME).build());
        changeOrganisationRequest.setOrganisationToRemove(Organisation
                .builder().organisationId(TEST_ORG_ID).organisationName(TEST_ORGANISATION_NAME).build());
        return changeOrganisationRequest;
    }

    private Map<String, Object> expectedData(final CaseData caseData) {

        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.convertValue(caseData, new TypeReference<>() {
        });
    }

    private User solicitorUser() {
        return getUser();
    }

    private User getUser() {
        return new User(
                APP_1_SOL_AUTH_TOKEN,
                UserInfo.builder()
                        .uid(SOLICITOR_USER_ID)
                        .build());
    }
}
