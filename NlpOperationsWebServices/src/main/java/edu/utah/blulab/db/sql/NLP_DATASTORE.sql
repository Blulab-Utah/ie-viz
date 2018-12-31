CREATE TABLE DocumentIdentifier (
  ID       INT          NOT NULL IDENTITY (1, 1),
  Doc_Name VARCHAR(255) NOT NULL,
  PRIMARY KEY (ID)
);
GO


CREATE TABLE AnnotationResults (
  Document_ID         INT          NOT NULL,
  Document_Type       VARCHAR(255) NULL,
  Id                  VARCHAR(255) NULL,
  Annotation_Variable VARCHAR(255) NULL,
  Property            VARCHAR(255) NULL,
  Document_Value      VARCHAR(255) NULL,
  Value_Properties    VARCHAR(255) NULL,
  Annotations         VARCHAR(255) NULL,
  PRIMARY KEY (Document_ID)
);

-- DELETE
-- FROM sys.foreign_keys
-- WHERE referenced_object_id = object_id('dbo.DocumentIdentifier')
--
-- GO

CREATE TABLE Users (
  ID              INT          NOT NULL,
  username        VARCHAR(255) NOT NULL,
  organization_id INT          NOT NULL,
  login_time      DATETIME,
  logout_time     DATETIME,
  PRIMARY KEY (ID)
);
GO

CREATE TABLE Organizations (
  ID                INT          NOT NULL,
  organization_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (ID)
);
GO


SET IDENTITY_INSERT dbo.DocumentIdentifier ON;


CREATE TABLE WARTHOG_NLP.NLP_RESULT (
            RESULT_ID NUMBER(22,0),
            RUN_ID NUMBER(22,0),
            NLP_INPUT_ID NUMBER(22,0),
            RPT_ID VARCHAR2(50),
            DOC_SRC VARCHAR2(50),
            NLP_PIPELINE_ID NUMBER(22,0),
            "RESULT" VARCHAR2(255),
            RESULT_DETAIL VARCHAR2(255),
            RESULT_FEATURES VARCHAR2(500),
            RESULT_DTM DATE DEFAULT SYSDATE,
            RESULT_TYPE_ID NUMBER(22,0)
) ;

# Snippet level results:
CREATE TABLE WARTHOG_NLP.NLP_RESULT_SNIPPET (
            SNIPPET_ID NUMBER,
            RESULT_ID NUMBER,
            SNIPPET_1 VARCHAR2(2000),
            SNIPPET_2 VARCHAR2(2000),
            TERM_SEARCHED VARCHAR2(500),
            MENTION_TYPE VARCHAR2(200),
            TERM_SNIPPET_1_START_LOC NUMBER,
            TERM_SNIPPET_1_END_LOC NUMBER,
            TERM_SNIPPET_2_START_LOC NUMBER,
            TERM_SNIPPET_2_END_LOC NUMBER,
            TERM_START_LOC_DOCUMENT NUMBER,
            TERM_END_LOC_DOCUMENT NUMBER,
            MENTION_FEATURES VARCHAR2(800)
) ;

# Visit level results:
CREATE TABLE WARTHOG_NLP.NLP_RESULT_VISIT (
            VISIT_RESULT_ID NUMBER(22,0),
            RUN_ID NUMBER(22,0),
            NLP_INPUT_ID NUMBER(22,0),
            VISIT_NO VARCHAR2(40),
            NLP_PIPELINE_ID NUMBER(22,0),
            RESULT_TYPE_ID NUMBER(22,0),
            "RESULT" VARCHAR2(255),
            RESULT_DETAIL VARCHAR2(255),
            RESULT_FEATURES VARCHAR2(500),
            RESULT_DTM DATE
) ;

# Feature values (optional, just for searching on feature-value purposes):
CREATE TABLE WARTHOG_NLP.NLP_RESULT_FEATURES (
            RESULT_FEATURE_ID NUMBER(22,0),
            FEATURE_ID NUMBER(22,0),
            RESULT_ID NUMBER(22,0),
            FEATURE_NAME VARCHAR2(255),
            FEATURE_VALUE VARCHAR2(1000)
) ;


# Configuration tables (not necessary for IE-Vize, but might be useful to consider):

