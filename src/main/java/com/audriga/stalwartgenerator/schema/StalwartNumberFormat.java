package com.audriga.stalwartgenerator.schema;

import com.google.gson.annotations.SerializedName;

public enum StalwartNumberFormat {
    integer,
    unsignedInteger,
    @SerializedName("float")
    float_,
    size,
    duration
}
