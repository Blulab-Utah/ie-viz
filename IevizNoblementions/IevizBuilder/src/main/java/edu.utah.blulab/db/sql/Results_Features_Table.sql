CREATE TABLE NLP_RESULT_FEATURES (
	RESULT_FEATURE_ID MEDIUMINT NOT NULL AUTO_INCREMENT,
    SNIPPET_ID MEDIUMINT,
	RESULT_DOC_ID MEDIUMINT,
	FEATURE_NAME VARCHAR(255),
	FEATURE_VALUE VARCHAR(1000),
    FEATURE_VALUE_NUMERIC VARCHAR(1000),
    PRIMARY KEY (RESULT_FEATURE_ID),
    INDEX doc_ind (RESULT_DOC_ID),
	FOREIGN KEY (RESULT_DOC_ID)
        REFERENCES NLP_RESULT_DOC(RESULT_DOC_ID),
	FOREIGN KEY (SNIPPET_ID)
        REFERENCES NLP_RESULT_SNIPPET(SNIPPET_ID)
) ;
