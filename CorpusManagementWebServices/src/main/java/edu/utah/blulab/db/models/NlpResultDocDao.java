package edu.utah.blulab.db.models;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "NLP_RESULT_DOC")
public class NlpResultDocDao {
    private Integer resultDocId;
    private Integer runId;
    private Integer nlpInputId;
    private String rptId;
    private String docSrc;
    private Integer nlpPipelineId;
    private String result;
    private String resultDetail;
    private String resultFeatures;
    private Timestamp resultDtm;
    private Integer resultTypeId;
    private String fileObjId;

    @Id
    @Column(name = "RESULT_DOC_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getResultDocId() {
        return resultDocId;
    }

    public void setResultDocId(Integer resultDocId) {
        this.resultDocId = resultDocId;
    }

    @Basic
    @Column(name = "RUN_ID")
    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }

    @Basic
    @Column(name = "NLP_INPUT_ID")
    public Integer getNlpInputId() {
        return nlpInputId;
    }

    public void setNlpInputId(Integer nlpInputId) {
        this.nlpInputId = nlpInputId;
    }

    @Basic
    @Column(name = "RPT_ID")
    public String getRptId() {
        return rptId;
    }

    public void setRptId(String rptId) {
        this.rptId = rptId;
    }

    @Basic
    @Column(name = "DOC_SRC")
    public String getDocSrc() {
        return docSrc;
    }

    public void setDocSrc(String docSrc) {
        this.docSrc = docSrc;
    }

    @Basic
    @Column(name = "NLP_PIPELINE_ID")
    public Integer getNlpPipelineId() {
        return nlpPipelineId;
    }

    public void setNlpPipelineId(Integer nlpPipelineId) {
        this.nlpPipelineId = nlpPipelineId;
    }

    @Basic
    @Column(name = "RESULT")
    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Basic
    @Column(name = "RESULT_DETAIL")
    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }

    @Basic
    @Column(name = "RESULT_FEATURES")
    public String getResultFeatures() {
        return resultFeatures;
    }

    public void setResultFeatures(String resultFeatures) {
        this.resultFeatures = resultFeatures;
    }

    @Basic
    @Column(name = "RESULT_DTM")
    public Timestamp getResultDtm() {
        return resultDtm;
    }

    public void setResultDtm(Timestamp resultDtm) {
        this.resultDtm = resultDtm;
    }

    @Basic
    @Column(name = "RESULT_TYPE_ID")
    public Integer getResultTypeId() {
        return resultTypeId;
    }

    public void setResultTypeId(Integer resultTypeId) {
        this.resultTypeId = resultTypeId;
    }

    @Basic
    @Column(name = "FILE_OBJ_ID")
    public String getFileObjId() {
        return fileObjId;
    }

    public void setFileObjId(String fileObjId) {
        this.fileObjId = fileObjId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NlpResultDocDao that = (NlpResultDocDao) o;

        if (resultDocId != null ? !resultDocId.equals(that.resultDocId) : that.resultDocId != null) return false;
        if (runId != null ? !runId.equals(that.runId) : that.runId != null) return false;
        if (nlpInputId != null ? !nlpInputId.equals(that.nlpInputId) : that.nlpInputId != null) return false;
        if (rptId != null ? !rptId.equals(that.rptId) : that.rptId != null) return false;
        if (docSrc != null ? !docSrc.equals(that.docSrc) : that.docSrc != null) return false;
        if (nlpPipelineId != null ? !nlpPipelineId.equals(that.nlpPipelineId) : that.nlpPipelineId != null)
            return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        if (resultDetail != null ? !resultDetail.equals(that.resultDetail) : that.resultDetail != null) return false;
        if (resultFeatures != null ? !resultFeatures.equals(that.resultFeatures) : that.resultFeatures != null)
            return false;
        if (resultDtm != null ? !resultDtm.equals(that.resultDtm) : that.resultDtm != null) return false;
        if (resultTypeId != null ? !resultTypeId.equals(that.resultTypeId) : that.resultTypeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = resultDocId != null ? resultDocId.hashCode() : 0;
        result1 = 31 * result1 + (runId != null ? runId.hashCode() : 0);
        result1 = 31 * result1 + (nlpInputId != null ? nlpInputId.hashCode() : 0);
        result1 = 31 * result1 + (rptId != null ? rptId.hashCode() : 0);
        result1 = 31 * result1 + (docSrc != null ? docSrc.hashCode() : 0);
        result1 = 31 * result1 + (nlpPipelineId != null ? nlpPipelineId.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (resultDetail != null ? resultDetail.hashCode() : 0);
        result1 = 31 * result1 + (resultFeatures != null ? resultFeatures.hashCode() : 0);
        result1 = 31 * result1 + (resultDtm != null ? resultDtm.hashCode() : 0);
        result1 = 31 * result1 + (resultTypeId != null ? resultTypeId.hashCode() : 0);
        return result1;
    }
}
