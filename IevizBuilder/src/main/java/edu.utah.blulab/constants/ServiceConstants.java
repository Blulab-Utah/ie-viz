package edu.utah.blulab.constants;

import java.util.HashSet;

/**
 * Created by Deep on 11/2/2015.
 */
public class ServiceConstants {

    public static final String EMPTY = "null";
    public static final String DATA_FIELD = "data";
    public static final String ERROR_FIELD = "error";
    public static final String STATUS_FIELD = "status";
    public static final String TOKEN = "token";
    public static final String USERNAME = "username";
    public static final HashSet<String> UMLS_SET = new HashSet<String>() {{
        add("icd10cm");
        add("snomed");
        add("rxnorm");
        add("icd10pcs");
    }};

}
