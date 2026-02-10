
package net.platform.ai.vector;

import java.util.List;

public interface EmbeddingFacade {
    /**
     * 批量计算向量
     */
    List<float[]> vector(List<String> texts);
}
