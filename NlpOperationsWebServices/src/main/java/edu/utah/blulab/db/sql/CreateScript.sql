CREATE TABLE `Users` (
  `ID` int(11) NOT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `organization_id` int(11) NOT NULL,
  `login_time` datetime DEFAULT NULL,
  `logout_time` datetime DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `NLP_RUN_DEF` (
  `RUN_ID` mediumint(9) NOT NULL AUTO_INCREMENT,
  `USER_ID` int(11) DEFAULT NULL,
  `RUN_NAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `RUN_DESCRIPTION` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`RUN_ID`),
  KEY `USER_ID` (`USER_ID`),
  CONSTRAINT `NLP_RUN_DEF_ibfk_1` FOREIGN KEY (`USER_ID`) REFERENCES `Users` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `NLP_RESULT_DOC` (
  `RESULT_DOC_ID` mediumint(9) NOT NULL AUTO_INCREMENT,
  `RUN_ID` mediumint(9) DEFAULT NULL,
  `NLP_INPUT_ID` int(11) DEFAULT NULL,
  `RPT_ID` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DOC_OBJECT_ID` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `DOC_SRC` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `NLP_PIPELINE_ID` int(11) DEFAULT NULL,
  `RESULT` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `RESULT_DETAIL` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `RESULT_FEATURES` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `RESULT_DTM` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `RESULT_TYPE_ID` int(11) DEFAULT NULL,
  PRIMARY KEY (`RESULT_DOC_ID`),
  KEY `RUN_ID` (`RUN_ID`),
  CONSTRAINT `NLP_RESULT_DOC_ibfk_1` FOREIGN KEY (`RUN_ID`) REFERENCES `NLP_RUN_DEF` (`RUN_ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `NLP_RESULT_SNIPPET` (
  `SNIPPET_ID` mediumint(9) NOT NULL AUTO_INCREMENT,
  `RESULT_DOC_ID` mediumint(9) DEFAULT NULL,
  `SNIPPET_1` varchar(2000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TERM_SEARCHED` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `MENTION_TYPE` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `TERM_SNIPPET_1_START_LOC` int(11) DEFAULT NULL,
  `TERM_SNIPPET_1_END_LOC` int(11) DEFAULT NULL,
  `TERM_START_LOC_DOCUMENT` int(11) DEFAULT NULL,
  `TERM_END_LOC_DOCUMENT` int(11) DEFAULT NULL,
  `MULTISPAN_ANNOTATION` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `VARIABLE_URI` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `MENTION_FEATURES` varchar(800) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`SNIPPET_ID`),
  KEY `doc_ind` (`RESULT_DOC_ID`),
  CONSTRAINT `NLP_RESULT_SNIPPET_ibfk_1` FOREIGN KEY (`RESULT_DOC_ID`) REFERENCES `NLP_RESULT_DOC` (`RESULT_DOC_ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `NLP_RESULT_FEATURES` (
  `RESULT_FEATURE_ID` mediumint(9) NOT NULL AUTO_INCREMENT,
  `SNIPPET_ID` mediumint(9) DEFAULT NULL,
  `RESULT_DOC_ID` mediumint(9) DEFAULT NULL,
  `FEATURE_NAME` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FEATURE_VALUE` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `FEATURE_VALUE_NUMERIC` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`RESULT_FEATURE_ID`),
  KEY `doc_ind` (`RESULT_DOC_ID`),
  KEY `SNIPPET_ID` (`SNIPPET_ID`),
  CONSTRAINT `NLP_RESULT_FEATURES_ibfk_1` FOREIGN KEY (`RESULT_DOC_ID`) REFERENCES `NLP_RESULT_DOC` (`RESULT_DOC_ID`) ON DELETE CASCADE,
  CONSTRAINT `NLP_RESULT_FEATURES_ibfk_2` FOREIGN KEY (`SNIPPET_ID`) REFERENCES `NLP_RESULT_SNIPPET` (`SNIPPET_ID`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=281 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Organizations` (
  `ID` int(11) NOT NULL,
  `organization_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
