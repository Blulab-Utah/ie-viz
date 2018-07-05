package edu.utah.blulab.db.models;

import javax.persistence.*;

@Entity
@Table(name = "NLP_RESULT_SNIPPET")
public class NlpResultSnippetEntity {
    private Integer snippetId;
    private Integer resultDocId;
    private String snippet1;
    private String termSearched;
    private String mentionType;
    private Integer termSnippet1StartLoc;
    private Integer termSnippet1EndLoc;
    private Integer termStartLocDocument;
    private Integer termEndLocDocument;
    private String mentionFeatures;

    @Id
    @Column(name = "SNIPPET_ID")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Integer getSnippetId() {
        return snippetId;
    }

    public void setSnippetId(Integer snippetId) {
        this.snippetId = snippetId;
    }

    @Basic
    @Column(name = "RESULT_DOC_ID")
    public Integer getResultDocId() {
        return resultDocId;
    }

    public void setResultDocId(Integer resultDocId) {
        this.resultDocId = resultDocId;
    }

    @Basic
    @Column(name = "SNIPPET_1")
    public String getSnippet1() {
        return snippet1;
    }

    public void setSnippet1(String snippet1) {
        this.snippet1 = snippet1;
    }

    @Basic
    @Column(name = "TERM_SEARCHED")
    public String getTermSearched() {
        return termSearched;
    }

    public void setTermSearched(String termSearched) {
        this.termSearched = termSearched;
    }

    @Basic
    @Column(name = "MENTION_TYPE")
    public String getMentionType() {
        return mentionType;
    }

    public void setMentionType(String mentionType) {
        this.mentionType = mentionType;
    }

    @Basic
    @Column(name = "TERM_SNIPPET_1_START_LOC")
    public Integer getTermSnippet1StartLoc() {
        return termSnippet1StartLoc;
    }

    public void setTermSnippet1StartLoc(Integer termSnippet1StartLoc) {
        this.termSnippet1StartLoc = termSnippet1StartLoc;
    }

    @Basic
    @Column(name = "TERM_SNIPPET_1_END_LOC")
    public Integer getTermSnippet1EndLoc() {
        return termSnippet1EndLoc;
    }

    public void setTermSnippet1EndLoc(Integer termSnippet1EndLoc) {
        this.termSnippet1EndLoc = termSnippet1EndLoc;
    }

    @Basic
    @Column(name = "TERM_START_LOC_DOCUMENT")
    public Integer getTermStartLocDocument() {
        return termStartLocDocument;
    }

    public void setTermStartLocDocument(Integer termStartLocDocument) {
        this.termStartLocDocument = termStartLocDocument;
    }

    @Basic
    @Column(name = "TERM_END_LOC_DOCUMENT")
    public Integer getTermEndLocDocument() {
        return termEndLocDocument;
    }

    public void setTermEndLocDocument(Integer termEndLocDocument) {
        this.termEndLocDocument = termEndLocDocument;
    }

    @Basic
    @Column(name = "MENTION_FEATURES")
    public String getMentionFeatures() {
        return mentionFeatures;
    }

    public void setMentionFeatures(String mentionFeatures) {
        this.mentionFeatures = mentionFeatures;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NlpResultSnippetEntity that = (NlpResultSnippetEntity) o;

        if (snippetId != null ? !snippetId.equals(that.snippetId) : that.snippetId != null) return false;
        if (resultDocId != null ? !resultDocId.equals(that.resultDocId) : that.resultDocId != null) return false;
        if (snippet1 != null ? !snippet1.equals(that.snippet1) : that.snippet1 != null) return false;
        if (termSearched != null ? !termSearched.equals(that.termSearched) : that.termSearched != null) return false;
        if (mentionType != null ? !mentionType.equals(that.mentionType) : that.mentionType != null) return false;
        if (termSnippet1StartLoc != null ? !termSnippet1StartLoc.equals(that.termSnippet1StartLoc) : that.termSnippet1StartLoc != null)
            return false;
        if (termSnippet1EndLoc != null ? !termSnippet1EndLoc.equals(that.termSnippet1EndLoc) : that.termSnippet1EndLoc != null)
            return false;
        if (termStartLocDocument != null ? !termStartLocDocument.equals(that.termStartLocDocument) : that.termStartLocDocument != null)
            return false;
        if (termEndLocDocument != null ? !termEndLocDocument.equals(that.termEndLocDocument) : that.termEndLocDocument != null)
            return false;
        if (mentionFeatures != null ? !mentionFeatures.equals(that.mentionFeatures) : that.mentionFeatures != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = snippetId != null ? snippetId.hashCode() : 0;
        result = 31 * result + (resultDocId != null ? resultDocId.hashCode() : 0);
        result = 31 * result + (snippet1 != null ? snippet1.hashCode() : 0);
        result = 31 * result + (termSearched != null ? termSearched.hashCode() : 0);
        result = 31 * result + (mentionType != null ? mentionType.hashCode() : 0);
        result = 31 * result + (termSnippet1StartLoc != null ? termSnippet1StartLoc.hashCode() : 0);
        result = 31 * result + (termSnippet1EndLoc != null ? termSnippet1EndLoc.hashCode() : 0);
        result = 31 * result + (termStartLocDocument != null ? termStartLocDocument.hashCode() : 0);
        result = 31 * result + (termEndLocDocument != null ? termEndLocDocument.hashCode() : 0);
        result = 31 * result + (mentionFeatures != null ? mentionFeatures.hashCode() : 0);
        return result;
    }
}