CREATE TABLE WARTHOG_NLP.NLP_PIPELINE_DEF (
            PIPELINE_ID NUMBER,
            PIPELINE_NAME VARCHAR2(255),
            PIPELINE_DESCRIPTION VARCHAR2(2000),
            PIPELINE_STATUS VARCHAR2(50),
            ENTERED_DTM DATE
) ;

CREATE TABLE WARTHOG_NLP.NLP_RESULT_TYPE_DEF (
            RESULT_TYPE_ID NUMBER,
            RESULT_TYPE_NAME VARCHAR2(100),
            RESULT_TYPE_DESCRIPTION VARCHAR2(2000),
            UPDATED_DTM DATE,
            PIPELINE_ID NUMBER,
            RESULT_VALUE_SET VARCHAR2(500)
) ;


CREATE TABLE WARTHOG_NLP.NLP_RULES_DEF (
            RULE_SET_ID NUMBER(22,0),
            PIPELINE_ID NUMBER(22,0),
            COMPONENT_NAME VARCHAR2(200),
            RULES CLOB,
            COMMENTS VARCHAR2(300),
            CREATE_DTM DATE
) ;

 CREATE TABLE "WARTHOG_NLP"."NLP_FEATURE_DEF"
   (        "FEATURE_ID" NUMBER(22,0),
            "FEATURE_NAME" VARCHAR2(100 BYTE),
            "FEATURE_DESCRIPTION" VARCHAR2(2000 BYTE),
            "UPDATED_DTM" DATE
   )

# Execution related tables:

  CREATE TABLE "WARTHOG_NLP"."NLP_LOG"
   (        "RUN_ID" NUMBER,
            "PIPELINE_ID" NUMBER,
            "START_DTM" DATE,
            "END_DTM" DATE,
            "NUM_NOTES" NUMBER,
            "NUM_VISITS" NUMBER,
            "NUM_POSITIVE" NUMBER,
            "COMMENTS" VARCHAR2(500 BYTE)
   )

# CREATE TABLE WARTHOG_NLP.NLP_DEVELOP_LOG (
            LOG_ID NUMBER,
            PIPELINE_ID NUMBER,
            PIPELINE_NAME VARCHAR2(255),
            TYPE_OF_CHANGE VARCHAR2(255),
            DESCRIPTION CLOB,
            CHANGE_DTM DATE DEFAULT SYSDATE

) ;

# Input configuration table and views (just for reference):
CREATE TABLE WARTHOG_NLP.NLP_INPUT (
            NLP_INPUT_ID NUMBER,
            VISIT_NO NUMBER,
            PIPELINE_ID NUMBER,
            LAST_PROCESSED_DTM DATE,
            INSERT_DT DATE,
            STOP_PROCESSING_DTM DATE,
            FINAL_STATUS VARCHAR2(50),
            INSERT_DTM DATE,
            WARTHOG_COHORT_ID NUMBER
) ;

