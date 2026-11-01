package com.ruoyi.system.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 商城商品变更后，异步通知 AI 导购服务更新向量库。
 * 商品主流程不依赖该调用，失败只记录日志，不影响商品增删改。
 */
@Component
public class AgentIndexClient
{
    private static final Logger log = LoggerFactory.getLogger(AgentIndexClient.class);

    /** 独立守护线程池，避免阻塞商品主流程或 JVM 退出 */
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "agent-index-sync");
        t.setDaemon(true);
        return t;
    });

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${mall.agent.base-url:http://127.0.0.1:8090}")
    private String baseUrl;

    /** 商品新增/编辑后写入或覆盖向量 */
    public void upsert(Long goodsId)
    {
        submit(goodsId, "POST", "upsert");
    }

    /** 商品删除/下架后移除向量 */
    public void delete(Long goodsId)
    {
        submit(goodsId, "DELETE", "delete");
    }

    private void submit(Long goodsId, String method, String action)
    {
        if (goodsId == null || baseUrl == null || baseUrl.isBlank())
        {
            return;
        }
        String url = baseUrl + "/agent/goods/" + goodsId + "/index";
        executor.submit(() -> send(goodsId, method, action, url));
    }

    private void send(Long goodsId, String method, String action, String url)
    {
        try
        {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300)
            {
                log.warn("AI向量库{}失败, goodsId={}, status={}, body={}",
                        action, goodsId, response.statusCode(), response.body());
            }
        }
        catch (Exception e)
        {
            log.warn("AI向量库{}异常, goodsId={}, error={}", action, goodsId, e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown()
    {
        executor.shutdown();
    }
}
