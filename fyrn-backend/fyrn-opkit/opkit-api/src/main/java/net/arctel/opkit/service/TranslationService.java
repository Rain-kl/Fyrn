package net.arctel.opkit.service;

import net.arctel.opkit.input.TranslationBaseInput;

public interface TranslationService {

    /**
     * 翻译文本
     *
     * @param input 翻译输入参数
     * @return 翻译后的文本
     */
    String translate(TranslationBaseInput input);


}
