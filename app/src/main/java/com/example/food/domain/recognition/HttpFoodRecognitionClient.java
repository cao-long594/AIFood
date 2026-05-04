package com.example.food.domain.recognition;

import com.example.food.BuildConfig;
import com.example.food.core.concurrent.AppExecutors;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpFoodRecognitionClient implements FoodRecognitionClient {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient httpClient;
    private final Gson gson;

    public HttpFoodRecognitionClient() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(45, TimeUnit.SECONDS)
                .writeTimeout(45, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    @Override
    public void recognize(File image, Callback callback) {
        String apiUrl = BuildConfig.FOOD_RECOGNITION_API_URL;
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            callback.onError("请先配置识别接口地址");
            return;
        }

        AppExecutors.runOnIo(() -> {
            String apiKey = BuildConfig.FOOD_RECOGNITION_API_KEY;
            if (apiKey == null || apiKey.trim().isEmpty()) {
                postError(callback, "请先配置识别接口密钥");
                return;
            }

            String model = BuildConfig.FOOD_RECOGNITION_MODEL;
            if (model == null || model.trim().isEmpty()) {
                postError(callback, "请先配置识别模型");
                return;
            }

            try {
                Request.Builder requestBuilder = new Request.Builder()
                        .url(apiUrl)
                        .header("Authorization", "Bearer " + apiKey.trim())
                        .header("Content-Type", "application/json")
                        .post(RequestBody.create(buildRequestJson(image, model.trim()), JSON));

                Response response = httpClient.newCall(requestBuilder.build()).execute();
                try (response) {
                ResponseBody responseBody = response.body();
                String json = responseBody == null ? "" : responseBody.string();
                if (!response.isSuccessful()) {
                    postError(callback, "识别失败：" + response.code() + formatErrorBody(json));
                    return;
                }

                RecognitionResult result = parseDashScopeResult(json);
                if (result == null || result.getNutritionPer100g() == null) {
                    postError(callback, "识别结果缺少营养数据");
                    return;
                }
                AppExecutors.runOnMain(() -> callback.onSuccess(result));
                }
            } catch (IOException e) {
                postError(callback, "网络请求失败，请稍后重试");
            } catch (JsonSyntaxException e) {
                postError(callback, "识别结果格式不正确");
            } catch (IllegalStateException e) {
                postError(callback, e.getMessage());
            }
        });
    }

    private String buildRequestJson(File image, String model) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("model", model);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");

        JsonArray content = new JsonArray();
        JsonObject imageContent = new JsonObject();
        imageContent.addProperty("type", "image_url");
        JsonObject imageUrl = new JsonObject();
        imageUrl.addProperty("url", "data:image/jpeg;base64," + encodeImage(image));
        imageContent.add("image_url", imageUrl);
        content.add(imageContent);

        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text",
                "请识别图片中的主要食物，并估算这顿饭的摄入重量。"
                        + "只返回 JSON，不要返回 Markdown。格式："
                        + "{\"canonicalFoodName\":\"食物名\","
                        + "\"nutritionPer100g\":{\"calories\":0,\"carbohydrate\":0,\"protein\":0,\"fat\":0,"
                        + "\"saturatedFat\":0,\"monounsaturatedFat\":0,\"polyunsaturatedFat\":0},"
                        + "\"estimatedAmountGram\":100,\"sourceProvider\":\"dashscope\","
                        + "\"confidence\":0.8,\"topK\":[{\"name\":\"食物名\",\"confidence\":0.8,\"ingredients\":[\"食材\"]}],"
                        + "\"possibleIngredients\":[\"食材\"]}。"
                        + "营养值必须按每 100g 估算，单位为 kcal 或 g。");
        content.add(textContent);

        message.add("content", content);
        messages.add(message);
        root.add("messages", messages);
        return gson.toJson(root);
    }

    private RecognitionResult parseDashScopeResult(String json) {
        JsonObject root = gson.fromJson(json, JsonObject.class);
        JsonArray choices = root == null ? null : root.getAsJsonArray("choices");
        if (choices == null || choices.size() == 0) {
            throw new IllegalStateException("识别接口没有返回结果");
        }

        JsonObject message = choices.get(0).getAsJsonObject().getAsJsonObject("message");
        if (message == null || !message.has("content")) {
            throw new IllegalStateException("识别接口返回内容为空");
        }

        String content = message.get("content").getAsString();
        return gson.fromJson(stripCodeFence(content), RecognitionResult.class);
    }

    private String encodeImage(File image) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        try (FileInputStream input = new FileInputStream(image)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        }
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
    }

    private String stripCodeFence(String content) {
        if (content == null) {
            return "";
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?", "").trim();
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
            }
        }
        return trimmed;
    }

    private String formatErrorBody(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "";
        }
        String compact = body.trim().replaceAll("\\s+", " ");
        if (compact.length() > 160) {
            compact = compact.substring(0, 160) + "...";
        }
        return "：" + compact;
    }

    private void postError(Callback callback, String message) {
        AppExecutors.runOnMain(() -> callback.onError(message));
    }
}
