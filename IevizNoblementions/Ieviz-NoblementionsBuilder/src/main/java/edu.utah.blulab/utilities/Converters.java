package edu.utah.blulab.utilities;

import java.util.InputMismatchException;

public class Converters {
    public static String tsvToJson(String tsv) {
        if (DataUtils.checkDataType(tsv) != DataType.TSV) {
            throw new InputMismatchException(
                    "Input String is not in TSV format");
        }
        ArrayOfJson array = buildJSONFromString(tsv, "\t");
        return array.toString();
    }

    public static String tsvToCsv(String tsv) {
        if (DataUtils.checkDataType(tsv) != DataType.TSV) {
            throw new InputMismatchException(
                    "Input String is not in TSV format");
        }
        return tsv.replaceAll(",", " ").replaceAll("\t", ",");
    }

    private static ArrayOfJson buildJSONFromString(String text, String separator) {
        String[] lines = text.split("\n");

        String columns[] = lines[0].split(separator);

        ArrayOfJson array = new ArrayOfJson();
        for (int i = 1; i < lines.length; i++) {
            JsonObject json = new JsonObject();
            String values[] = lines[i].split(separator);

            for (int j = 0; j < values.length; j++) {
                JsonPair pair = new JsonPair(columns[j], values[j]);
                json.addPair(pair);
            }
            array.addObject(json);
        }
        return array;
    }
}
