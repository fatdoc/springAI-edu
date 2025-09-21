package org.javaup.ai.cotroller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TTS语音合成控制器
 */
@RestController
@RequestMapping("/tts")
public class TTSController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 文本转语音
     * @param
     * @return 音频数据
     */
    @PostMapping("/synthesize")
    public ResponseEntity<?> textToSpeech(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("文本内容不能为空");
            }

            // 构建请求体
            Map<String, Object> requestBody = buildTTSRequest(text);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer HfZ61-mR05QDMg50V1foczoubgz9mFyY");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 调用TTS API
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://openspeech.byteoversea.com/api/v1/tts",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                // 解析响应
                JsonNode jsonResponse = objectMapper.readTree(response.getBody());
                String audioBase64 = jsonResponse.path("data").asText();

                if (audioBase64.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("TTS服务未返回音频数据");
                }

                // 解码Base64音频数据
                byte[] audioBytes = Base64.getDecoder().decode(audioBase64);

                // 返回音频数据
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.valueOf("audio/mpeg"));
                responseHeaders.setContentLength(audioBytes.length);
                responseHeaders.set("Content-Disposition", "inline; filename=speech.mp3");

                return new ResponseEntity<>(audioBytes, responseHeaders, HttpStatus.OK);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("TTS服务调用失败: " + response.getBody());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("语音合成失败: " + e.getMessage());
        }
    }

    /**
     * 构建TTS请求体
     */
    private Map<String, Object> buildTTSRequest(String text) {
        Map<String, Object> requestBody = new HashMap<>();

        // 1. app 部分（不含 token，如果你走 Authorization header 方式）
        Map<String, Object> app = new HashMap<>();
        app.put("appid", "你的 AppId");  // 必须替换为你真实的
        app.put("cluster", "volcano_tts");

        // 2. user 部分
        Map<String, Object> user = new HashMap<>();
        user.put("uid", "你的用户ID");  // 可自定义，如用户唯一标识

        // 3. audio 配置
        Map<String, Object> audio = new HashMap<>();
        audio.put("voice_type", "BV700_streaming");
        audio.put("encoding", "mp3");
        audio.put("compression_rate", 1);
        audio.put("rate", 24000);
        audio.put("speed_ratio", 1.0);
        audio.put("volume_ratio", 1.0);
        audio.put("pitch_ratio", 1.0);
        audio.put("emotion", "happy");
        audio.put("language", "cn");

        // 4. request 配置
        Map<String, Object> request = new HashMap<>();
        request.put("reqid", UUID.randomUUID().toString());
        request.put("text", text);
        request.put("text_type", "plain");
        request.put("operation", "query");
        request.put("silence_duration", "125");
        request.put("with_frontend", "1");
        request.put("frontend_type", "unitTson");
        request.put("pure_english_opt", "1");

        // 5. extra_param 子结构（嵌套 map）
        Map<String, Object> extraParam = new HashMap<>();
        extraParam.put("disable_emoji_filter", true);

        request.put("extra_param", extraParam);  // 嵌入 extra_param

        // 总体组装
        requestBody.put("app", app);
        requestBody.put("user", user);
        requestBody.put("audio", audio);
        requestBody.put("request", request);

        return requestBody;
    }
}