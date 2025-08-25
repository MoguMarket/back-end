package com.lionkit.mogumarket.recommend.llm;

public interface LlmClient {
    String complete(String prompt);
}