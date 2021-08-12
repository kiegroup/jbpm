/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.event.emitters.elasticsearch;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jbpm.persistence.api.integration.EventCollection;
import org.jbpm.persistence.api.integration.EventEmitter;
import org.jbpm.persistence.api.integration.InstanceView;
import org.jbpm.persistence.api.integration.base.BaseEventCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic ElasticSearch implementation of EventEmitter that simply pushes out data to
 * ElasticSearch server. It performs all the operation in the background thread but does not 
 * do any intermediate data persistence, meaning it can result in data lost in case of server
 * crashes. 
 * 
 * This event emitter expects following parameters to configure itself - via system properties
 * <ul>
 *  <li>org.jbpm.event.emitters.elasticsearch.date_format - date and time format to be sent to ElasticSearch - default format is yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
 *  <li>org.jbpm.event.emitters.elasticsearch.url - location of the ElasticSearch server - defaults to http://localhost:9200</li>
 *  <li>org.jbpm.event.emitters.elasticsearch.user - optional user name for authentication to ElasticSearch server</li>
 *  <li>org.jbpm.event.emitters.elasticsearch.password - optional password for authentication to ElasticSearch server</li>
 * </ul>
 * 
 * NOTE: Optional authentication is a BASIC authentication. 
 */
public class ElasticSearchEventEmitter implements EventEmitter {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchEventEmitter.class);
    
    private String elasticSearchUrl = System.getProperty("org.jbpm.event.emitters.elasticsearch.url", "http://localhost:9200");
    private String elasticSearchUser = System.getProperty("org.jbpm.event.emitters.elasticsearch.user");
    private String elasticSearchPassword = System.getProperty("org.jbpm.event.emitters.elasticsearch.password");
    
    private static ESInstanceViewTransformerFactory getFactory ()
    {
        ESInstanceViewTransformerFactory factory = null;
        String className = System.getProperty("org.jbpm.event.emitters.elasticsearch.factory");
        if (className != null) {
            try {
                factory = Class.forName(className).asSubclass(ESInstanceViewTransformerFactory.class).getConstructor().newInstance();
            }
            catch (ReflectiveOperationException | ClassCastException ex) {
                logger.warn ("Error initializing configured factory "+className+ " using default",ex);
            }
        }
        return factory != null ? factory : new DefaultESInstanceViewTransformerFactory();
    }
    
    private ObjectMapper mapper = new ObjectMapper();
    
    private ESInstanceViewTransformerFactory factory = getFactory();
   

    private ExecutorService executor;
    
    private CloseableHttpClient httpclient;

    public ElasticSearchEventEmitter() {
        factory.configureObjectMapper(mapper);
        executor = buildExecutorService();
        httpclient = buildClient();
    }
    

    @Override
    public void deliver(Collection<InstanceView<?>> data) {
        // no-op

    }

    @Override
    public void apply(Collection<InstanceView<?>> data) {
        if (data.isEmpty()) {
            return;
        }

        executor.execute(() -> {
            StringBuilder content = new StringBuilder();
            Set<String> visitedIds = new HashSet<>();
            for (InstanceView<?> view : data) {
                try {
                    ESInstanceViewTransformer instanceView = factory.getInstanceViewTransformer(view);
                    if (instanceView == null) {
                        logger.warn("Unsupported view {} ", view.getClass());
                        continue;
                    }
                    ESRequest request = visitedIds.add(view.getCompositeId()) ? instanceView.index(view) : instanceView
                            .update(view);
                    content.append(String.format(
                            "{ \"%s\" : { \"_index\" : \"%s\", \"_type\" : \"%s\", \"_id\" : \"%s\" } }%n%s%n", request
                                    .getOperation(), request.getIndex(), request.getType(), request.getId(), mapper
                                            .writeValueAsString(request.getBody())));
                } catch (JsonProcessingException e) {
                    logger.error("Error while serializing {} to JSON", view, e);
                }
            }

            try {
                HttpPut httpPut = new HttpPut(elasticSearchUrl + "/_bulk");
                httpPut.setEntity(new StringEntity(content.toString(), "UTF-8"));
                logger.debug("Elastic search request {}", httpPut.getRequestLine());
                httpPut.setHeader("Content-Type", "application/x-ndjson");

                // Create a custom response handler
                ResponseHandler<String> responseHandler = response -> {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity entity = response.getEntity();
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                };
                String responseBody = httpclient.execute(httpPut, responseHandler);
                logger.debug("Elastic search response: {}", responseBody);
            } catch (Exception e) {
                logger.error("Unexpected exception while sending data to ElasticSearch", e);
            }
        });
    }

    @Override
    public void drop(Collection<InstanceView<?>> data) {
        // no-op

    }

    @Override
    public EventCollection newCollection() {
        return new BaseEventCollection();
    }

    @Override
    public void close() {
        try {
            httpclient.close();
        } catch (IOException e) {
            logger.error("Error when closing http client", e);
        }
        
        executor.shutdown();
        logger.info("Elasticsearch event emitter closed successfully");
    }

    protected CloseableHttpClient buildClient() {

        HttpClientBuilder builder = HttpClients.custom();

        if (elasticSearchUser != null && elasticSearchPassword != null) {
            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(elasticSearchUser, elasticSearchPassword);
            provider.setCredentials(AuthScope.ANY, credentials);
            
            builder.setDefaultCredentialsProvider(provider);
        }

        return builder.build();
    }
    
    protected ExecutorService buildExecutorService() {

        return Executors.newCachedThreadPool();
    }

}
