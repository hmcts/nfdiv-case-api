package uk.gov.hmcts.divorce.payment;

import java.util.Set;

public final class FeesAndPayConstants {

    public static final String CHANNEL = "channel";
    public static final String EVENT = "event";
    public static final String JURISDICTION_1 = "jurisdiction1";
    public static final String JURISDICTION_2 = "jurisdiction2";
    public static final String SERVICE = "service";
    public static final String KEYWORD = "keyword";
    public static final String DIVORCE_APPLICATION_FEE_CODE = "FEE0002";
    public static final String FINANCIAL_ORDER_ON_NOTICE_FEE_CODE = "FEE0229";
    public static final String GENERAL_APPLICATION_WITHOUT_NOTICE_FEE_CODE = "FEE0228";
    public static final String BAILIFF_SERVE_DOC_FEE_CODE = "FEE0392";
    public static final Set<String> SINGLE_USE_FEE_CODES = Set.of(DIVORCE_APPLICATION_FEE_CODE);

    private FeesAndPayConstants() {
    }
}
