package com.example.ott.customValidation;

import jakarta.validation.GroupSequence;

@GroupSequence({ BlankChecks.class, FormatChecks.class })
public interface ValidationOrder {

}
