/*
 * Copyright (c) 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.launcher.util;

import java.util.ArrayList;
import java.util.List;

public enum JavaHeapSize {

    NOT_USED(0, "", "heapsize_notUsed"),
    MB_256(256, "256m", "heapsize_mb_256"),
    MB_512(512, "512m", "heapsize_mb_512"),
    MB_768(768, "768m", "heapsize_mb_768"),
    GB_1(1024, "1g", "heapsize_gb_1"),
    GB_1_5(1536, "1536m", "heapsize_gb_1_5"),
    GB_2(2 * 1024, "2g", "heapsize_gb_2"),
    GB_2_5(2560, "2560m", "heapsize_gb_2_5"),
    GB_3(3 * 1024, "3g", "heapsize_gb_3"),
    GB_4(4 * 1024, "4g", "heapsize_gb_4"),
    GB_5(5 * 1024, "5g", "heapsize_gb_5"),
    GB_6(6 * 1024, "6g", "heapsize_gb_6"),
    GB_7(7 * 1024, "7g", "heapsize_gb_7"),
    GB_8(8 * 1024, "8g", "heapsize_gb_8"),
    GB_9(9 * 1024, "9g", "heapsize_gb_9"),
    GB_10(10 * 1024, "10g", "heapsize_gb_10"),
    GB_11(11 * 1024, "11g", "heapsize_gb_11"),
    GB_12(12 * 1024, "12g", "heapsize_gb_12"),
    GB_13(13 * 1024, "13g", "heapsize_gb_13"),
    GB_14(14 * 1024, "14g", "heapsize_gb_14"),
    GB_15(15 * 1024, "15g", "heapsize_gb_15"),
    GB_16(16 * 1024, "16g", "heapsize_gb_16");

    private static final int MAX_32_BIT_MB = 1024;

    private final int mb;
    private final String sizeParameter;
    private final String labelKey;

    private JavaHeapSize(final int mb, final String sizeParameter, final String labelKey) {
        this.mb = mb;
        this.sizeParameter = sizeParameter;
        this.labelKey = labelKey;
    }

    public final boolean isUsed() {
        return this != NOT_USED;
    }

    public final String getSizeParameter() {
        return sizeParameter;
    }

    public final String toString() {
        return BundleUtils.getLabel(labelKey);
    }

    public static List<JavaHeapSize> getHeapSizes(final long totalPhysicalMemorySize, final boolean bit64) {
        final List<JavaHeapSize> heapSizes = new ArrayList<JavaHeapSize>();

        for (final JavaHeapSize heapSize : JavaHeapSize.values()) {
            if ((heapSize.mb <= totalPhysicalMemorySize) && (bit64 || heapSize.mb <= MAX_32_BIT_MB)) {
                heapSizes.add(heapSize);
            }
        }

        return heapSizes;
    }

}
