package net.imglib2.labkit_rest_api.dvid.metadata;

import javax.json.bind.annotation.JsonbProperty;

public class Value {
    @JsonbProperty("DataType")
    private final String dataType;

    @JsonbProperty("Label")
    private final String labelType;

    public Value(String dataType, String labelType) {
        this.dataType = dataType;
        this.labelType = labelType;
    }

    public String getDataType() {
        return dataType;
    }

    public String getLabelType() {
        return labelType;
    }
}
