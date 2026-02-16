-- 1. 创建 Schema
CREATE SCHEMA IF NOT EXISTS ailab;

-- 2. 开启向量扩展
CREATE EXTENSION IF NOT EXISTS vector SCHEMA ailab;

-- 3. 创建表
CREATE TABLE ailab.doc_chunks
(
    id        bigserial PRIMARY KEY,
    file_id   uuid,
    content   text,
    page_num  int,
    chunk_idx int,
    text_type varchar(20),
    bbox      jsonb,
    embedding vector(1024)
);