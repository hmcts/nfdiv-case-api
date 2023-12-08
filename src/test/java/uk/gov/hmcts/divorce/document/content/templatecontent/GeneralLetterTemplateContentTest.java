package uk.gov.hmcts.divorce.document.content.templatecontent;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.document.content.DocmosisCommonContent;
import uk.gov.hmcts.divorce.notification.CommonContent;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.util.AddressUtil.getPostalAddress;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CIVIL_PARTNER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.FEEDBACK;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.HUSBAND_OR_WIFE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.IS_JOINT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.NAME_FORMAT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_ADDRESS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RECIPIENT_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.RELATION;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class GeneralLetterTemplateContentTest {

    private static final String TEST_NAME = "Test name";
    @Mock
    private Clock clock;

    @Mock
    private CommonContent commonContent;

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @InjectMocks
    private GeneralLetterTemplateContent generalLetterTemplateContent;

    @Test
    void shouldProvideCorrectTemplateContentForGeneralLetterForApplicant() {
        CaseData caseData = validApplicant1CaseData();

        setMockClock(clock);

        var generalLetter = GeneralLetter
                .builder()
                .generalLetterParties(GeneralParties.APPLICANT)
                .generalLetterDetails(TEST_NAME).build();

        caseData.setGeneralLetter(generalLetter);

        final var templateContent = generalLetterTemplateContent.getTemplateContent(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1().getLanguagePreference());

        assertThat(templateContent).containsAllEntriesOf(
                Map.of(
                        FEEDBACK, generalLetter.getGeneralLetterDetails(),
                        ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER),
                        CASE_REFERENCE, formatId(TEST_CASE_ID),
                        IS_JOINT, !caseData.getApplicationType().isSole(),
                        RECIPIENT_NAME, getRecipientName(caseData.getApplicant1())
                )
        );
    }

    @Test
    void shouldProvideCorrectTemplateContentForGeneralLetterForRespondent() {
        CaseData caseData = validApplicant1CaseData();

        setMockClock(clock);

        var generalLetter = GeneralLetter
                .builder()
                .generalLetterParties(GeneralParties.RESPONDENT)
                .generalLetterDetails(TEST_NAME).build();

        caseData.setGeneralLetter(generalLetter);

        final var templateContent = generalLetterTemplateContent.getTemplateContent(
                caseData,
                TEST_CASE_ID,
                caseData.getApplicant1().getLanguagePreference());

        assertThat(templateContent).containsAllEntriesOf(
                Map.of(
                        FEEDBACK, generalLetter.getGeneralLetterDetails(),
                        ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER),
                        CASE_REFERENCE, formatId(TEST_CASE_ID),
                        IS_JOINT, !caseData.getApplicationType().isSole(),
                        RECIPIENT_NAME, getRecipientName(caseData.getApplicant1())
                )
        );
    }

    @Test
    void shouldProvideCorrectTemplateContentForGeneralLetterForOther() {
        CaseData caseData = validApplicant1CaseData();

        setMockClock(clock);

        var generalLetter = GeneralLetter
                .builder()
                .generalLetterParties(GeneralParties.OTHER)
                .generalLetterDetails(TEST_NAME)
                .otherRecipientName(TEST_NAME)
                .otherRecipientAddress(AddressGlobalUK.builder().build())
                .build();

        caseData.setGeneralLetter(generalLetter);

        final var templateContent = generalLetterTemplateContent.getTemplateContent(
                caseData,
                TEST_CASE_ID,
                null);

        assertThat(templateContent).containsAllEntriesOf(
                Map.of(
                        FEEDBACK, generalLetter.getGeneralLetterDetails(),
                        ISSUE_DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER),
                        CASE_REFERENCE, formatId(TEST_CASE_ID),
                        IS_JOINT, !caseData.getApplicationType().isSole(),
                        RELATION, caseData.isDivorce() ? HUSBAND_OR_WIFE : CIVIL_PARTNER,
                        RECIPIENT_ADDRESS, getPostalAddress(generalLetter.getOtherRecipientAddress()),
                        RECIPIENT_NAME, TEST_NAME
                )
        );
    }

    private String getRecipientName(final Applicant applicant) {
        return String.format(NAME_FORMAT, applicant.getFirstName(), applicant.getLastName());
    }
}
