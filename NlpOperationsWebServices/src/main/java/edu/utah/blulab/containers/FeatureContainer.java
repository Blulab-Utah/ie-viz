package edu.utah.blulab.containers;

public class FeatureContainer {

    private String property;
    private String propertyValue;
    private String propertyValueNumeric;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getPropertyValueNumeric() {
        return propertyValueNumeric;
    }

    public void setPropertyValueNumeric(String propertyValueNumeric) {
        this.propertyValueNumeric = propertyValueNumeric;
    }

    @Override
    public String toString() {
        return "FeatureContainer{" +
                "property='" + property + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", propertyValueNumeric='" + propertyValueNumeric + '\'' +
                '}';
    }
}
