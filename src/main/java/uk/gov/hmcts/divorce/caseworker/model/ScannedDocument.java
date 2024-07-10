package uk.gov.hmcts.divorce.caseworker.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.deserializer.LocalDateTimeDeserializer;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.type.ScannedDocumentType;

public class ScannedDocument {
    @CCD(
            label = "Select document type",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "ScannedDocumentType"
    )
    private ScannedDocumentType type;
    @CCD(
            label = "Document subtype"
    )
    private String subtype;
    @CCD(
            label = "Scanned document url"
    )
    private Document url;
    @CCD(
            label = "Document control number"
    )
    private String controlNumber;
    @CCD(
            label = "File Name"
    )
    private String fileName;
    @CCD(
            label = "Scanned date"
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime scannedDate;
    @CCD(
            label = "Delivery date"
    )
    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    private LocalDateTime deliveryDate;
    @CCD(
            label = "Exception record reference"
    )
    private String exceptionRecordReference;

    @JsonCreator
    public ScannedDocument(@JsonProperty("type") ScannedDocumentType type, @JsonProperty("subtype") String subtype, @JsonProperty("url") Document url, @JsonProperty("controlNumber") String controlNumber, @JsonProperty("fileName") String fileName, @JsonProperty("scannedDate") LocalDateTime scannedDate, @JsonProperty("deliveryDate") LocalDateTime deliveryDate, @JsonProperty("exceptionRecordReference") String exceptionRecordReference) {
        this.type = type;
        this.subtype = subtype;
        this.url = url;
        this.controlNumber = controlNumber;
        this.fileName = fileName;
        this.scannedDate = scannedDate;
        this.deliveryDate = deliveryDate;
        this.exceptionRecordReference = exceptionRecordReference;
    }

    public static ScannedDocument.ScannedDocumentBuilder builder() {
        return new ScannedDocument.ScannedDocumentBuilder();
    }

    public ScannedDocumentType getType() {
        return this.type;
    }

    public String getSubtype() {
        return this.subtype;
    }

    public Document getUrl() {
        return this.url;
    }

    public String getControlNumber() {
        return this.controlNumber;
    }

    public String getFileName() {
        return this.fileName;
    }

    public LocalDateTime getScannedDate() {
        return this.scannedDate;
    }

    public LocalDateTime getDeliveryDate() {
        return this.deliveryDate;
    }

    public String getExceptionRecordReference() {
        return this.exceptionRecordReference;
    }

    public void setType(ScannedDocumentType type) {
        this.type = type;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public void setUrl(Document url) {
        this.url = url;
    }

    public void setControlNumber(String controlNumber) {
        this.controlNumber = controlNumber;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    public void setScannedDate(LocalDateTime scannedDate) {
        this.scannedDate = scannedDate;
    }

    @JsonDeserialize(
            using = LocalDateTimeDeserializer.class
    )
    @JsonFormat(
            pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
    )
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public void setExceptionRecordReference(String exceptionRecordReference) {
        this.exceptionRecordReference = exceptionRecordReference;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ScannedDocument)) {
            return false;
        } else {
            ScannedDocument other = (ScannedDocument) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                label107:
                {
                    Object this$type = this.getType();
                    Object other$type = other.getType();
                    if (this$type == null) {
                        if (other$type == null) {
                            break label107;
                        }
                    } else if (this$type.equals(other$type)) {
                        break label107;
                    }

                    return false;
                }

                Object this$subtype = this.getSubtype();
                Object other$subtype = other.getSubtype();
                if (this$subtype == null) {
                    if (other$subtype != null) {
                        return false;
                    }
                } else if (!this$subtype.equals(other$subtype)) {
                    return false;
                }

                Object this$url = this.getUrl();
                Object other$url = other.getUrl();
                if (this$url == null) {
                    if (other$url != null) {
                        return false;
                    }
                } else if (!this$url.equals(other$url)) {
                    return false;
                }

                label86:
                {
                    Object this$controlNumber = this.getControlNumber();
                    Object other$controlNumber = other.getControlNumber();
                    if (this$controlNumber == null) {
                        if (other$controlNumber == null) {
                            break label86;
                        }
                    } else if (this$controlNumber.equals(other$controlNumber)) {
                        break label86;
                    }

                    return false;
                }

                label79:
                {
                    Object this$fileName = this.getFileName();
                    Object other$fileName = other.getFileName();
                    if (this$fileName == null) {
                        if (other$fileName == null) {
                            break label79;
                        }
                    } else if (this$fileName.equals(other$fileName)) {
                        break label79;
                    }

                    return false;
                }

                label72:
                {
                    Object this$scannedDate = this.getScannedDate();
                    Object other$scannedDate = other.getScannedDate();
                    if (this$scannedDate == null) {
                        if (other$scannedDate == null) {
                            break label72;
                        }
                    } else if (this$scannedDate.equals(other$scannedDate)) {
                        break label72;
                    }

                    return false;
                }

                Object this$deliveryDate = this.getDeliveryDate();
                Object other$deliveryDate = other.getDeliveryDate();
                if (this$deliveryDate == null) {
                    if (other$deliveryDate != null) {
                        return false;
                    }
                } else if (!this$deliveryDate.equals(other$deliveryDate)) {
                    return false;
                }

                Object this$exceptionRecordReference = this.getExceptionRecordReference();
                Object other$exceptionRecordReference = other.getExceptionRecordReference();
                if (this$exceptionRecordReference == null) {
                    if (other$exceptionRecordReference != null) {
                        return false;
                    }
                } else if (!this$exceptionRecordReference.equals(other$exceptionRecordReference)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof uk.gov.hmcts.ccd.sdk.type.ScannedDocument;
    }

    public int hashCode() {
        int result = 1;
        Object $type = this.getType();
        result = result * 59 + ($type == null ? 43 : $type.hashCode());
        Object $subtype = this.getSubtype();
        result = result * 59 + ($subtype == null ? 43 : $subtype.hashCode());
        Object $url = this.getUrl();
        result = result * 59 + ($url == null ? 43 : $url.hashCode());
        Object $controlNumber = this.getControlNumber();
        result = result * 59 + ($controlNumber == null ? 43 : $controlNumber.hashCode());
        Object $fileName = this.getFileName();
        result = result * 59 + ($fileName == null ? 43 : $fileName.hashCode());
        Object $scannedDate = this.getScannedDate();
        result = result * 59 + ($scannedDate == null ? 43 : $scannedDate.hashCode());
        Object $deliveryDate = this.getDeliveryDate();
        result = result * 59 + ($deliveryDate == null ? 43 : $deliveryDate.hashCode());
        Object $exceptionRecordReference = this.getExceptionRecordReference();
        result = result * 59 + ($exceptionRecordReference == null ? 43 : $exceptionRecordReference.hashCode());
        return result;
    }

    public String toString() {
        ScannedDocumentType var10000 = this.getType();
        return "ScannedDocument(type=" + var10000 + ", subtype=" + this.getSubtype() + ", url=" + this.getUrl() + ", controlNumber=" + this.getControlNumber() + ", fileName=" + this.getFileName() + ", scannedDate=" + this.getScannedDate() + ", deliveryDate=" + this.getDeliveryDate() + ", exceptionRecordReference=" + this.getExceptionRecordReference() + ")";
    }

    public ScannedDocument() {
    }

    public static class ScannedDocumentBuilder {
        private ScannedDocumentType type;
        private String subtype;
        private Document url;
        private String controlNumber;
        private String fileName;
        private LocalDateTime scannedDate;
        private LocalDateTime deliveryDate;
        private String exceptionRecordReference;

        ScannedDocumentBuilder() {
        }

        public ScannedDocument.ScannedDocumentBuilder type(ScannedDocumentType type) {
            this.type = type;
            return this;
        }

        public ScannedDocument.ScannedDocumentBuilder subtype(String subtype) {
            this.subtype = subtype;
            return this;
        }

        public ScannedDocument.ScannedDocumentBuilder url(Document url) {
            this.url = url;
            return this;
        }

        public ScannedDocument.ScannedDocumentBuilder controlNumber(String controlNumber) {
            this.controlNumber = controlNumber;
            return this;
        }

        public ScannedDocument.ScannedDocumentBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        @JsonDeserialize(
                using = LocalDateTimeDeserializer.class
        )
        @JsonFormat(
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        )
        public ScannedDocument.ScannedDocumentBuilder scannedDate(LocalDateTime scannedDate) {
            this.scannedDate = scannedDate;
            return this;
        }

        @JsonDeserialize(
                using = LocalDateTimeDeserializer.class
        )
        @JsonFormat(
                pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS"
        )
        public ScannedDocument.ScannedDocumentBuilder deliveryDate(LocalDateTime deliveryDate) {
            this.deliveryDate = deliveryDate;
            return this;
        }

        public ScannedDocument.ScannedDocumentBuilder exceptionRecordReference(String exceptionRecordReference) {
            this.exceptionRecordReference = exceptionRecordReference;
            return this;
        }

        public ScannedDocument build() {
            return new ScannedDocument(this.type, this.subtype, this.url, this.controlNumber, this.fileName, this.scannedDate, this.deliveryDate, this.exceptionRecordReference);
        }

        public String toString() {
            return "ScannedDocument.ScannedDocumentBuilder(type=" + this.type + ", subtype=" + this.subtype + ", url=" + this.url + ", controlNumber=" + this.controlNumber + ", fileName=" + this.fileName + ", scannedDate=" + this.scannedDate + ", deliveryDate=" + this.deliveryDate + ", exceptionRecordReference=" + this.exceptionRecordReference + ")";
        }
    }
}
