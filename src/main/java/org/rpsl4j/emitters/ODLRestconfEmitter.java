/*
 * Copyright (c) 2015 Benjamin Roberts, Nathan Kelly, Andrew Maxwell
 * All rights reserved.
 */

package org.rpsl4j.emitters;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Created by benjamin on 21/07/15.
 */

public abstract class ODLRestconfEmitter implements OutputEmitter {

    String  RESTCONF_ADDRESS        = "127.0.0.1",
    		RESTCONF_USERNAME   = null,
          	RESTCONF_PASSWORD   = null;
    int    	RESTCONF_PORT       = 8181;
    private static final Set<Header>        httpClientHeaders = new HashSet<Header>();

    private HttpClient httpClient = getHttpClient();

    static {
        httpClientHeaders.add(new BasicHeader("Content-Type", "application/xml"));
    }

    @Override
    public void setArguments(Map<String, String> arguments) {
        for(Entry<String, String> argument : arguments.entrySet()) {
            switch (argument.getKey()) {
                case "RESTCONF_ADDRESS":
                    RESTCONF_ADDRESS = argument.getValue();
                    break;
                case "RESTCONF_PORT":
                    RESTCONF_PORT = Integer.parseInt(argument.getValue());
                    break;
                case "RESTCONF_USERNAME":
                    RESTCONF_USERNAME = argument.getValue();
                    break;
                case "RESTCONF_PASSWORD":
                    RESTCONF_PASSWORD = argument.getValue();
                    break;
                default:
                    System.err.println("Unknown emitter argument: " + argument.getKey());
                    break;
            }
        }

        //Refresh client with new details:
        httpClient = getHttpClient();
    }

    /**
     * Build a {@link CredentialsProvider} containing (if configured) the restconf username and password
     * @return credential provider to auth restconf requests with
     */
    CredentialsProvider getCredentialsProvider() {
        CredentialsProvider cp = new BasicCredentialsProvider();
        if(RESTCONF_USERNAME != null && RESTCONF_PASSWORD != null) {
            cp.setCredentials(
                    new AuthScope(RESTCONF_ADDRESS, RESTCONF_PORT),
                    new UsernamePasswordCredentials(RESTCONF_USERNAME, RESTCONF_PASSWORD));
        }
        return cp;
    }

    /**
     * Build a {@link HttpClient} instance with the headers and configured credentials required by restconf
     * @return a restconf capable {@HttpClient}
     */
    HttpClient getHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();

        builder.setDefaultHeaders(httpClientHeaders).
                setDefaultCredentialsProvider(getCredentialsProvider());

        return builder.build();

    }

    /**
     * Execute a HTTP request to the endpoint on the configured restconf instance
     * @param request Request to send
     * @return response of processed request
     * @throws IOException
     */
    protected HttpResponse executeHttpRequest(HttpRequest request) throws IOException {
        try {
            return httpClient.execute(
                    new HttpHost(RESTCONF_ADDRESS, RESTCONF_PORT),
                    request);
        } catch (ClientProtocolException e) {
            throw new IOException(e);
        }
    }
}
