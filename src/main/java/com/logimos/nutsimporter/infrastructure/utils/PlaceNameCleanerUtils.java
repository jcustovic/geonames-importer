package com.logimos.nutsimporter.infrastructure.utils;

import org.apache.commons.text.WordUtils;

public final class PlaceNameCleanerUtils {

    private PlaceNameCleanerUtils() {
    }

    public static String cleanName(String countryCodeIso2, String name) {
        switch (countryCodeIso2) {
            case "DE": // Remove Stadt, Landeshauptstadt, Hansestadt etc. after comma
                if (name.contains(",")) {
                    return name.substring(0, name.indexOf(","));
                }
                return name;
            case "HR": {
                return WordUtils.capitalizeFully(name, ' ', '-');
            }
        }

        return name;
    }
}
