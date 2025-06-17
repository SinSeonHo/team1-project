package com.example.ott.type;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum genretype {
    RPG,
    FPS,
    ADVENTURE,
    STRATEGY,
    SIMULATION,
    SPORTS,
    PUZZLE,
    ACTION,
    HORROR,
    COMEDY,
    ROMANCE,
    FANTASY,
    DRAMA,
    THRILLER,
    SLICE_OF_LIFE,
    SCHOOL,
    SCI_FI,
    DOCUMENTARY;

    public static Set<genretype> enumGenre(String pgenre) {
        if (pgenre == null || pgenre.isBlank())
            return Set.of();

        return Arrays.stream(pgenre.split(",")).map(String::trim)
                .map(String::toUpperCase)
                .filter(name -> {
                    try {
                        genretype.valueOf(name);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(genretype::valueOf)
                .collect(Collectors.toSet());

    }

}
