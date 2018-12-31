package edu.utah.blulab.models;

import java.io.Serializable;

public class CorpusModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String corpus;

    public String getCorpus() {
        return corpus;
    }

    public void setCorpus(String corpus) {
        this.corpus = corpus;
    }
}
