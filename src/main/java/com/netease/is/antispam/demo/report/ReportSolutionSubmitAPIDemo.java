/*
 * @(#) ReportSolutionSubmitAPIDemo.java 2021-09-03
 *
 * Copyright 2021 NetEase.com, Inc. All rights reserved.
 */

package com.netease.is.antispam.demo.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netease.is.antispam.demo.utils.DemoConstants;
import com.netease.is.antispam.demo.utils.HttpClient4Utils;
import com.netease.is.antispam.demo.utils.SignatureUtils;
import org.apache.http.Consts;
import org.apache.http.client.HttpClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 举报解决方案检测提交接口API示例
 *
 * @author spring404
 * @version 2021-09-03
 */
public class ReportSolutionSubmitAPIDemo {

    /**
     * 产品密钥ID，产品标识
     */
    private final static String SECRETID = "your_secret_id";
    /**
     * 产品私有密钥，服务端生成签名信息使用，请严格保管，避免泄露
     */
    private final static String SECRETKEY = "your_secret_key";
    /**
     * 易盾反垃圾举报解决方案在线检测接口地址
     */
    private final static String API_URL = "http://as.dun.163.com/v1/report/submit";
    /**
     * 实例化HttpClient，发送http请求使用，可根据需要自行调参
     */
    private static HttpClient httpClient = HttpClient4Utils.createHttpClient(100, 20, 10000, 2000, 2000);


    public static void main(String[] args) {
        Map<String, String> params = new HashMap<>(16);
        // 1.设置公共参数
        params.put("secretId", SECRETID);
        params.put("version", "v1");
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("nonce", String.valueOf(new Random().nextInt()));
        // MD5, SM3, SHA1, SHA256
        params.put("signatureMethod", "MD5");

        // 2.设置私有参数
        params.put("dataId", "0000");
        params.put("callback", "i am callback");
        params.put("account", "account1");
        params.put("scenarios", "直播涉政");
        params.put("roomId", "room3");
        params.put("reportedId", "user010");
        params.put("reportType", "举报类型");
        //举报内容
        JsonArray jsonArray = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("type", "text");
        text.addProperty("data", "文本信息");
        jsonArray.add(text);
        JsonObject image = new JsonObject();
        image.addProperty("type", "image");
        image.addProperty("data", "http://xxx.jpg");
        jsonArray.add(image);
        JsonObject audio = new JsonObject();
        audio.addProperty("type", "audio");
        audio.addProperty("data", "http://xxx.mp3");
        jsonArray.add(audio);
        JsonObject audiovideo = new JsonObject();
        audiovideo.addProperty("type", "audiovideo");
        audiovideo.addProperty("data", "http://xxx.mp4");
        jsonArray.add(audiovideo);
        params.put("content", jsonArray.toString());

        // 3.生成签名信息
        String signature = SignatureUtils.genSignature(SECRETKEY, params);
        params.put("signature", signature);

        // 4.发送HTTP请求，这里使用的是HttpClient工具包，产品可自行选择自己熟悉的工具包发送请求
        String response = HttpClient4Utils.sendPost(httpClient, API_URL, params, Consts.UTF_8);

        // 5.解析接口返回值
        JsonObject jObject = new JsonParser().parse(response).getAsJsonObject();
        int code = jObject.get("code").getAsInt();
        String msg = jObject.get("msg").getAsString();
        if (code == DemoConstants.SUCCESS_CODE) {
            JsonObject result = jObject.getAsJsonObject("result").getAsJsonObject("antispam");
            String taskId = result.get("taskId").getAsString();
            String dataId = result.has("dataId") ? result.get("dataId").getAsString() : "";
            System.out.printf("SUBMIT SUCCESS: taskId=%s, dataId=%s%n", taskId, dataId);
        } else {
            System.out.printf("ERROR: code=%s, msg=%s%n", code, msg);
        }
    }
}