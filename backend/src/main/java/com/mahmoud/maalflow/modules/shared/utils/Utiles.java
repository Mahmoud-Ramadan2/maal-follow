package com.mahmoud.maalflow.modules.shared.utils;

/**
 * Utility class for  validation.
 *
 * @author Mahmoud
 */
public class Utiles {


    /**
     * Check if the given value is a valid enum constant for the specified enum class.
     *
     * @param enumClass The enum class to check against.
     * @param value     The string value to validate.
     * @return true if the value is a valid enum constant, false otherwise.
     */
    public static boolean isValidEnumValue(Class<? extends Enum<?>> enumClass, String value) {
        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
