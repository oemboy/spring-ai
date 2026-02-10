
package net.platform.ai.vector;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import lombok.Getter;

@Getter
@Service
@ConditionalOnProperty(name = "ai.embedding.source", havingValue = "local")
public class LocalVectorClient implements EmbeddingFacade {
    private final RestClient restClient;

    private final String baseUrl;

    public LocalVectorClient(RestClient restClient,
        @Value("${ai.embedding.local-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = restClient;
    }

    @Override
    public List<float[]> vector(List<String> texts) {
        // 调用你部署在容器里的 FastAPI 接口
        return restClient.post()
            .uri("/v1/embeddings")
            .body(Map.of("input", texts))
            .retrieve()
            .body(new ParameterizedTypeReference<List<float[]>>() {});
    }
}
