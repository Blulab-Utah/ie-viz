package edu.utah.blulab.containers;

public class FeatureContainer {

    private String Property;
    private String PropertyValue;
    private String PropertyValueNumeric;

    public String getProperty() {
        return Property;
    }

    public void setProperty(String property) {
        Property = property;
    }

    public String getPropertyValue() {
        return PropertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        PropertyValue = propertyValue;
    }

    public String getPropertyValueNumeric() {
        return PropertyValueNumeric;
    }

    public void setPropertyValueNumeric(String propertyValueNumeric) {
        PropertyValueNumeric = propertyValueNumeric;
    }

    @Override
    public String toString() {
        return "FeatureContainer{" +
                "Property='" + Property + '\'' +
                ", PropertyValue='" + PropertyValue + '\'' +
                ", PropertyValueNumeric='" + PropertyValueNumeric + '\'' +
                '}';
    }
}
