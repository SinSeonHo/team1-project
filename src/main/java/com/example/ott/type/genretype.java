package com.example.ott.type;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum GenreType {
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

    public static Set<GenreType> enumGenre(String pgenre) {
        if (pgenre == null || pgenre.isBlank())
            return Set.of();

        return Arrays.stream(pgenre.split(",")).map(String::trim)
                .map(String::toUpperCase)
                .filter(name -> {
                    try {
                        GenreType.valueOf(name);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                })
                .map(GenreType::valueOf)
                .collect(Collectors.toSet());

    }

}
