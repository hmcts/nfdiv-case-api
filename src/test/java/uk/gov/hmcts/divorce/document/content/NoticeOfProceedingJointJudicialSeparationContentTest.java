package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;
import uk.gov.hmcts.divorce.divorcecase.model.DivorceOrDissolution;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_JUSTICE_GOV_UK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FIRST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.LAST_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.JUDICIAL_SEPARATION_PROCEEDINGS;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.MARRIED_TO_MORE_THAN_ONE_PERSON;
import static uk.gov.hmcts.divorce.document.content.NoticeOfProceedingJointJudicialSeparationContent.REISSUED_DATE;
import static uk.gov.hmcts.divorce.notification.CommonContent.ADDRESS;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingJointJudicialSeparationContentTest {

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private NoticeOfProceedingJointJudicialSeparationContent nopJointJudicialSeparationContent;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(nopJointJudicialSeparationContent, "phoneNumber", "0300 303 0642");
    }

    @Test
    public void shouldSuccessfullyApplyJSDivorceContent() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .postTown("town")
                .postCode("postcode")
                .build()
        );
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.setIsJudicialSeparation(YES);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DIVORCE);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        var ctscContactDetails = CtscContactDetails
            .builder()
            .phoneNumber("0300 303 0642")
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> templateContent = nopJointJudicialSeparationContent.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2());

        assertThat(templateContent)
            .contains(
                entry(CASE_REFERENCE, formatId(1616591401473378L)),
                entry(FIRST_NAME, TEST_FIRST_NAME),
                entry(LAST_NAME, TEST_LAST_NAME),
                entry(ISSUE_DATE, "18 June 2021"),
                entry(ADDRESS, "line1\nline2\ntown\npostcode"),
                entry(RELATION, "wife"),
                entry(JUDICIAL_SEPARATION_PROCEEDINGS, "judicial separation proceedings"),
                entry(JUDICIAL_SEPARATION, "judicial separation"),
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK),
                entry(CTSC_CONTACT_DETAILS, ctscContactDetails),
                entry(MARRIED_TO_MORE_THAN_ONE_PERSON, "You must tell the court if you’ve been married to more than one" +
                    " person during this marriage.")
            );

    }

    @Test
    public void shouldSuccessfullyApplyJSDissolutionContent() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .postTown("town")
                .postCode("postcode")
                .build()
        );
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.setIsJudicialSeparation(YES);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        var ctscContactDetails = CtscContactDetails
            .builder()
            .phoneNumber("0300 303 0642")
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> templateContent = nopJointJudicialSeparationContent.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2());

        assertThat(templateContent)
            .contains(
                entry(CASE_REFERENCE, formatId(1616591401473378L)),
                entry(FIRST_NAME, TEST_FIRST_NAME),
                entry(LAST_NAME, TEST_LAST_NAME),
                entry(ISSUE_DATE, "18 June 2021"),
                entry(ADDRESS, "line1\nline2\ntown\npostcode"),
                entry(RELATION, "wife"),
                entry(JUDICIAL_SEPARATION_PROCEEDINGS, "separation proceedings"),
                entry(JUDICIAL_SEPARATION, "separation"),
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK),
                entry(CTSC_CONTACT_DETAILS, ctscContactDetails)
            );

    }

    @Test
    public void shouldSuccessfullyApplyJSDissolutionReissueContent() {
        CaseData caseData = caseData();
        caseData.getApplicant1().setFirstName(TEST_FIRST_NAME);
        caseData.getApplicant1().setLastName(TEST_LAST_NAME);
        caseData.getApplicant1().setGender(MALE);
        caseData.getApplicant1().setAddress(
            AddressGlobalUK
                .builder()
                .addressLine1("line1")
                .addressLine2("line2")
                .country("UK")
                .postTown("town")
                .postCode("postcode")
                .build()
        );
        caseData.getApplicant2().setLanguagePreferenceWelsh(NO);
        caseData.setIsJudicialSeparation(YES);
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DivorceOrDissolution.DISSOLUTION);
        caseData.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        caseData.getApplication().setReissueDate(LocalDate.of(2021, 8, 18));
        var ctscContactDetails = CtscContactDetails
            .builder()
            .phoneNumber("0300 303 0642")
            .build();

        when(commonContent.getPartner(caseData, caseData.getApplicant2())).thenReturn("wife");

        Map<String, Object> templateContent = nopJointJudicialSeparationContent.apply(
            caseData,
            TEST_CASE_ID,
            caseData.getApplicant1(),
            caseData.getApplicant2());

        assertThat(templateContent)
            .contains(
                entry(CASE_REFERENCE, formatId(1616591401473378L)),
                entry(FIRST_NAME, TEST_FIRST_NAME),
                entry(LAST_NAME, TEST_LAST_NAME),
                entry(ISSUE_DATE, "18 June 2021"),
                entry(REISSUED_DATE, "Reissued on: 18 August 2021"),
                entry(ADDRESS, "line1\nline2\ntown\npostcode"),
                entry(RELATION, "wife"),
                entry(JUDICIAL_SEPARATION_PROCEEDINGS, "separation proceedings"),
                entry(JUDICIAL_SEPARATION, "separation"),
                entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                entry(DIVORCE_OR_CIVIL_PARTNERSHIP_EMAIL, CONTACT_DIVORCE_JUSTICE_GOV_UK),
                entry(CTSC_CONTACT_DETAILS, ctscContactDetails)
            );

    }

}
