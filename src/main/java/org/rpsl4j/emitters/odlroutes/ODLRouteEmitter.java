/*
 * Copyright (c) 2015 Benjamin Roberts, Nathan Kelly, Andrew Maxwell
 * All rights reserved.
 */

package org.rpsl4j.emitters.odlroutes;

import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import net.ripe.db.whois.common.rpsl.RpslObject;

import org.rpsl4j.emitters.OutputEmitter;
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
public class ODLRouteEmitter implements OutputEmitter {
	private static String TARGET_PEER = null;
	private static final String TEMPLATE_RESOURCE = "mustache/ODLRouteEmitter.mustache"; 
	private static Mustache tempateRenderer = new DefaultMustacheFactory().compile(TEMPLATE_RESOURCE);
	
	@Override
	public String emit(Set<RpslObject> objects) {
		StringWriter outputWriter = new StringWriter();
		BGPRpslDocument doc = new BGPRpslDocument(objects);
		
		//Run emitter for each peer
		for(BGPPeer peer : doc.getPeerSet()) {
			//If peer provided, ignore non matching ones
			if(TARGET_PEER != null && !peer.getName().equals(TARGET_PEER))
				continue;
			
			tempateRenderer.execute(outputWriter, peer);
		}
		
		return outputWriter.toString();
		
	}

	@Override
	public void setArguments(Map<String, String> arguments) {
		if(arguments.containsKey("TARGET_PEER"))
			TARGET_PEER = arguments.get("TARGET_PEER");
		
	}
}
