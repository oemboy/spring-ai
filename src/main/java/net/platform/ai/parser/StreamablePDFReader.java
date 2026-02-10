
package net.platform.ai.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StreamablePDFReader {
    // 配置：针对 bge-small-zh-v1.5 优化
    public static final int TARGET_CHUNK_SIZE = 500;

    public static final int OVERLAP_SIZE = 100;

    public static final String FINISH_TAG = "FINISH_TAG";

    public static final Chunk FINISH_CHUNK = Chunk.fill(FINISH_TAG, Map.of());

    public record Chunk(String content, Map<String, Object> metadata, Throwable error) {
        public static Chunk error(Throwable e) {
            return new Chunk(null, null, e);
        }

        public static Chunk fill(String content, Map<String, Object> metadata) {
            return new Chunk(content, metadata, null);
        }
    }

    public Stream<Chunk> streamChunks(InputStream inputStream) {
        BlockingQueue<Chunk> queue = new ArrayBlockingQueue<>(1000);

        Thread.ofVirtual().start(() -> {
            // 【改进点】：直接将 InputStream 包装为随机访问缓冲区
            // PDFBox 会根据需要读取字节，而不是一次性全部 load 进堆
            try (RandomAccessReadBuffer rar = new RandomAccessReadBuffer(inputStream);
                PDDocument document = Loader.loadPDF(rar)) {

                SemanticCoordinateStripper stripper = new SemanticCoordinateStripper(queue);
                stripper.setSortByPosition(true);
                stripper.getText(document);

            } catch (Exception e) {
                log.error("streamChunks error:", e);
                if (!queue.offer(Chunk.error(e))) {
                    log.warn("Warning: Failed to put error into the queue.");
                }
            } finally {
                if (!queue.offer(FINISH_CHUNK)) {
                    log.warn("Warning: Failed to put finish marker into the queue.");
                }
            }
        });

        return Stream.generate(() -> {
            try {
                Chunk chunk = queue.take();
                if (chunk.error() != null) {
                    throw new RuntimeException("Fatal error in PDF parsing stream", chunk.error());
                }
                return chunk;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Stream consumption interrupted", e);
            }
        }).takeWhile(chunk -> !FINISH_CHUNK.equals(chunk));
    }

    /**
     * 核心逻辑：带语义聚合和滑动窗口的 Stripper
     */
    private static class SemanticCoordinateStripper extends PDFTextStripper {
        private final BlockingQueue<Chunk> queue;

        // 状态缓冲区
        private final StringBuilder buffer = new StringBuilder();

        private final List<Map<String, Object>> metadataBuffer = new ArrayList<>();

        private int chunkStartPage = -1;

        SemanticCoordinateStripper(BlockingQueue<Chunk> queue) {
            this.queue = queue;
        }

        @Override
        protected void writeString(String string, List<TextPosition> textPositions) {
            if (string == null || string.trim().isEmpty() || textPositions.isEmpty()) {
                return;
            }

            if (chunkStartPage == -1) {
                chunkStartPage = getCurrentPageNo();
            }

            // 1. 累加文本
            buffer.append(string.trim()).append(" ");

            // 2. 收集这一行的位置元数据
            TextPosition first = textPositions.getFirst();
            Map<String, Object> lineMeta = Map.of("p", getCurrentPageNo(), "bbox", Map.of("x", first.getXDirAdj(), "y",
                first.getYDirAdj(), "w", first.getWidthDirAdj(), "h", first.getHeightDir()));
            metadataBuffer.add(lineMeta);

            // 3. 达到阈值则推送 (使用 while 处理单次写入极长的情况)
            while (buffer.length() >= TARGET_CHUNK_SIZE) {
                flushBuffer(false);
            }
        }

        /**
         * 结束文档前，必须把缓冲区最后的数据推出
         */
        @Override
        public void endDocument(PDDocument document) throws IOException {
            if (!buffer.isEmpty()) {
                flushBuffer(true);
            }
            super.endDocument(document);
        }

        private void flushBuffer(boolean isEndOfDoc) {
            // 截取内容
            String content = isEndOfDoc ? buffer.toString() : buffer.substring(0, TARGET_CHUNK_SIZE);

            // 封装 Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("start_page", chunkStartPage);
            metadata.put("end_page", getCurrentPageNo());
            metadata.put("line_count", metadataBuffer.size());
            // 复制一份明细，避免引用问题
            metadata.put("details", new ArrayList<>(metadataBuffer));

            try {
                queue.put(Chunk.fill(content.trim(), metadata));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("PDF Parsing Interrupted", e);
            }

            // --- 滑动窗口处理 ---
            if (!isEndOfDoc) {
                // 计算保留长度
                int keepLength = Math.min(buffer.length(), OVERLAP_SIZE);
                String overlapText = buffer.substring(buffer.length() - keepLength);

                buffer.setLength(0);
                buffer.append(overlapText);

                // 简单的元数据清理：保留最近的几行元数据作为重叠部分的参考
                if (metadataBuffer.size() > 5) {
                    List<Map<String, Object>> nextMeta =
                        new ArrayList<>(metadataBuffer.subList(metadataBuffer.size() - 3, metadataBuffer.size()));
                    metadataBuffer.clear();
                    metadataBuffer.addAll(nextMeta);
                }
            } else {
                buffer.setLength(0);
                metadataBuffer.clear();
            }
            chunkStartPage = getCurrentPageNo();
        }
    }
}