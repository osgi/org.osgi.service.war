/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
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
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/
package org.osgi.test.cases.webcontainer.junit;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.test.cases.webcontainer.util.EventFactory;
import org.osgi.test.cases.webcontainer.util.OSGiTestCaseProperties;
import org.osgi.test.cases.webcontainer.util.WebContainerTestBundleControl;

/**
 * @version $Rev$ $Date$
 *
 *          EventTest to verify Events are published by the web extender
 *          bundle at certain situations defined by rfc 66
 */
public class EventTest extends WebContainerTestBundleControl {
    Bundle eventhandler;
    private final int WAITCOUNT = 10;
    private final String EXTENDER_BUNDLE = "extender.bundle";
    private final String EXTENDER_BUNDLE_ID = "extender.bundle.id";
    private final String EXTENDER_BUNDLE_VERSION = "extender.bundle.version";
    private final String EXTENDER_BUNDLE_SYMBOLICNAME = "extender.bundle.symbolicName";
    private final static String COLLISION = "collision";
    private final static String COLLISION_BUNDLES = "collision.bundles";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        super.prepare("/tw1");
        this.options.put(Constants.BUNDLE_SYMBOLICNAME, "org.osgi.test.cases.webcontainer.tw1");

        // install the war file
        System.out.println("install war file: tw1.war at context path " + this.warContextPath);
        String loc = super.getWarURL("tw1.war", this.options);
        if (this.debug) {
            System.out.println("bundleName to be passed into installBundle is " + loc);
        }
        super.b = installBundle(loc, false);

        // verify event admin service is installed
        System.out.println("verify event admin service is installed.  The tests in this class require event admin service being installed.");
		ServiceReference<EventAdmin> sr = context
				.getServiceReference(EventAdmin.class);
        assertThat(sr).as("EventAdmin service is not available").isNotNull();
        assertThat(context.getService(sr)).isNotNull();

