package net.arctel.opkit.service.impl.translation;

import jakarta.annotation.Resource;
import net.arctel.framework.exception.BizException;
import net.arctel.oms.common.constants.ErrorConstant;
import net.arctel.oms.service.OmsParameterService;
import net.arctel.opkit.common.constants.TransEngineConstant;
import net.arctel.opkit.input.TranslationBaseInput;
import net.arctel.opkit.model.TranslationResult;
import net.arctel.opkit.service.TranslationService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Service(TransEngineConstant.DEEPLX)
public class DeeplxTranslationImpl implements TranslationService {

    @Resource
    OmsParameterService omsParameterService;

    @Override
    public String translate(TranslationBaseInput input) {
        @SuppressWarnings("unchecked")
        List<String> paramValueByCode = omsParameterService.getParamValueByCode(20010, List.class);
        if (paramValueByCode == null || paramValueByCode.size() != 2) {
            throw new BizException("缺少参数配置：DEEPLX_BASE_URL");
        }

        DeeplxTranslateClient client = new DeeplxTranslateClient(paramValueByCode.get(0));
        String token = paramValueByCode.get(1);
        try {
            String resultJson = client.translateWithUrlToken(
                    token,
                    input.getText(),
                    input.getSourceLanguage(),
                    input.getTargetLanguage()
            );
            TranslationResult translationResult = TranslationResult.parseFromJson(resultJson);
            return translationResult.getData();
        } catch (Exception e) {
            throw new BizException("翻译失败", e);
        }
    }


    public static class DeeplxTranslateClient {

        private final HttpClient httpClient;
        private final String baseUrl; // e.g. https://api.example.com

        public DeeplxTranslateClient(String baseUrl) {
            if (!baseUrl.startsWith("http")) {
                throw new IllegalArgumentException("Base URL must start with http or https");
            }
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
            this.httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        }

        /**
         * 方式B：如果不能传 Authorization，就用 URL Param：/translate?token=xxx
         */
        public String translateWithUrlToken(String accessToken,
                                            String text,
                                            String sourceLang,
                                            String targetLang) throws Exception {

            String encodedToken = URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
            String url;
            if (baseUrl.equals("https://api.deeplx.org")) {
                url = baseUrl + "/" + encodedToken + "/translate";
            } else {
                url = baseUrl + "/translate?token=" + encodedToken;
            }
            String jsonBody = buildJsonBody(text, sourceLang, targetLang);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            return send(request);
        }

        private String send(HttpRequest request) throws Exception {
            HttpResponse<String> resp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // 429：频繁请求限流，按你描述会出现
            if (resp.statusCode() == 429) {
                throw new RuntimeException("HTTP 429 Too Many Requests: " + resp.body());
            }
            if (resp.statusCode() >= 500) {
                throw new BizException(ErrorConstant.EXTERNAL_ERROR_PREFIX + resp.statusCode(), resp.body());
            }

            // 其他非 2xx
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
            }

            // 返回原始 JSON 字符串（你可以再用 Jackson/Gson 解析）
            return resp.body();
        }

        private String buildJsonBody(String text, String sourceLang, String targetLang) {
            // 简单 JSON 转义（足够覆盖常见字符）；生产建议用 Jackson/Gson 构造 JSON
            return "{"
                    + "\"text\":\"" + jsonEscape(text) + "\","
                    + "\"source_lang\":\"" + jsonEscape(sourceLang) + "\","
                    + "\"target_lang\":\"" + jsonEscape(targetLang) + "\""
                    + "}";
        }

        private String jsonEscape(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\r", "\\r")
                    .replace("\n", "\\n")
                    .replace("\t", "\\t");
        }


    }

}
