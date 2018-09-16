package com.training.utility;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.ManagedBean;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

@ManagedBean
public class TrainingRestDelegate {
    private static final Logger LOG = LogManager.getLogger(TrainingRestDelegate.class.getName());

    public TrainingRestDelegate() {
    }

    public Response restGet(String url, String accessToken) {
        LOG.debug("URL : " + url + " SessionID : " + accessToken);
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Authorization", accessToken)
                .accept(Constants.CONTENT_TYPE).get();

        response.bufferEntity();
        client.close();

        return (response);
    }

    public Response restGetByQueryParam(String url,Map<String,String> queryStrings) {
        LOG.debug("URL : " + url);
        final Client client = createJaxRsClient();
        WebTarget target = client.target(url);
        for(String key:queryStrings.keySet()) {
        	String value = queryStrings.get(key);
        		target = target.queryParam(key, value);
        }
        LOG.info("target is " + target.getUri());
        final Response response = target.request().accept(Constants.CONTENT_TYPE).get();

        response.bufferEntity();
        client.close();

        return (response);
    }
    public Response restGetByQueryParamByContentType(String url,Map<String,String> queryStrings,String contentType) {
        LOG.debug("URL : " + url);
        final Client client = createJaxRsClient();
        WebTarget target = client.target(url);
        for(String key:queryStrings.keySet()) {
        	String value = queryStrings.get(key);
        		target = target.queryParam(key, value);
        }
        LOG.info("target is " + target.getUri());
        final Response response = target.request().accept(contentType).get();

        response.bufferEntity();
        client.close();

        return (response);
    }
    public Response restGetByQueryParam(String url,Map<String,String> queryStrings, String accessToken) {
        LOG.debug("URL : " + url);
        final Client client = createJaxRsClient();
        WebTarget target = client.target(url);
        for(String key:queryStrings.keySet()) {
        	String value = queryStrings.get(key);
        		target = target.queryParam(key, value);
        }
        LOG.info("target is " + target.getUri());
        final Response response = target.request().header("Authorization", accessToken).accept(Constants.CONTENT_TYPE).get();

        response.bufferEntity();
        client.close();

        return (response);
    }
    public Response restPost(String url, Object entity, String accessToken) {

        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Content-Type", Constants.CONTENT_TYPE)
                .header("Authorization", accessToken).accept(Constants.CONTENT_TYPE).post(Entity.json(entity));

        response.bufferEntity();
        client.close();

        return (response);
    }

    public Response restChangePost(String url, Object entity, String accessToken, String userToken) {

        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Content-Type", Constants.CONTENT_TYPE)
                .header("Authorization", accessToken).header("X-CUSTOMIZE-TOKEN", userToken)
                .accept(Constants.CONTENT_TYPE).post(Entity.json(entity));

        response.bufferEntity();
        client.close();

        return (response);
    }



    public Response restChangeGet(String url, String accessToken, String userToken) {
        LOG.debug("URL : " + url + " SessionID : " + accessToken);
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Authorization", accessToken).header("X-CUSTOMIZE-TOKEN", userToken)
                .accept(Constants.CONTENT_TYPE).get();

        response.bufferEntity();
        client.close();

        return (response);
    }

    public Response restPutChangeLab(String url, String accessToken, String labId, Object entity) {
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Authorization", accessToken).header("LABID", labId)
                .accept(Constants.CONTENT_TYPE).put((Entity.entity(entity, Constants.CONTENT_TYPE)));

        response.bufferEntity();
        client.close();

        return (response);
    }

    /* Overloaded restPost for when you need to make a REST call where the Content-Type isn't 'application/json' */
    public Response restPost(String url, Object entity, String accessToken, String mediaType) {

        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Authorization", accessToken).accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(entity, mediaType));

        response.bufferEntity();
        client.close();

        return (response);
    }

    public Response restDelete(String url, String accessToken) {

        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Authorization", accessToken)
                .accept(Constants.CONTENT_TYPE).delete();

        response.bufferEntity();
        client.close();

        return (response);
    }

    public Response restPut(String url, String accessToken, Object entity) {
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Authorization", accessToken)
                .accept(Constants.CONTENT_TYPE).put((Entity.entity(entity, Constants.CONTENT_TYPE)));
        response.bufferEntity();
        client.close();
        return (response);
    }