CREATE OR REPLACE FORCE EDITIONABLE VIEW "WARTHOG_NLP"."NLP_INPUT_DOCUMENTS_VW" ("NLP_INPUT_ID", "RPT_ID", "MSH_DT", "SOURCE", "FILLER_NO", "FILLER_APP_ID", "ADDENDA_NO", "RPT_TYPE_CD", "PROC_DT", "DICT_DT", "AUTH_DT", "STORE_DT", "PAT_ID", "VISIT_NO", "DICT_ID", "DICT_ID_SOURCE", "PROV_ID", "PROV_ID_SOURCE", "AUTH_ID", "AUTH_ID_SOURCE", "TRANS_ID", "STATUS", "EXT_IMAGE_REF", "TEXT", "Q_MSG_ID", "ERROR_CODE", "PROV_TYPE", "SPECIALTY_1", "ADM_DTM") AS
  SELECT Z.NLP_INPUT_ID,
          A.RPT_ID,
          A.MSH_DT,
          A.SOURCE,
          A.FILLER_NO,
          A.FILLER_APP_ID,
          A.ADDENDA_NO,
          A.RPT_TYPE_CD,
          A.PROC_DT,
          A.DICT_DT,
          A.AUTH_DT,
          A.STORE_DT,
          a.PAT_ID,
          TO_CHAR (A.VISIT_NO) AS visit_no,
          A.DICT_ID,
          A.DICT_ID_SOURCE,
          A.PROV_ID,
          A.PROV_ID_SOURCE,
          A.AUTH_ID,
          A.AUTH_ID_SOURCE,
          A.TRANS_ID,
          A.STATUS,
          A.EXT_IMAGE_REF,
          A.TEXT,
          A.Q_MSG_ID,
          A.ERROR_CODE,
          coalesce (r.PROV_TYPE,c.prov_type) as prov_type,
          C.SPECIALTY_1,
          F.ADM_DTM
     FROM CLINDATA.GENERAL_TEXT_CLOBS A
          JOIN WARTHOG_NLP.NLP_INPUT Z
             ON     A.VISIT_NO = Z.VISIT_NO
                AND Z.STOP_PROCESSING_DTM IS NULL
                AND z.pipeline_id not in (3,2)
          JOIN WARTHOG_NLP.NLP_PIPELINE_DEF Q
             ON     Z.PIPELINE_ID = Q.PIPELINE_ID
                AND Q.PIPELINE_STATUS = 'ACTIVE'
          JOIN VISIT_DM.VISIT F ON F.VISIT_NO = Z.VISIT_NO
          LEFT OUTER JOIN VOCAB.PROVIDER_MASTER_MV C
             ON LPAD (A.PROV_ID, 8, '0') = LPAD (C.PROV_ID, 8, '0')
          left outer join PROVIDER_DM.PROVIDER_MASTER_VW r
           ON LPAD (A.PROV_ID, 8, '0') = LPAD (r.PROV_ID, 8, '0')
    WHERE     NOT EXISTS
                (SELECT 1
                    FROM u0065655.TCR_CRITERIA_VALUES E
                   WHERE     E.criteria_value_text = A.RPT_TYPE_CD
                            and e.CRITERIA_ID = 84928
                          )
                  AND EXISTS
                   (SELECT 1
                    FROM u0065655.TCR_CRITERIA_VALUES G
                   WHERE     G.criteria_value_text = C.PROV_TYPE
                             and G.CRITERIA_ID = 99766
                         )
           UNION ALL
   SELECT "NLP_INPUT_ID",
          "RPT_ID",
          "MSH_DT",
          "SOURCE",
          "FILLER_NO",
          "FILLER_APP_ID",
          "ADDENDA_NO",
          "RPT_TYPE_CD",
          "PROC_DT",
          "DICT_DT",
          "AUTH_DT",
          "STORE_DT",
          "PAT_ID",
          "VISIT_NO",
          "DICT_ID",
          "DICT_ID_SOURCE",
          "PROV_ID",
          "PROV_ID_SOURCE",
          "AUTH_ID",
          "AUTH_ID_SOURCE",
          "TRANS_ID",
          "STATUS",
          "EXT_IMAGE_REF",
          "TEXT",
          "Q_MSG_ID",
          "ERROR_CODE",
          "PROV_TYPE",
          "SPECIALTY_1",
          "ADM_DTM"
     FROM WARTHOG_NLP.NLP_INPUT_PE_DOCS
      UNION ALL
   SELECT "NLP_INPUT_ID",
          "RPT_ID",
          "MSH_DT",
          "SOURCE",
          "FILLER_NO",
          "FILLER_APP_ID",
          "ADDENDA_NO",
          "RPT_TYPE_CD",
          "PROC_DT",
          "DICT_DT",
          "AUTH_DT",
          "STORE_DT",
          "PAT_ID",
          "VISIT_NO",
          "DICT_ID",
          "DICT_ID_SOURCE",
          "PROV_ID",
          "PROV_ID_SOURCE",
          "AUTH_ID",
          "AUTH_ID_SOURCE",
          "TRANS_ID",
          "STATUS",
          "EXT_IMAGE_REF",
          "TEXT",
          "Q_MSG_ID",
          "ERROR_CODE",
          "PROV_TYPE",
          "SPECIALTY_1",
          "ADM_DTM"
     FROM WARTHOG_NLP.NLP_INPUT_DVT_DOCS;
