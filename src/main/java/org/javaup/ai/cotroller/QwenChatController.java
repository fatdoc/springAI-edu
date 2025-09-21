package org.javaup.ai.cotroller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.javaup.ai.products.domain.Product;
import org.javaup.ai.products.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/chat")
public class QwenChatController {

    private static final Logger logger = LoggerFactory.getLogger(QwenChatController.class);

    @Value("${qwen.api-key}")
    private String qwenApiKey;

    @Value("${qwen.base-url:https://dashscope.aliyuncs.com}")
    private String qwenBaseUrl;

    @Value("${qwen.model:qwen-turbo}")
    private String qwenModel;

    @Autowired
    private ProductService productService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> handleChatMessage(@RequestParam("message") String userMessage) {
        if (qwenApiKey == null || qwenApiKey.isEmpty()) {
            logger.error("通义千问 API 配置缺失: api-key 未设置");
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=utf-8");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("text", "通义千问 API 配置错误，请联系管理员");
            return new ResponseEntity<>(errorResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            // 是否识别为中药类问题
            if (needsHerbInfo(userMessage)) {
                String herbName = extractHerbName(userMessage);
                if (herbName != null && !herbName.isEmpty()) {
                    // 1. 获取数据库结构化信息
                    String herbInfoJson = callHerbInfoApi(herbName);
                    JsonNode herbJsonNode = objectMapper.readTree(herbInfoJson);

                    // 2. 用中药信息作为 Prompt，让 AI 生成简要功效说明
                    String prompt = "以txt格式返回给我，请根据以下中药信息，用简洁的语言介绍该中药的功效、用法或相关背景：\n" + herbJsonNode.toPrettyString();
                    String aiText = sendQwenChatRequest(prompt);

                    // 3. 构建统一响应结构
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", "herb_info");
                    response.put("text", aiText);
                    response.put("data", objectMapper.convertValue(herbJsonNode, Map.class));

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Type", "application/json; charset=utf-8");
                    return new ResponseEntity<>(response, headers, HttpStatus.OK);
                }
            }

            // 普通聊天请求（非中药类）
            CompletableFuture<String> qwenFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return sendQwenChatRequest(userMessage);
                } catch (Exception e) {
                    logger.error("通义千问普通聊天处理失败", e);
                    return "抱歉，AI 回答暂时不可用。";
                }
            });

            String qwenAnswer = qwenFuture.get(30, TimeUnit.SECONDS);

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("type", "chat");
            finalResponse.put("text", qwenAnswer);

            logger.info("通义千问最终回复: {}", finalResponse);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=utf-8");
            return new ResponseEntity<>(finalResponse, headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("处理聊天消息时发生错误", e);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json; charset=utf-8");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("text", "请求失败，请检查网络或API配置。");
            return new ResponseEntity<>(errorResponse, headers, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean needsHerbInfo(String userMessage) {
        String[] herbKeywords = {"中药", "药材", "草药", "黄芪", "人参", "当归", "枸杞", "茯苓", "甘草", "白术", "党参", "熟地", "白芍", "川芎", "红花", "丹参", "三七", "天麻", "灵芝", "何首乌", "金银花", "菊花", "薄荷", "板蓝根", "连翘", "蒲公英", "车前草", "决明子", "山楂", "陈皮", "半夏", "贝母", "杏仁", "桔梗", "桑叶"};
        String lowerMessage = userMessage.toLowerCase();
        for (String keyword : herbKeywords) {
            if (lowerMessage.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String extractHerbName(String userMessage) {
        String[] herbNames = {"黄芪", "人参", "当归", "枸杞", "茯苓", "甘草", "白术", "党参", "熟地", "白芍", "川芎", "红花", "丹参", "三七", "天麻", "灵芝", "何首乌", "金银花", "菊花", "薄荷", "板蓝根", "连翘", "蒲公英", "车前草", "决明子", "山楂", "陈皮", "半夏", "贝母", "杏仁", "桔梗", "桑叶"};
        String lowerMessage = userMessage.toLowerCase();
        for (String herbName : herbNames) {
            if (lowerMessage.contains(herbName.toLowerCase())) {
                return herbName;
            }
        }
        return null;
    }

    private String sendQwenChatRequest(String userMessage) throws Exception {
        URL url = new URL(qwenBaseUrl + "/compatible-mode/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + qwenApiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", qwenModel);

        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode userMsg = objectMapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        requestBody.set("messages", messages);

        requestBody.put("temperature", 0.8);
        requestBody.put("max_tokens", 2000);

        String jsonInputString = objectMapper.writeValueAsString(requestBody);
        logger.info("发送给通义千问的请求体: {}", jsonInputString);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        int responseCode = conn.getResponseCode();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }

        logger.info("通义千问响应 (HTTP {}): {}", responseCode, response.toString());
        conn.disconnect();

        if (responseCode >= 400) {
            throw new IOException("通义千问 API 返回错误: " + response.toString());
        }

        JsonNode rootNode = objectMapper.readTree(response.toString());
        JsonNode choices = rootNode.path("choices");
        if (choices.isArray() && choices.size() > 0) {
            JsonNode choice = choices.get(0);
            JsonNode message = choice.path("message");
            return message.path("content").asText();
        }

        return "抱歉，获取回复失败。";
    }

    private String callHerbInfoApi(String herbName) {
        try {
            List<Product> products = productService.getProductsByName(herbName);

            if (products != null && !products.isEmpty()) {
                Product product = products.get(0);

                ObjectNode herbInfo = objectMapper.createObjectNode();
                herbInfo.put("name", product.getProductName());
                herbInfo.put("image_url", product.getMainImageUrl());
                herbInfo.put("price", "¥" + product.getPrice().toString());
                herbInfo.put("purchase_url", "https://www.taobao.com/search?q=" + URLEncoder.encode(product.getProductName(), "UTF-8"));

                return objectMapper.writeValueAsString(herbInfo);
            } else {
                ObjectNode herbInfo = objectMapper.createObjectNode();
                herbInfo.put("name", herbName);
                herbInfo.put("image_url", "http://localhost:8080/static/default.jpg");
                herbInfo.put("price", "暂无价格信息");
                herbInfo.put("purchase_url", "https://www.taobao.com/search?q=" + URLEncoder.encode(herbName, "UTF-8"));

                return objectMapper.writeValueAsString(herbInfo);
            }
        } catch (Exception e) {
            logger.error("调用中药信息API失败", e);
            return "{\"error\": \"查询失败\"}";
        }
    }
}
