/**
 * This file is licensed under the MIT license.
 * See the LICENSE file in project root for details.
 */

package org.terasology.launcher.packages;

/**
 * Thrown when errors are encountered within {@link PackageManager}.
 */
public class PackageManagerException extends RuntimeException {
    public PackageManagerException(final String message) {
        super(message);
    }
}
