package io.stardog.starwizard.services.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class HttpService {
    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpService.class);

    @Inject
    public HttpService(HttpClient httpClient, ObjectMapper mapper) {
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    public HttpService() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();

        // set SSL timeout http://stackoverflow.com/questions/9925113/httpclient-stuck-without-any-exception
        SocketConfig sc = SocketConfig.custom()
                .setSoTimeout(60000)
                .build();
        cm.setDefaultSocketConfig(sc);

        this.httpClient = HttpClientBuilder.create()
                .disableAutomaticRetries()
                .setConnectionManager(cm)
                .build();

        mapper = new ObjectMapper();
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void fetchFile(URI url, File localFile) {
        fetchFile(httpClient, new HttpGet(url), localFile);
    }

    public void fetchFile(HttpClient httpClient, HttpRequestBase request, File localFile) {
        LOGGER.info("Downloading " + request.getURI());
        try {
            localFile.createNewFile();
            FileOutputStream out = new FileOutputStream(localFile);

            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() >= 300) {
                EntityUtils.consumeQuietly(response.getEntity());
                throw new IOException("Unable to fetch from " + request.getURI() + ", status code " + response.getStatusLine());
            }

            response.getEntity().writeTo(out);
            EntityUtils.consumeQuietly(response.getEntity());

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Map<String,Object> getJson(URI uri) {
        return requestJson(new HttpGet(uri));
    }

    public Map<String,Object> getJson(URI uri, Map<String,String> params, List<Header> headers) {
        HttpGet get = new HttpGet(getUrl(uri, params));
        for (Header h : headers) {
            get.addHeader(h);
        }
        return requestJson(get);
    }

    public Map<String,Object> postJson(URI uri, Object postData, List<Header> headers) {
        HttpPost post = new HttpPost(uri);
        post.setEntity(toJsonEntity(postData));
        for (Header h : headers) {
            post.addHeader(h);
        }
        return requestJson(post);
    }

    public URI getUrl(URI baseUrl, Map<String,String> queryParams) {
        UriBuilder builder = UriBuilder.fromUri(baseUrl);
        if (queryParams != null) {
            for (String k : queryParams.keySet()) {
                builder.queryParam(k, queryParams.get(k));
            }
        }
        return builder.build();
    }

    public Map<String,Object> postUrlEncoded(URI uri, Map<String,String> params, List<Header> headers) {
        try {
            HttpPost post = new HttpPost(uri);
            for (Header h : headers) {
                post.addHeader(h);
            }
            List<BasicNameValuePair> paramPairs = params.keySet().stream()
                    .map(k -> new BasicNameValuePair(k, params.get(k)))
                    .collect(Collectors.toList());
            post.setEntity(new UrlEncodedFormEntity(paramPairs));
            return requestJson(post);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String,Object> requestJson(HttpUriRequest request) {
        return requestJson(this.httpClient, request);
    }

    public Map<String,Object> requestJson(HttpClient httpClient, HttpUriRequest request) {
        try {
            HttpResponse response = httpClient.execute(request);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (response.getEntity() != null) {
                response.getEntity().writeTo(out);
                String json = out.toString("UTF-8");
                if (json.startsWith("[")) {
                    json = "{\"data\":" + json + "}";
                }

                return mapper.readValue(json, new TypeReference<HashMap<String, Object>>() {
                });
            } else {
                return ImmutableMap.of();
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public StringEntity toJsonEntity(Object obj) {
        try {
            return new StringEntity(mapper.writeValueAsString(obj));
        } catch (UnsupportedEncodingException|JsonProcessingException e) {
            LOGGER.error("Unexpected exception encoding JSON entity", e);
            throw new RuntimeException(e);
        }
    }
}
