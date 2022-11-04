// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.launcher.util;

public enum JavaHeapSize {

    NOT_USED("", "heapsize_notUsed"),
    MB_256("256m", "heapsize_mb_256"),
    MB_512("512m", "heapsize_mb_512"),
    MB_768("768m", "heapsize_mb_768"),
    GB_1("1g", "heapsize_gb_1"),
    GB_1_5("1536m", "heapsize_gb_1_5"),
    GB_2("2g", "heapsize_gb_2"),
    GB_2_5("2560m", "heapsize_gb_2_5"),
    GB_3("3g", "heapsize_gb_3"),
    GB_4("4g", "heapsize_gb_4"),
    GB_5("5g", "heapsize_gb_5"),
    GB_6("6g", "heapsize_gb_6"),
    GB_7("7g", "heapsize_gb_7"),
    GB_8("8g", "heapsize_gb_8"),
    GB_9("9g", "heapsize_gb_9"),
    GB_10("10g", "heapsize_gb_10"),
    GB_11("11g", "heapsize_gb_11"),
    GB_12("12g", "heapsize_gb_12"),
    GB_13("13g", "heapsize_gb_13"),
    GB_14("14g", "heapsize_gb_14"),
    GB_15("15g", "heapsize_gb_15"),
    GB_16("16g", "heapsize_gb_16");

    private final String sizeParameter;
    private final String labelKey;

    JavaHeapSize(String sizeParameter, String labelKey) {
        this.sizeParameter = sizeParameter;
        this.labelKey = labelKey;
    }

    public final boolean isUsed() {
        return this != NOT_USED;
    }

    public final String getSizeParameter() {
        return sizeParameter;
    }

    public String getLabelKey() {
        return labelKey;
    }

    @Override
    public final String toString() {
        return I18N.getLabel(labelKey);
    }
}
