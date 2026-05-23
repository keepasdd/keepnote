package com.keepasd.knowledgebase.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.keepasd.knowledgebase.repository.elasticsearch")
public class ElasticsearchRepositoryConfig {
}
