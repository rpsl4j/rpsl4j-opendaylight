package org.rpsl4j.emitters;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.ripe.db.whois.common.rpsl.RpslObject;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.junit.Test;

public class ODLRestconfEmitterTest {

	/**
	 * Dummy ODLRestconfEmitter so that tests can be made against non abstract instance	 *
	 */
	private static class DummyEmitter extends ODLRestconfEmitter {
		public String emit(Set<RpslObject> objects) {
			return null;
		}
	}
	
	@Test
	public void credentialProviderTest() {
		ODLRestconfEmitter em = new DummyEmitter();
		Map<String, String> argMap = new HashMap<String, String>();
		AuthScope defaultScope = new AuthScope(em.RESTCONF_ADDRESS, em.RESTCONF_PORT);
		CredentialsProvider cp;
		Credentials cr;
		
		//Check that no credentials present without user/pass set
		cp = em.getCredentialsProvider();
		cr = cp.getCredentials(defaultScope);
		assertNull("No credentials should be present if username/password not set", cr);
					
		//Add username and password
		argMap.put("RESTCONF_USERNAME", "user");
		argMap.put("RESTCONF_PASSWORD", "password");
		em.setArguments(argMap);
		cp = em.getCredentialsProvider();
		cr = cp.getCredentials(defaultScope);
		assertEquals("username should match provided argument", "user", cr.getUserPrincipal().getName());
		assertEquals("password  should match provided argument", "password", cr.getPassword());
		
		argMap.put("RESTCONF_ADDRESS", "127.0.0.2");
		argMap.put("RESTCONF_PORT", "8182");
		em.setArguments(argMap);
		cp = em.getCredentialsProvider();
		cr = cp.getCredentials(new AuthScope("127.0.0.2", 8182));
		assertEquals("username should match provided argument given custom restconf host", "user", cr.getUserPrincipal().getName());
		assertEquals("password  should match provided argument given custom restconf host", "password", cr.getPassword());
		assertNull("credentials shouldn't exist for host other than provided restconf", cp.getCredentials(defaultScope));
		
	}
}
