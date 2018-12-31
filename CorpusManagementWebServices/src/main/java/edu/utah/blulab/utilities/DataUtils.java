package edu.utah.blulab.utilities;

public class DataUtils {

    public static DataType checkDataType(String input) {

        try {
            String firstLine = input.substring(0, input.indexOf("\n"));
            if (firstLine.contains(",") && !firstLine.contains("{")) {
                return DataType.CSV;
            }
            if (firstLine.contains("\t") && !firstLine.contains("{")) {
                return DataType.TSV;
            }

        } catch (IndexOutOfBoundsException e) {
            if (input.contains("{") && input.contains("}")) {
                return DataType.JSON;
            }
        }
        if (input.contains("{") && input.contains("}")) {
            return DataType.JSON;
        }
        return null;

    }
}