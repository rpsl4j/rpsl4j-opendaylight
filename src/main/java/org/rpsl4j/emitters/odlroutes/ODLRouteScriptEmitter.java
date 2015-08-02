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

public class ODLRouteScriptEmitter implements OutputEmitter {
	private static final String TEMPLATE_RESOURCE = "mustache/odlroutes/ODLRouteScriptEmitter.mustache";
	private static Mustache tempateRenderer = new DefaultMustacheFactory().compile(TEMPLATE_RESOURCE);
	
	static Set<BGPPeer> peers;
	
	@Override
	public String emit(Set<RpslObject> objects) {
		StringWriter outputWriter = new StringWriter();
		BGPRpslDocument doc = new BGPRpslDocument(objects);
		peers = doc.getPeerSet();
		
		tempateRenderer.execute(outputWriter, this);
		
		return outputWriter.toString();
		
	}

	@Override
	public void setArguments(Map<String, String> arguments) {		
	}
}