        System.out.println("install & start the test event handler");
        this.eventhandler = context.installBundle(
                getWebServer() + "eventHandler.jar");
        this.eventhandler.start();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        this.eventhandler.uninstall();
    }

    /**
     * this test tests a simple war start/stop with the correct events emitted
     * @throws Exception
     */
    @Test
    public void testEvent001() throws Exception {
        // clear all events in the eventfactory
        //EventFactory.clearEvents();
        //assertEquals("factory should not have any event", 0, EventFactory.getEventSize());

        this.b.start();

        // expect emit of the following events:
        // org/osgi/service/web/DEPLOYING the web extender has spotted the web application bundle and is starting it.
        // org/osgi/service/web/DEPLOYED the web extender has finished starting the web application bundle. Formatted: Bullets and Numbering
        // wait a few seconds to make sure events are delivered.
        int count = 0;
        Event eventPrevious = null;
        Event eventCurrent = null;
        while(eventCurrent == null && count < WAITCOUNT) {
            eventCurrent = EventFactory.getEvent("org.osgi.test.cases.webcontainer.tw1", "org/osgi/service/web/DEPLOYED");
			Thread.sleep(1000 * OSGiTestCaseProperties.getScaling());
            count++;
        }

        assertThat(EventFactory.getEventSize()).as("event factory size should be 2").isEqualTo(2);
        eventPrevious = EventFactory.getEvent("org.osgi.test.cases.webcontainer.tw1", "org/osgi/service/web/DEPLOYING");
        assertThat(eventPrevious).isNotNull();
        assertThat(eventCurrent).isNotNull();

        long startingTime = (Long)eventPrevious.getProperty(EventConstants.TIMESTAMP);
        long startedTime = (Long)eventCurrent.getProperty(EventConstants.TIMESTAMP);

        assertThat(eventPrevious.getTopic()).isEqualTo("org/osgi/service/web/DEPLOYING");
        assertThat((String)eventPrevious.getProperty(EventConstants.BUNDLE_SYMBOLICNAME)).isEqualTo("org.osgi.test.cases.webcontainer.tw1");
        assertThat(eventPrevious.getProperty(EventConstants.BUNDLE_ID)).isEqualTo(this.b.getBundleId());
        assertThat(eventPrevious.getProperty(EventConstants.BUNDLE)).isEqualTo(this.b);
        assertThat(eventPrevious.getProperty(EventConstants.BUNDLE_VERSION)).isEqualTo(this.b.getVersion());
        assertThat((String)eventPrevious.getProperty("context.path")).isEqualTo(this.b.getHeaders().get("Web-ContextPath"));
        assertThat(startingTime).isNotNull();
        assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE)).isNotNull();
        assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE_ID)).isNotNull();
        assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME)).isNotNull();
        assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE_VERSION)).isNotNull();

        assertThat(eventCurrent.getTopic()).isEqualTo("org/osgi/service/web/DEPLOYED");
        assertThat((String)eventCurrent.getProperty(EventConstants.BUNDLE_SYMBOLICNAME)).isEqualTo("org.osgi.test.cases.webcontainer.tw1");
        assertThat(eventCurrent.getProperty(EventConstants.BUNDLE_ID)).isEqualTo(this.b.getBundleId());
        assertThat(eventCurrent.getProperty(EventConstants.BUNDLE)).isEqualTo(this.b);
        assertThat(eventCurrent.getProperty(EventConstants.BUNDLE_VERSION)).isEqualTo(this.b.getVersion());
        assertThat((String)eventPrevious.getProperty("context.path")).isEqualTo(this.b.getHeaders().get("Web-ContextPath"));
        assertThat(startedTime).isNotNull();
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE)).isNotNull();
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_ID)).isNotNull();
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME)).isNotNull();
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_VERSION)).isNotNull();

        // the extender information should be the same

        assertThat(startedTime).isGreaterThanOrEqualTo(startingTime);
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE));
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_ID)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE_ID));
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME));
        assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_VERSION)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE_VERSION));

        eventPrevious = null;
        eventCurrent = null;


        this.b.stop();
        // emit the following events:
        // org/osgi/service/web/UNDEPLOYING the web extender is stopping the web application bundle.
        // org/osgi/service/web/UNDEPLOYED a web extender has stopped the web application bundle.
        // wait a few seconds to make sure events are delivered.
        count = 0;
        while(eventCurrent == null && count < WAITCOUNT) {
        	eventCurrent = EventFactory.getEvent("org.osgi.test.cases.webcontainer.tw1", "org/osgi/service/web/UNDEPLOYED");
			Thread.sleep(1000 * OSGiTestCaseProperties.getScaling());
            count++;
        }
        eventPrevious = EventFactory.getEvent("org.osgi.test.cases.webcontainer.tw1", "org/osgi/service/web/UNDEPLOYING");
        assertThat(eventPrevious).isNotNull();
        assertThat(eventCurrent).isNotNull();
        assertThat(eventPrevious.getTopic()).isEqualTo("org/osgi/service/web/UNDEPLOYING");
        assertThat(eventCurrent.getTopic()).isEqualTo("org/osgi/service/web/UNDEPLOYED");
        assertThat((Long)eventPrevious.getProperty(EventConstants.TIMESTAMP)).isGreaterThanOrEqualTo(startedTime);
        assertThat((Long)eventCurrent.getProperty(EventConstants.TIMESTAMP)).isGreaterThanOrEqualTo((Long)eventPrevious.getProperty(EventConstants.TIMESTAMP));
    }

    /**
     * this test tests a war start failure with correct events emitted.
     * @throws Exception
     */
	@SuppressWarnings("unchecked")
	@Test
	public void testEvent002() throws Exception {
        // start the bundle again and try deploy another bundle that cause a failure
        this.b.start();
        // wait a few seconds to make sure events are delivered.
        int count = 0;
        Event eventPrevious = null;
        Event eventCurrent = null;

        // clear all events in the eventfactory
        EventFactory.clearEvents();
        assertThat(EventFactory.getEventSize()).as("factory should not have any event").isZero();

        Map<String, Object> options = new HashMap<String, Object>();
        options.put(WEB_CONTEXT_PATH, "/tw1");
        options.put(Constants.BUNDLE_SYMBOLICNAME, "org.osgi.test.cases.webcontainer.tw4");

        // install the war file that uses the same WebContextPath
        System.out.println("install war file: tw4.war at context path /tw1");
        Bundle b2 = installBundle(super.getWarURL("tw4.war", options), false);
        try {
            b2.start();
            // emit the following events:
            // org/osgi/service/web/DEPLOYING the web extender has spotted the web application bundle and is starting it.
            // org/osgi/service/web/FAILED - a web extender cannot start the bundle, this will be fired after a DEPLOYING
            // event has been fired if the bundle cannot be started for any reason.
            // wait a few seconds to make sure events are delivered.
            count = 0;
            while(eventCurrent == null && count < WAITCOUNT) {
                eventCurrent = EventFactory.getEvent("org.osgi.test.cases.webcontainer.tw4", "org/osgi/service/web/FAILED");
				Thread.sleep(1000 * OSGiTestCaseProperties.getScaling());
                count++;
            }
            eventPrevious = EventFactory.getEvent("org.osgi.test.cases.webcontainer.tw4", "org/osgi/service/web/DEPLOYING");
            assertThat(eventPrevious).isNotNull();
            assertThat(eventCurrent).isNotNull();

            long startingTime = (Long)eventPrevious.getProperty(EventConstants.TIMESTAMP);
            long failedTime = (Long)eventCurrent.getProperty(EventConstants.TIMESTAMP);

            assertThat(eventPrevious.getTopic()).isEqualTo("org/osgi/service/web/DEPLOYING");
            assertThat((String)eventPrevious.getProperty(EventConstants.BUNDLE_SYMBOLICNAME)).isEqualTo("org.osgi.test.cases.webcontainer.tw4");
            assertThat(eventPrevious.getProperty(EventConstants.BUNDLE_ID)).isEqualTo(b2.getBundleId());
            assertThat(eventPrevious.getProperty(EventConstants.BUNDLE)).isEqualTo(b2);
            assertThat(eventPrevious.getProperty(EventConstants.BUNDLE_VERSION)).isEqualTo(b2.getVersion());
            assertThat((String)eventPrevious.getProperty("context.path")).isEqualTo(b2.getHeaders().get("Web-ContextPath"));
            assertThat(startingTime).isNotNull();
            assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE)).isNotNull();
            assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE_ID)).isNotNull();
            assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME)).isNotNull();
            assertThat(eventPrevious.getProperty(EXTENDER_BUNDLE_VERSION)).isNotNull();

            assertThat(eventCurrent.getTopic()).isEqualTo("org/osgi/service/web/FAILED");
            assertThat((String)eventCurrent.getProperty(EventConstants.BUNDLE_SYMBOLICNAME)).isEqualTo("org.osgi.test.cases.webcontainer.tw4");
            assertThat(eventCurrent.getProperty(EventConstants.BUNDLE_ID)).isEqualTo(b2.getBundleId());
            assertThat(eventCurrent.getProperty(EventConstants.BUNDLE)).isEqualTo(b2);
            assertThat(eventCurrent.getProperty(EventConstants.BUNDLE_VERSION)).isEqualTo(b2.getVersion());
            assertThat((String)eventPrevious.getProperty("context.path")).isEqualTo(b2.getHeaders().get("Web-ContextPath"));
            assertThat(failedTime).isNotNull();
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE)).isNotNull();
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_ID)).isNotNull();
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME)).isNotNull();
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_VERSION)).isNotNull();
            // remove this checking as spec doesn't mandate the exception property to be set.
            //assertNotNull((Throwable)eventCurrent.getProperty(EventConstants.EXCEPTION));
            assertThat(eventCurrent.getProperty(COLLISION)).isNotNull();
            assertThat((String)eventCurrent.getProperty(COLLISION)).isEqualTo("/tw1");
            assertThat(eventCurrent.getProperty(COLLISION_BUNDLES)).isNotNull();
            assertThat(contains((Collection<Long>)eventCurrent.getProperty(COLLISION_BUNDLES), b2.getBundleId())).as("check collision.bundles property contains " + b2.getBundleId()).isTrue();
            assertThat(contains((Collection<Long>)eventCurrent.getProperty(COLLISION_BUNDLES), this.b.getBundleId())).as("check collision.bundles property contains " + this.b.getBundleId()).isTrue();

            // the extender information should be the same
            assertThat(failedTime).isGreaterThanOrEqualTo(startingTime);
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE));
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_ID)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE_ID));
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE_SYMBOLICNAME));
            assertThat(eventCurrent.getProperty(EXTENDER_BUNDLE_VERSION)).isEqualTo(eventPrevious.getProperty(EXTENDER_BUNDLE_VERSION));
        } finally {
            b2.uninstall();
        }
    }

    /**
     * check to see if a list contains the particular bundleId
     * @param bundleIds
     * @param bundlId
     * @return
     */
    private boolean contains(Collection<Long> bundleIds, Long bundleId) {
        for (Long id : bundleIds) {
            if (id.longValue() == bundleId.longValue()) {
                return true;
            }
        }
        return false;
    }
}
