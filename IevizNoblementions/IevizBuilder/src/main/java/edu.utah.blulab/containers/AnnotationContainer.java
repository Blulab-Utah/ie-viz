package edu.utah.blulab.containers;

import java.util.List;

public class AnnotationContainer {

    private List<FeatureContainer> features;
    private String doc;
    private int startLoc;
    private int endLoc;
    private String mentionFeatures;
    private String variable;

    public List<FeatureContainer> getFeatures() {
        return features;
    }

    public void setFeatures(List<FeatureContainer> features) {
        this.features = features;
    }

    public String getDoc() {
        return doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public int getStartLoc() {
        return startLoc;
    }

    public void setStartLoc(int startLoc) {
        this.startLoc = startLoc;
    }

    public int getEndLoc() {
        return endLoc;
    }

    public void setEndLoc(int endLoc) {
        this.endLoc = endLoc;
    }

    public String getMentionFeatures() {
        return mentionFeatures;
    }

    public void setMentionFeatures(String mentionFeatures) {
        this.mentionFeatures = mentionFeatures;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (FeatureContainer fcTemp : features) {
            sb.append(fcTemp.toString()+ "\n");
        }
        return "AnnotationContainer{\n" +
                "doc='" + doc + '\'' +
                ", startLoc=" + startLoc +
                ", endLoc=" + endLoc +
                ", mentionFeatures='" + mentionFeatures + '\'' +
                ", variable='" + variable + '\'' + "\n" +
                "features=" + sb.toString() +
                '}';
    }
}
