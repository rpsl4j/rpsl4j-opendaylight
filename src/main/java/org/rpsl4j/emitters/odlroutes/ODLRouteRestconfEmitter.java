/*
 * Copyright (c) 2015 Benjamin Roberts, Nathan Kelly, Andrew Maxwell
 * All rights reserved.
 */

package org.rpsl4j.emitters.odlroutes;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.ripe.db.whois.common.rpsl.RpslObject;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.rpsl4j.emitters.ODLRestconfEmitter;
import org.rpsl4j.emitters.rpsldocument.BGPPeer;
import org.rpsl4j.emitters.rpsldocument.BGPRpslDocument;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

/**
 * Emits the body for `ipv4-routes` put/post requests used to populate ODL route tables.
 * By default will output one for each peer, but the TARGET_PEER setting can be passed;
 * filtering the emitter down to a single peer at a time
 * @author Benjamin George Roberts
 */
public class ODLRouteRestconfEmitter extends ODLRestconfEmitter {
	private static String TARGET_PEER = null;
	private static final String TEMPLATE_RESOURCE = "mustache/odlroutes/ODLRouteRestconfEmitter.mustache";
	private static Mustache templateRenderer = new DefaultMustacheFactory().compile(TEMPLATE_RESOURCE);

    private static final String RIB_TABLE_FORMAT_STRING = "/restconf/config/bgp-rib:application-rib/" +
            "%s-app-rib/tables/bgp-types:ipv4-address-family/bgp-types:unicast-subsequent-address-family/";

    @Override
	public Map<String, String> validArguments() {
    	Map<String, String> superArgs = super.validArguments();
    	superArgs.put("TARGET_PEER", "Name of peer to filter route injection to (default: nil)");
    	return superArgs;
    }
 
	@Override
	public String emit(Set<RpslObject> objects) {
		BGPRpslDocument doc = new BGPRpslDocument(objects);
		
		//Run emitter for each peer
		for(BGPPeer peer : doc.getPeerSet()) {
			//If peer provided, ignore non matching ones
			if(TARGET_PEER != null && !peer.getName().equals(TARGET_PEER))
				continue;

            StringWriter outputWriter = new StringWriter();
            templateRenderer.execute(outputWriter,peer);

            String  endpointURL = String.format(RIB_TABLE_FORMAT_STRING, peer.getName()),
                    payload     = outputWriter.toString();


            try {
                //Deleting old route table
                //TODO deal with partial updates
                executeHttpRequest(new HttpDelete(endpointURL));

                //Post new route table
                HttpPost request = new HttpPost(endpointURL);
                request.setEntity(new StringEntity(payload));

                HttpResponse r = executeHttpRequest(request);

                //Check for failure
                if(r.getStatusLine().getStatusCode() != 204) {
                    System.err.println(String.format("Posting routes failed (%d)", r.getStatusLine().getStatusCode()));

                    if(r.getEntity() != null)
                        r.getEntity().writeTo(System.out);
                }
            } catch(IOException e) {
                //TODO separate error handling
                System.err.println(String.format("Failed to inject %s routes: %s", peer.getName(), e.getMessage()));
                e.printStackTrace();
            }

		}
        //TODO return something
        return "";
	}

	@Override
	public void setArguments(Map<String, String> paramArguments) {
        //Make our own copy so we can pass it to superclass
        Map<String, String> arguments = new HashMap<>(paramArguments);

		if(arguments.containsKey("TARGET_PEER")) {
            TARGET_PEER = arguments.get("TARGET_PEER");
            arguments.remove("TARGET_PEER");
        }

        super.setArguments(arguments);
	}
}
