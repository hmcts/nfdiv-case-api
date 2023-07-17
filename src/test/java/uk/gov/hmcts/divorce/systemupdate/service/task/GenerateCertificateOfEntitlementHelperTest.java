package uk.gov.hmcts.divorce.systemupdate.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.BEFORE_DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_FO_ELIGIBLE_FROM;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_END_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.CommonContent.IS_DIVORCE;
import static uk.gov.hmcts.divorce.notification.CommonContent.NAME;
import static uk.gov.hmcts.divorce.notification.CommonContent.PARTNER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper.GET_A_DIVORCE;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper.IS_JOINT;
import static uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlementHelper.IS_RESPONDENT;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_REFERENCE;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant2WithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContent;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getOfflineSolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getSolicitorDocTemplateContent;

@ExtendWith(MockitoExtension.class)
public class GenerateCertificateOfEntitlementHelperTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private CommonContent commonContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private GenerateCertificateOfEntitlementHelper generateCertificateOfEntitlementHelper;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(generateCertificateOfEntitlementHelper, "finalOrderOffsetDays", 43);
    }

    @Test
    void shouldGenerateCoETemplateContentForApplicant1() {
        setMockClock(clock);

        final CaseData caseData = getCaseData();
        final String applicant1Name =  join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName());
        final String applicant1Address = caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck();

        Map<String, Object> applicant1TemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        applicant1TemplateVars.put(NAME, applicant1Name);
        applicant1TemplateVars.put(ADDRESS, applicant1Address);
        applicant1TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant1TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant1TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant1TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant1TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant1TemplateVars);
    }

    @Test
    void shouldGenerateJudicialSeparationCoETemplateContentForApplicant1() {
        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final String applicant1Name =  join(" ", caseData.getApplicant1().getFirstName(), caseData.getApplicant1().getLastName());
        final String applicant1Address = caseData.getApplicant1().getCorrespondenceAddressWithoutConfidentialCheck();

        Map<String, Object> applicant1TemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        applicant1TemplateVars.put(NAME, applicant1Name);
        applicant1TemplateVars.put(ADDRESS, applicant1Address);
        applicant1TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant1TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant1TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant1TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant1TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(IS_JOINT, false);
        applicant1TemplateVars.put(PARTNER, "Wife");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant2(), ENGLISH))
            .thenReturn("Wife");

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant1TemplateVars);
    }

    @Test
    void shouldGenerateCoETemplateContentForApplicant1Solicitor() {
        setMockClock(clock);

        final CaseData caseData = getCaseDataWithSolicitor();

        Map<String, Object> applicant1TemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        applicant1TemplateVars.put(NAME, caseData.getApplicant1().getSolicitor().getName());
        applicant1TemplateVars.put(ADDRESS, caseData.getApplicant1().getSolicitor().getAddress());
        applicant1TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant1TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant1TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant1TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant1TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant1TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant1(), caseData.getApplicant2());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant1TemplateVars);
    }

    @Test
    void shouldGenerateJudicialSeparationCoETemplateContentForApplicant1Solicitor() {
        final CaseData caseData = getCaseDataWithSolicitor();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        Map<String, Object> applicant1TemplateVars = getBasicDocmosisTemplateContent(ENGLISH);
        applicant1TemplateVars.put(SOLICITOR_NAME, caseData.getApplicant1().getSolicitor().getName());
        applicant1TemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant1().getSolicitor().getReference());
        applicant1TemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant1().getSolicitor().getAddress());
        applicant1TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant1TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant1TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant1TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant1TemplateVars.put(IS_DIVORCE, true);
        applicant1TemplateVars.put(IS_JOINT, false);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, true, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant1()));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getSolicitorTemplateContent(
            caseData, TEST_CASE_ID, true, caseData.getApplicant1().getLanguagePreference());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant1TemplateVars);
    }

    @Test
    void shouldGenerateCoETemplateContentForRespondent() {
        setMockClock(clock);

        final CaseData caseData = getCaseData();
        final String respondentName =  join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName());
        final String respondentAddress = caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck();

        Map<String, Object> respondentTemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        respondentTemplateVars.put(NAME, respondentName);
        respondentTemplateVars.put(ADDRESS, respondentAddress);
        respondentTemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        respondentTemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        respondentTemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        respondentTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        respondentTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        respondentTemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(PARTNER, "Husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("Husband");

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getRespondentTemplateContent(
            caseData, TEST_CASE_ID);

        assertThat(results).containsExactlyInAnyOrderEntriesOf(respondentTemplateVars);
    }

    @Test
    void shouldGenerateCoETemplateContentForRespondentSolicitor() {
        setMockClock(clock);

        final CaseData caseData = getCaseDataWithSolicitor();

        Map<String, Object> respondentTemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        respondentTemplateVars.put(NAME, caseData.getApplicant2().getSolicitor().getName());
        respondentTemplateVars.put(ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        respondentTemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        respondentTemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        respondentTemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        respondentTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        respondentTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        respondentTemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(PARTNER, "Husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("Husband");

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getRespondentTemplateContent(
            caseData, TEST_CASE_ID);

        assertThat(results).containsExactlyInAnyOrderEntriesOf(respondentTemplateVars);
    }

    @Test
    void shouldGenerateJudicialSeparationCoETemplateContentForRespondent() {
        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final String respondentName =  join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName());
        final String respondentAddress = caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck();

        Map<String, Object> respondentTemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        respondentTemplateVars.put(NAME, respondentName);
        respondentTemplateVars.put(ADDRESS, respondentAddress);
        respondentTemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        respondentTemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        respondentTemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        respondentTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        respondentTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        respondentTemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(IS_RESPONDENT, true);
        respondentTemplateVars.put(IS_DIVORCE, true);
        respondentTemplateVars.put(IS_JOINT, false);
        respondentTemplateVars.put(PARTNER, "Husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("Husband");

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getRespondentTemplateContent(
            caseData, TEST_CASE_ID);

        assertThat(results).containsExactlyInAnyOrderEntriesOf(respondentTemplateVars);
    }

    @Test
    void shouldGenerateJudicialSeparationCoETemplateContentForRespondentSolicitor() {
        final CaseData caseData = getCaseDataWithSolicitor();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);

        Map<String, Object> respondentTemplateVars = getBasicDocmosisTemplateContent(ENGLISH);
        respondentTemplateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        respondentTemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant2().getSolicitor().getReference());
        respondentTemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        respondentTemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        respondentTemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        respondentTemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        respondentTemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        respondentTemplateVars.put(IS_DIVORCE, true);
        respondentTemplateVars.put(IS_JOINT, false);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant2()));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getSolicitorTemplateContent(
            caseData, TEST_CASE_ID, false, caseData.getApplicant2().getLanguagePreference());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(respondentTemplateVars);
    }

    @Test
    void shouldGenerateCoETemplateContentForApplicant2() {
        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        final String applicant2Name =  join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName());
        final String applicant2Address = caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck();

        Map<String, Object> applicant2TemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        applicant2TemplateVars.put(NAME, applicant2Name);
        applicant2TemplateVars.put(ADDRESS, applicant2Address);
        applicant2TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant2TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant2TemplateVars);
    }

    @Test
    void shouldGenerateCoETemplateContentForApplicant2Solicitor() {
        setMockClock(clock);

        final CaseData caseData = getCaseDataWithSolicitor();
        caseData.setApplicationType(JOINT_APPLICATION);

        Map<String, Object> applicant2TemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        applicant2TemplateVars.put(NAME, caseData.getApplicant2().getSolicitor().getName());
        applicant2TemplateVars.put(ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        applicant2TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant2TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant2TemplateVars);
    }

    @Test
    void shouldGenerateJudicialSeparationCoETemplateContentForApplicant2() {
        setMockClock(clock);

        final CaseData caseData = getCaseData();
        caseData.setApplicationType(JOINT_APPLICATION);
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        final String applicant2Name =  join(" ", caseData.getApplicant2().getFirstName(), caseData.getApplicant2().getLastName());
        final String applicant2Address = caseData.getApplicant2().getCorrespondenceAddressWithoutConfidentialCheck();

        Map<String, Object> applicant2TemplateVars = getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH);
        applicant2TemplateVars.put(NAME, applicant2Name);
        applicant2TemplateVars.put(ADDRESS, applicant2Address);
        applicant2TemplateVars.put(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant2TemplateVars.put(DIVORCE_OR_END_CIVIL_PARTNERSHIP, GET_A_DIVORCE);
        applicant2TemplateVars.put(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE);

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(DATE_FO_ELIGIBLE_FROM,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().plusDays(43).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(BEFORE_DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().minusDays(7).format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(IS_JOINT, true);
        applicant2TemplateVars.put(PARTNER, "Husband");

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(ENGLISH))
            .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        when(commonContent.getPartner(caseData, caseData.getApplicant1(), caseData.getApplicant2().getLanguagePreference()))
            .thenReturn("Husband");

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getTemplateContent(
            caseData, TEST_CASE_ID, caseData.getApplicant2(), caseData.getApplicant1());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant2TemplateVars);
    }

    @Test
    void shouldGenerateJudicialSeparationCoETemplateContentForApplicant2Solicitor() {
        final CaseData caseData = getCaseDataWithSolicitor();
        caseData.setSupplementaryCaseType(JUDICIAL_SEPARATION);
        caseData.setApplicationType(JOINT_APPLICATION);

        Map<String, Object> applicant2TemplateVars = getBasicDocmosisTemplateContent(ENGLISH);
        applicant2TemplateVars.put(SOLICITOR_NAME, caseData.getApplicant2().getSolicitor().getName());
        applicant2TemplateVars.put(SOLICITOR_REFERENCE, caseData.getApplicant2().getSolicitor().getReference());
        applicant2TemplateVars.put(SOLICITOR_ADDRESS, caseData.getApplicant2().getSolicitor().getAddress());
        applicant2TemplateVars.put(CASE_REFERENCE, formatId(TEST_CASE_ID));

        applicant2TemplateVars.put(COURT_NAME, caseData.getConditionalOrder().getCourt().getLabel());
        applicant2TemplateVars.put(DATE_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(DATE_TIME_FORMATTER));
        applicant2TemplateVars.put(TIME_OF_HEARING,
            caseData.getConditionalOrder().getDateAndTimeOfHearing().format(TIME_FORMATTER));
        applicant2TemplateVars.put(IS_DIVORCE, true);
        applicant2TemplateVars.put(IS_JOINT, true);

        when(docmosisCommonContent.getBasicSolicitorTemplateContent(caseData, TEST_CASE_ID, false, ENGLISH))
            .thenReturn(getSolicitorDocTemplateContent(caseData, caseData.getApplicant2()));

        Map<String, Object> results = generateCertificateOfEntitlementHelper.getSolicitorTemplateContent(
            caseData, TEST_CASE_ID, false, caseData.getApplicant2().getLanguagePreference());

        assertThat(results).containsExactlyInAnyOrderEntriesOf(applicant2TemplateVars);
    }

    private CaseData getCaseDataWithSolicitor() {
        final CaseData caseData = getCaseData();
        caseData.getApplicant1().setSolicitorRepresented(YES);
        caseData.getApplicant1().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant1().getSolicitor().setReference(TEST_REFERENCE);
        caseData.getApplicant2().setSolicitorRepresented(YES);
        caseData.getApplicant2().setSolicitor(getOfflineSolicitor());
        caseData.getApplicant2().getSolicitor().setReference(TEST_REFERENCE);

        return caseData;
    }

    private CaseData getCaseData() {
        final CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.setApplicant1(getApplicantWithAddress());
        caseData.getApplicant1().setEmail(null);
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant1().setSolicitorRepresented(NO);
        caseData.setApplicant2(getApplicant2WithAddress());
        caseData.getApplicant2().setEmail(null);
        caseData.getApplicant2().setOffline(YES);
        caseData.getApplicant2().setSolicitorRepresented(NO);
        caseData.setConditionalOrder(ConditionalOrder.builder()
            .dateAndTimeOfHearing(LocalDateTime.of(2022, 4, 28, 10, 0, 0))
            .court(BURY_ST_EDMUNDS)
            .build());

        return caseData;
    }
}
