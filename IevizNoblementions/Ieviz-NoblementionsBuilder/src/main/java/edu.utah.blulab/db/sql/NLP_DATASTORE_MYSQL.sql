CREATE TABLE DocumentIdentifier
(
  ID       INT          NOT NULL AUTO_INCREMENT,
  Doc_Name VARCHAR(255) NOT NULL,
  PRIMARY KEY (ID)
);

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


CREATE TABLE Organizations (
  ID                INT          NOT NULL,
  organization_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (ID)
);