    public <T> List<T> getList(Response response, Class<T> t) {
        List<T> result = null;
        if (response != null) {
            if (response.getStatus() == Constants.RESPONSE_OK_STATUS
                    || response.getStatus() == Response.Status.CREATED.getStatusCode()
                    || response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                LOG.debug("Response status is OK");
                final String body = response.readEntity(String.class);
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    result = mapper.readValue(body, mapper.getTypeFactory().constructCollectionType(List.class, t));
                } catch (final IOException e) {
                    LOG.error("Error occurred while fetching list", e);
                }
            } else if (response.getStatus() == Constants.RESPONSE_NOT_AUTHORIZED) {
                result = new ArrayList<T>();
                try {
                    //doLogout();
                } catch (final Exception ex) {
                    LOG.error("Error occurred while calling TrainingRestDelegate : doLogout() " + ex.getMessage());
                }
            } else {
                LOG.error("Response status is not OK. Status code : " + response.getStatus());
                result = new ArrayList<T>();
            }
        }
        return result;
    }

    public <T> T getObject(Response response, Class<T> t) {
        T result = null;
        if (response != null) {
            if (response.getStatus() == Constants.RESPONSE_OK_STATUS
                    || response.getStatus() == Response.Status.CREATED.getStatusCode()
                    || response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                LOG.debug("Response status is OK");
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    result = mapper.readValue(response.readEntity(String.class),
                            mapper.getTypeFactory().constructType(t));
                } catch (final IOException e) {
                    LOG.error("Error occurred while fetching Object from response", e);
                }
            } else if (response.getStatus() == Constants.RESPONSE_NOT_AUTHORIZED) {
                try {
                    //doLogout();
                } catch (final Exception ex) {
                    LOG.error("Error occurred while calling TrainingRestDelegate : doLogout " + ex.getMessage());
                }
            } else {
                LOG.error("Response status is not OK. Status code : " + response.getStatus());
            }
        }
        return result;
    }

    public int getInt(Response response) {
        int result = 0;
        if (response != null) {
            if (response.getStatus() == Constants.RESPONSE_OK_STATUS
                    || response.getStatus() == Response.Status.CREATED.getStatusCode()
                    || response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
                LOG.debug("Response status is OK");
                final ObjectMapper mapper = new ObjectMapper();
                try {
                    result = mapper.readValue(response.readEntity(String.class),
                            mapper.getTypeFactory().constructType(Integer.class));
                } catch (final IOException e) {
                    LOG.error("Error occurred while fetching Object from response", e);
                }
            } else if (response.getStatus() == Constants.RESPONSE_NOT_AUTHORIZED) {
                try {
                    //doLogout();
                } catch (final Exception ex) {
                    LOG.error("Error occurred while calling TrainingRestDelegate : doLogout() " + ex.getMessage());
                }
            } else {
                LOG.error("Response status is not OK. Status code : " + response.getStatus());
            }
        }
        return result;
    }

    public Response restSharingGet(String url, String accessToken) {
        
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header(Constants.SHARING_HEADER, accessToken)
                .accept(Constants.CONTENT_TYPE).get();

        response.bufferEntity();
        client.close();

        return (response);
    }
    
    public Response restSharingPost(String url, Object entity, String accessToken) {
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header("Content-Type", Constants.CONTENT_TYPE)
                .header(Constants.SHARING_HEADER, accessToken).accept(Constants.CONTENT_TYPE).post(Entity.json(entity));
        response.bufferEntity();
        client.close();
        return (response);
    }
    
    public Response restSharingDelete(String url, String accessToken) {
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header(Constants.SHARING_HEADER, accessToken)
                .accept(Constants.CONTENT_TYPE).delete();
        response.bufferEntity();
        client.close();
        return response;
    }
    
    public Response restSharingPut(String url, String accessToken, Object entity) {
        final Client client = createJaxRsClient();
        final WebTarget target = client.target(url);
        final Response response = target.request().header(Constants.SHARING_HEADER, accessToken)
                .accept(Constants.CONTENT_TYPE).put((Entity.entity(entity, Constants.CONTENT_TYPE)));
        response.bufferEntity();
        client.close();
        return (response);
    }

    
    private Client createJaxRsClient() {
        
        ClientBuilder builder = ClientBuilder.newBuilder();
        
//        if(PropertiesSingletonBean.getProperty("BACKEND_TRUSTSTORE") != null) {
//            
//            String trustStoreFile = System.getProperty("jboss.server.config.dir") + "/" + PropertiesSingletonBean.getProperty("BACKEND_TRUSTSTORE");
//            KeyStore trustStore = null;
//            try {
//                trustStore = KeyStore.getInstance("JKS");
//                trustStore.load(new FileInputStream(trustStoreFile), PropertiesSingletonBean.getProperty("BACKEND_TRUSTSTORE_PASSWORD").toCharArray());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            
//            builder.trustStore(trustStore);
//        }
        
        return(builder.build());
    }
    
}
