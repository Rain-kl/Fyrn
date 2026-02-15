package net.arctel.opkit.model;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranslationResult {

    private String data;

    private List<String> alternatives;

    public static TranslationResult parseFromJson(String json) {
        return JSON.parseObject(json, TranslationResult.class);
    }

}
