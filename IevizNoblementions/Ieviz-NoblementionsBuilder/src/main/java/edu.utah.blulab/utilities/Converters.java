package edu.utah.blulab.utilities;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Converters {
    public static String tsvToJson(String tsv) {
        if (DataUtils.checkDataType(tsv) != DataType.TSV) {
            throw new InputMismatchException(
                    "Input String is not in TSV format");
        }
        ArrayOfJson array = buildJSONFromString(tsv, "\t");
        return array.toString();
    }

    public static String csvToJson(String csv) {
        if (DataUtils.checkDataType(csv) != DataType.CSV) {
            throw new InputMismatchException(
                    "Input String is not in CSV format");
        }
        ArrayOfJson array = buildJSONFromString(csv, ",");
        return array.toString();
    }
    public static String jsonToCsv(String json) {
        if (DataUtils.checkDataType(json) != DataType.JSON) {
            throw new InputMismatchException(
                    "Input String is not in JSON format");
        }
        return buildXSPFromJSON(json, ",");
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

    private static String buildXSPFromJSON(String json, String separator) {

        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(json);

        StringBuilder output = new StringBuilder();

        Set<String> properties = new TreeSet<String>();
        while (matcher.find()) {
            String[] pairs = matcher.group(1).split(",");
            for (String pair : pairs) {
                properties.add(pair.split(":")[0].replaceAll("\"", ""));
            }
        }

        Iterator<String> it = properties.iterator();
        while (it.hasNext()) {
            output.append(it.next()).append(separator);
        }
        output.setLength(output.length() - 1);
        output.append("\n");

        matcher = pattern.matcher(json);
        while (matcher.find()) {
            String[] pairs = matcher.group(1).split(",");
            Map<String, String> map = new HashMap<String, String>();
            for (String pair : pairs) {
                String[] pairArray = pair.split(":");
                map.put(pairArray[0].replace("\"", ""), pairArray[1]);
            }
            it = properties.iterator();
            while (it.hasNext()) {
                output.append(map.get(it.next())).append(separator);
            }
            output.setLength(output.length() - 1);
            output.append("\n");
        }
        output.setLength(output.length() - 1);

        return output.toString();
    }
}
