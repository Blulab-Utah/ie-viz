package edu.utah.blulab.db.models;

/**
 * Created by Deep on 12/22/2016.
 */
public class FileContentsDao {

    private String inputContent;

    private String corpusName;

    private String fileName;

    public String getInputContent() {
        return inputContent;
    }

    public void setInputContent(String inputContent) {
        this.inputContent = inputContent;
    }

    public String getCorpusName() {
        return corpusName;
    }

    public void setCorpusName(String corpusName) {
        this.corpusName = corpusName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
