package uk.gov.hmcts.divorce.document.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.CtscContactDetails;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent.LEGAL_ADVISOR_COMMENTS;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent.REASON_JURISDICTION_DETAILS;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent.REASON_MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent.REASON_MARRIAGE_CERT_TRANSLATION;
import static uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusedForClarificationContent.REASON_PREVIOUS_PROCEEDINGS_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_DIVORCE_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CONTACT_EMAIL;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.CTSC_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DATE;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.DIVORCE_AND_DISSOLUTION_HEADER_TEXT;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES;
import static uk.gov.hmcts.divorce.document.content.DocmosisTemplateConstants.PHONE_AND_OPENING_TIMES_TEXT;
import static uk.gov.hmcts.divorce.notification.FormatUtil.DATE_TIME_FORMATTER;
import static uk.gov.hmcts.divorce.notification.FormatUtil.formatId;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getBasicDocmosisTemplateContentWithCtscContactDetails;

@ExtendWith(MockitoExtension.class)
class ConditionalOrderRefusedForClarificationContentTest {

    @Mock
    private DocmosisCommonContent docmosisCommonContent;

    @Mock
    private ConditionalOrderCommonContent conditionalOrderCommonContent;

    @Mock
    private ConditionalOrderRefusedForAmendmentContent conditionalOrderRefusedForAmendmentContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private ConditionalOrderRefusedForClarificationContent conditionalOrderRefusedForClarificationContent;

    @BeforeEach
    public void setUp() {
        setMockClock(clock, LocalDate.of(2022, 3, 16));
    }

    @Test
    public void shouldReturnEnglishTemplateContentForEnglish() {

        CaseData caseData = buildCaseData();

        when(docmosisCommonContent.getBasicDocmosisTemplateContent(caseData.getApplicant1().getLanguagePreference()))
                .thenReturn(getBasicDocmosisTemplateContentWithCtscContactDetails(ENGLISH));

        Map<String, Object> templateContent = conditionalOrderRefusedForClarificationContent.apply(
                caseData,
                1616591401473378L);

        var ctscContactDetails = CtscContactDetails
                .builder()
                .centreName("HMCTS Digital Divorce and Dissolution")
                .serviceCentre("Courts and Tribunals Service Centre")
                .poBox("PO Box 13226")
                .town("Harlow")
                .postcode("CM20 9UG")
                .emailAddress("contactdivorce@justice.gov.uk")
                .phoneNumber("0300 303 0642")
                .build();

        assertThat(templateContent)
                .contains(
                    entry(CCD_CASE_REFERENCE, formatId(1616591401473378L)),
                    entry(APPLICANT_1_FULL_NAME, caseData.getApplicant1().getFullName()),
                    entry(APPLICANT_2_FULL_NAME, caseData.getApplicant2().getFullName()),
                    entry(DATE, LocalDate.now(clock).format(DATE_TIME_FORMATTER)),
                    entry(JUDICIAL_SEPARATION, false),
                    entry(REASON_JURISDICTION_DETAILS,false),
                    entry(REASON_MARRIAGE_CERT_TRANSLATION, false),
                    entry(REASON_MARRIAGE_CERTIFICATE,false),
                    entry(REASON_PREVIOUS_PROCEEDINGS_DETAILS, false),
                    entry(LEGAL_ADVISOR_COMMENTS, Collections.emptyList()),
                    entry(DIVORCE_AND_DISSOLUTION_HEADER, DIVORCE_AND_DISSOLUTION_HEADER_TEXT),
                    entry(COURTS_AND_TRIBUNALS_SERVICE_HEADER, COURTS_AND_TRIBUNALS_SERVICE_HEADER_TEXT),
                    entry(CONTACT_EMAIL, CONTACT_DIVORCE_EMAIL),
                    entry(PHONE_AND_OPENING_TIMES, PHONE_AND_OPENING_TIMES_TEXT),
                    entry(CTSC_CONTACT_DETAILS, ctscContactDetails)
            );
    }

    private CaseData buildCaseData() {
        CaseData caseData = caseData();
        caseData.setApplicationType(ApplicationType.SOLE_APPLICATION);
        caseData.setConditionalOrder(ConditionalOrder.builder()
                .refusalClarificationReason(new HashSet<>())
                .build());

        return caseData;
    }
}
