package uk.gov.hmcts.divorce.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.common.config.EmailTemplatesConfig;
import uk.gov.hmcts.divorce.common.model.CaseData;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.divorce.common.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.APPLICATION_TO_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DISSOLUTION_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_APPLICATION;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.DIVORCE_COURT_EMAIL;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RELATIONSHIP;
import static uk.gov.hmcts.divorce.notification.NotificationConstants.RELATIONSHIP_COURT_HEADER;

@ExtendWith(MockitoExtension.class)
class CommonContentTest {

    @Mock
    private EmailTemplatesConfig emailTemplatesConfig;

    @InjectMocks
    private CommonContent commonContent;

    @Test
    void shouldSetCommonTemplateVariablesForDivorce() {

        final String courtEmail = "court@email.com";
        final Map<String, String> configTemplateVars = new HashMap<>();
        configTemplateVars.put(DIVORCE_COURT_EMAIL, courtEmail);

        final String firstName = "John";
        final String lastName = "Smith";
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DIVORCE)
            .applicant1FirstName(firstName)
            .applicant1LastName(lastName)
            .build();

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(configTemplateVars);

        final Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        assertThat(templateVars.size(), is(5));
        assertThat(templateVars.get(FIRST_NAME), is(firstName));
        assertThat(templateVars.get(LAST_NAME), is(lastName));
        assertThat(templateVars.get(RELATIONSHIP), is(DIVORCE_APPLICATION));
        assertThat(templateVars.get(RELATIONSHIP_COURT_HEADER), is("Divorce service"));
        assertThat(templateVars.get(COURT_EMAIL), is(courtEmail));
    }

    @Test
    void shouldSetCommonTemplateVariablesForDissolution() {

        final String courtEmail = "court@email.com";
        final Map<String, String> configTemplateVars = new HashMap<>();
        configTemplateVars.put(DISSOLUTION_COURT_EMAIL, courtEmail);

        final String firstName = "John";
        final String lastName = "Smith";
        final CaseData caseData = CaseData.builder()
            .divorceOrDissolution(DISSOLUTION)
            .applicant1FirstName(firstName)
            .applicant1LastName(lastName)
            .build();

        when(emailTemplatesConfig.getTemplateVars()).thenReturn(configTemplateVars);

        final Map<String, String> templateVars = commonContent.templateVarsFor(caseData);

        assertThat(templateVars.size(), is(5));
        assertThat(templateVars.get(FIRST_NAME), is(firstName));
        assertThat(templateVars.get(LAST_NAME), is(lastName));
        assertThat(templateVars.get(RELATIONSHIP), is(APPLICATION_TO_END_CIVIL_PARTNERSHIP));
        assertThat(templateVars.get(RELATIONSHIP_COURT_HEADER), is("End a civil partnership service"));
        assertThat(templateVars.get(COURT_EMAIL), is(courtEmail));
    }
}
