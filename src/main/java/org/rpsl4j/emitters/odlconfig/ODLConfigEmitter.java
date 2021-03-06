/*
 * Copyright (c) 2015 Benjamin Roberts, Nathan Kelly, Andrew Maxwell
 * All rights reserved.
 */

package org.rpsl4j.emitters.odlconfig;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;

import org.rpsl4j.emitters.OutputEmitter;
import org.rpsl4j.emitters.rpsldocument.BGPInetRtr;
import org.rpsl4j.emitters.rpsldocument.BGPPeer;
import org.rpsl4j.emitters.rpsldocument.BGPRpslDocument;

import net.ripe.db.whois.common.rpsl.RpslObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Creates and emits an OpenDaylight BGP configuration file.
 * @author Benjamin George Roberts
 */
public class ODLConfigEmitter implements OutputEmitter {
	private static final String TEMPLATE_RESOURCE = "mustache/odlconfig/ODLConfigEmitter.mustache";
	Set<BGPInetRtr> speakerSet;
	Set<BGPPeer> peerSet;
	ODLReconnectStrategy reconnectStrategy = new ODLReconnectStrategy();
	
	final static Logger log = LoggerFactory.getLogger(ODLConfigEmitter.class);

	private static final Map<String, String> argumentList = new HashMap<String, String>();
	
	/**
	 * Static initialiser to populate map of emitter arguments.
	 * All arguments are passed to ODLReconnectStrategy, so we add one for each of its params.
	 */
	static {
		argumentList.put("BGP_RECONNECT_SLEEP_MIN", "Minimum time to wait between BGP reconnect attempts (ms, default: 1000)");
		argumentList.put("BGP_RECONNECT_SLEEP_MAX", "Maximum time to wait between BGP reconnect attempts (ms, default: 180,000)");
		argumentList.put("BGP_RECONNCET_CONNECT_TIME", "Length of connection attempt (ms, default: 5000)");
		argumentList.put("BGP_RECONNCET_SLEEP_FACTOR", "Factor to increase sleep time by on successive reconnect attempts (decimal, default: 2.0)");
	}
	
	@Override
	public String emit(Set<RpslObject> objects) {	
		BGPRpslDocument doc = new BGPRpslDocument(objects);
		
		//Generate the speaker and autnum sets
		speakerSet = doc.getInetRtrSet();
		
		//Generate the peer set
		peerSet = doc.getPeerSet();
		
		//Instantiate the template engine and write the config
		Mustache templateRenderer = (new DefaultMustacheFactory()).compile(TEMPLATE_RESOURCE);
		StringWriter templateWriter = new StringWriter();

		templateRenderer.execute(templateWriter, this);
		templateWriter.flush();

		return templateWriter.toString();
	}

	@Override
	public void setArguments(Map<String, String> arguments) {
		//Update the reconnect strategy using provided arguments
		reconnectStrategy = new ODLReconnectStrategy(arguments);
	}

	@Override
	public Map<String, String> validArguments() {
		return new HashMap<String, String>(argumentList);
	}
}
