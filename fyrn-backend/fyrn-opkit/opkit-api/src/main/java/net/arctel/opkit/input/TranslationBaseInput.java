package net.arctel.opkit.input;

import lombok.Data;

@Data
public class TranslationBaseInput {

    private String text;

    private String sourceLanguage;

    private String targetLanguage;
}
