/*
 * Copyright (C) 2020 The zfoo Authors
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.ai.simulator.util;

import com.zfoo.protocol.collection.CollectionUtils;
import com.zfoo.protocol.util.JsonUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

/**
 * @author godotg
 */
public abstract class HttpUtils {

    public static final RestTemplate restTemplate = new RestTemplate();
    ;


    public static String get(String url) throws IOException, InterruptedException {
        return restTemplate.getForObject(url, String.class);
    }

    public static byte[] getBytes(String url) {
        return restTemplate.getForObject(url, byte[].class);
    }

    public static String post(String url, Object jsonObject) throws IOException, InterruptedException {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36");

        var requestBody = JsonUtils.object2String(jsonObject);
        var requestEntity = new HttpEntity<String>(requestBody, headers);
        var responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        var response = responseEntity.getBody();
        return response;
    }

    /**
     * 主要检查url是否已http开头，如果不已http开头，则认为是非法的url
     */
    public static boolean isHttpUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return false;
        }
        return url.startsWith("http://") || url.startsWith("https://");
    }

    /**
     * 检查文件的url是否合法
     */
    public static boolean isHttpUrls(List<String> urlLinks) {
        if (CollectionUtils.isEmpty(urlLinks)) {
            return true;
        }
        for (var urlLink : urlLinks) {
            if (!isHttpUrl(urlLink)) {
                return false;
            }
        }
        return true;
    }
}
