package pine.log.monitor.config;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import pine.log.monitor.engine.DataEngine;
import pine.log.monitor.engine.impl.ElasticSearchDataEngine;
import lombok.Data;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Data
@Configuration
@ConfigurationProperties(prefix = "spring.elasticsearch")
@ConditionalOnProperty(prefix = "spring.elasticsearch",  name = "enable", havingValue = "true")
public class ElasticSearchConfig {

    private String hostname;

    private String port;

    private String username;

    private String password;

    @Bean
    public RestClient buildRestClient(){
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(
                         username == null ? "elastic" : username,
                        password == null ? "changeme" : password));

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(
                        hostname == null ? "localhost" : hostname,
                        Integer.valueOf(port == null ? "9200" : port)))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(
                            HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
      return builder.build();
    }

    /**
     *  同步客户端
     * @return
     */
    @Bean
    @ConditionalOnClass(RestClient.class)
    public ElasticsearchClient elasticsearchClient(RestClient restClient){
        ElasticsearchTransport transport = buildTransferPort(restClient);
        return new ElasticsearchClient(transport);
    }

    /**
     *  异步客户端
     * @return
     */
    @Bean
    @ConditionalOnClass(RestClient.class)
    public ElasticsearchAsyncClient elasticSearchAsyncClient(RestClient restClient){
        ElasticsearchTransport transport = buildTransferPort(restClient);
        // And create the API client
        return new ElasticsearchAsyncClient(transport);
    }

    @Bean
    @Primary
    @ConditionalOnBean(value = ElasticsearchClient.class)
    public DataEngine getElasticDataEngine(){
        return new ElasticSearchDataEngine();
    }

    private ElasticsearchTransport buildTransferPort(RestClient restClient){
        // Create the transport with a Jackson mapper
        return new RestClientTransport(
                restClient, new JacksonJsonpMapper());
    }


}
