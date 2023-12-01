package uk.gov.hmcts.divorce.document.print.documentpack;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_OFFLINE_RESPONDENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JS_SOLICITOR_COVER_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_APPLICANT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.COVERSHEET_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_APPLICATION_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_A1_SOLE_APP1_CIT_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AL2_SOLE_APP1_CIT_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1APP2_SOL_JS_JOINT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_JS_SOLE_OS_PS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_APP1_SOLICITOR_JS_SOLE;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_AS2_SOLE_APP1_SOL_SS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_APP_2_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.COVERSHEET;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.NOTICE_OF_PROCEEDINGS_APP_2;

@Component
@RequiredArgsConstructor
@Slf4j
public class Applicant1NotificeOfProceedingDocumentPack implements DocumentPack {

    private static String LETTER_TYPE_PACK = "applicant-aos-pack";

    private static String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final String LETTER_TYPE_APPLICANT_PACK = "applicant-aos-pack";
    private static final String LETTER_TYPE_AOS_RESPONSE_PACK = "aos-response-pack";

    private static final DocumentPackInfo APPLICANT_1_AOS_APPLICATION_PACK = new DocumentPackInfo(
            ImmutableMap.of(
                    NOTICE_OF_PROCEEDINGS_APP_1, Optional.of(NFD_NOP_AL2_SOLE_APP1_CIT_PS),
                    APPLICATION, Optional.of(DIVORCE_APPLICATION_SOLE)
            ),
            ImmutableMap.of(
                    NFD_NOP_AL2_SOLE_APP1_CIT_PS, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                    DIVORCE_APPLICATION_SOLE, DIVORCE_APPLICATION_DOCUMENT_NAME
            )
    );

    private static final DocumentPackInfo APPLICANT_1_AOS_AND_RESPONDENT_AOS_PACK = new DocumentPackInfo(
            ImmutableMap.of(
                    NOTICE_OF_PROCEEDINGS_APP_1, Optional.of(NFD_NOP_AL2_SOLE_APP1_CIT_PS),
                    APPLICATION, Optional.of(DIVORCE_APPLICATION_SOLE),
                    COVERSHEET, Optional.of(COVERSHEET_APPLICANT),
                    NOTICE_OF_PROCEEDINGS_APP_2, Optional.of(NFD_NOP_AL2_SOLE_APP1_CIT_PS),
                    APPLICATION, Optional.of(DIVORCE_APPLICATION_SOLE)
            ),
            ImmutableMap.of(
                    NFD_NOP_AL2_SOLE_APP1_CIT_PS, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                    DIVORCE_APPLICATION_SOLE, DIVORCE_APPLICATION_DOCUMENT_NAME,
                    COVERSHEET_APPLICANT, COVERSHEET_DOCUMENT_NAME,
                    NFD_NOP_AL2_SOLE_APP1_CIT_PS, NOTICE_OF_PROCEEDINGS_DOCUMENT_NAME,
                    DIVORCE_APPLICATION_SOLE, DIVORCE_APPLICATION_DOCUMENT_NAME
                    )
    );


    @Override
    public DocumentPackInfo getDocumentPack(final CaseData caseData, final Applicant applicant) {

        final boolean isApplicant1 = caseData.getApplicant1().equals(applicant);
        var isCourtService = caseData.getApplication().isCourtServiceMethod();

        final boolean isSoleApplication = caseData.getApplicationType().isSole();

        var templateId = getTemplateId(caseData);

        APPLICANT_1_AOS_APPLICATION_PACK.documentPack().putIfAbsent()
    }

    private String  getTemplateId(CaseData caseData) {

        final boolean isSoleApplication = caseData.getApplicationType().isSole();

        Long caseId = Long.parseLong(caseData.getHyphenatedCaseRef().replace("-", ""));

        var templateId = "";

        if (isSoleApplication) {
            if (caseData.isJudicialSeparationCase()) {
                templateId = generateSoleNoticeOfProceedingsForJudicialSeparation(caseData, caseId);
            } else {
                templateId = generateSoleNoticeOfProceedings(caseData, caseId);
            }
        } else {
            if (caseData.isJudicialSeparationCase()) {
                templateId =  generateJointJSNoticeOfProceedings(caseData, caseId);
            } else {
                templateId = generateJointNoticeOfProceedings(caseData, caseId);
            }
        }

        return templateId;
    }
    @Override
    public String getLetterId() {
        return LETTER_TYPE_PACK;
    }

    private String generateSoleNoticeOfProceedings(CaseData caseData, Long caseId) {
        final Applicant applicant1 = caseData.getApplicant1();

        if (applicant1.isRepresented()) {
            log.info("Generating notice of proceedings for applicant solicitor for case id {} ", caseId);

            return caseData.getApplication().isCourtServiceMethod()
                    ? NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS
                    : NFD_NOP_AS2_SOLE_APP1_SOL_SS;
        } else {
            log.info("Generating notice of proceedings for applicant for sole case id {} ", caseId);

            return caseData.getApplication().isCourtServiceMethod()
                    ? NFD_NOP_A1_SOLE_APP1_CIT_CS
                    : NFD_NOP_AL2_SOLE_APP1_CIT_PS;
        }
    }

    private String generateJointNoticeOfProceedings(CaseData caseData, Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            log.info("Generating solicitor applicant 1 notice of proceedings for joint case id {} ", caseId);

           return NFD_NOP_AS1_SOLEJOINT_APP1APP2_SOL_CS;
        } else {
            log.info("Generating applicant 1 notice of proceedings for joint case id {} ", caseId);

            return NFD_NOP_JA1_JOINT_APP1APP2_CIT;
        }
    }

    private String generateJointJSNoticeOfProceedings(CaseData caseData, Long caseId) {

        if (caseData.getApplicant1().isRepresented()) {
            log.info("setting applicant 1 solicitor notice of proceedings content for joint Judicial Separation case id {} ", caseId);

            return NFD_NOP_APP1APP2_SOL_JS_JOINT;

        } else {
            log.info("Setting applicant 1 notice of proceedings content for joint Judicial Separation case id {} ", caseId);

            return NFD_NOP_JA1_JOINT_APP1APP2_CIT_JS;
        }
    }

    private String generateSoleNoticeOfProceedingsForJudicialSeparation(CaseData caseData, Long caseId) {

        final Applicant applicant1 = caseData.getApplicant1();
        var isCourtService = caseData.getApplication().isCourtServiceMethod();

        if (applicant1.isRepresented()) {
            log.info("Generating notice of judicial separation proceedings for applicant solicitor for case id {} ", caseId);

            return isCourtService
                    ? NFD_NOP_APP1_SOLICITOR_JS_SOLE
                    : NFD_NOP_JS_SERVICE_SOLICITOR_TEMPLATE_ID;
        } else {
            log.info("Generating notice of judicial separation proceedings for applicant for case id {} ", caseId);

            return isCourtService ? NFD_NOP_APP1_JS_SOLE : NFD_NOP_APP1_JS_SOLE_OS_PS;
        }
    }
}
