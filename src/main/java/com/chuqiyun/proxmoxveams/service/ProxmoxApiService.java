package com.chuqiyun.proxmoxveams.service;

import com.alibaba.fastjson2.JSONObject;
import com.chuqiyun.proxmoxveams.entity.Master;
import com.chuqiyun.proxmoxveams.utils.TrustSslUtil;
import com.chuqiyun.proxmoxveams.utils.exception.UnauthorizedException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * @author mryunqi
 * @date 2023/6/18
 */
public class ProxmoxApiService {
    /**
    * @Author: mryunqi
    * @Description: 获取节点url
    * @DateTime: 2023/6/20 15:26
    * @Params: Master node
    * @Return String
    */
    public String getNodeUrl(Master node) {
        return "https://" + node.getHost() + ":" + node.getPort() + "/api2/json";
    }

    /**
    * @Author: mryunqi
    * @Description: 通用POST请求
    * @DateTime: 2023/6/20 16:33
    * @Params: Master node, HashMap<String,String> cookie,String url, HashMap<String, String> params
    * @Return  JSONObject
    */
    public JSONObject postNodeApi(Master node, HashMap<String,String> cookie,String url, HashMap<String, Object> params) throws UnauthorizedException {
        // 构建请求主体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", cookie.get("cookie"));
        headers.add("CSRFPreventionToken", cookie.get("CSRFPreventionToken"));

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        for (String key : params.keySet()) {
            requestBody.add(key, params.get(key));
        }

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // 忽略证书验证
        TrustSslUtil.initDefaultSsl();
        // 发送 POST 请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(getNodeUrl(node) + url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 提取 Cookie 和 CSRF 预防令牌
            JSONObject body = JSONObject.parseObject(response.getBody());
            assert body != null;
            return body;
        } else {
            throw new UnauthorizedException("请求失败");
        }
    }

    /**
    * @Author: mryunqi
    * @Description: 通用GET请求
    * @DateTime: 2023/6/20 16:39
    */
    public JSONObject getNodeApi(Master node, HashMap<String,String> cookie,String url, HashMap<String, Object> params) throws UnauthorizedException {
        // 构建请求主体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", cookie.get("cookie"));
        headers.add("CSRFPreventionToken", cookie.get("CSRFPreventionToken"));

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        for (String key : params.keySet()) {
            requestBody.add(key, params.get(key));
        }

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // 忽略证书验证
        TrustSslUtil.initDefaultSsl();
        // 发送 POST 请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(getNodeUrl(node) + url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 提取 Cookie 和 CSRF 预防令牌
            JSONObject body = JSONObject.parseObject(response.getBody());
            assert body != null;
            return body;
        } else {
            throw new UnauthorizedException("请求失败");
        }
    }

    /**
    * @Author: mryunqi
    * @Description: 通用PUT请求
    * @DateTime: 2023/6/20 23:00
    */
    public JSONObject putNodeApi(Master node, HashMap<String,String> cookie,String url, HashMap<String, Object> params) throws UnauthorizedException {
        // 构建请求主体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Cookie", cookie.get("cookie"));
        headers.add("CSRFPreventionToken", cookie.get("CSRFPreventionToken"));

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        for (String key : params.keySet()) {
            requestBody.add(key, params.get(key));
        }

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        // 忽略证书验证
        TrustSslUtil.initDefaultSsl();
        // 发送 POST 请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(getNodeUrl(node) + url, HttpMethod.PUT, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 提取 Cookie 和 CSRF 预防令牌
            JSONObject body = JSONObject.parseObject(response.getBody());
            assert body != null;
            return body;
        } else {
            throw new UnauthorizedException("请求失败");
        }
    }

    /**
    * @Author: mryunqi
    * @Description: 登录获取cookie
    * @DateTime: 2023/6/20 15:22
    * @Return
    */
    public HashMap<String, String> loginAndGetCookie(HashMap<String,String> user){
        RestTemplate restTemplate = new RestTemplate();

        // 构建请求主体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("username", user.get("username"));
        requestBody.add("password", user.get("password"));
        requestBody.add("realm", user.get("realm"));

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);
        // 忽略证书验证
        TrustSslUtil.initDefaultSsl();
        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.exchange(user.get("url") + "/api2/json/access/ticket", HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            // 提取 Cookie 和 CSRF 预防令牌
            JSONObject body = JSONObject.parseObject(response.getBody());
            assert body != null;
            String ticket = body.getJSONObject("data").getString("ticket");
            String csrfToken = body.getJSONObject("data").getString("CSRFPreventionToken");

            // 将 Cookie 和 CSRF 预防令牌存储起来，以便后续使用
            HashMap<String, String> authenticationTokens = new HashMap<>();
            authenticationTokens.put("ticket",ticket);
            authenticationTokens.put("csrfToken",csrfToken);
            return authenticationTokens;
        } else {
            throw new UnauthorizedException("Login failed");
        }
    }

}