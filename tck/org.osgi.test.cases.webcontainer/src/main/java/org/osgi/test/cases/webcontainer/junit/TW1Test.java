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
import static org.assertj.core.api.Assertions.fail;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.Manifest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.cases.webcontainer.util.ConstantsUtil;
import org.osgi.test.cases.webcontainer.util.Dispatcher;
import org.osgi.test.cases.webcontainer.util.WebContainerTestBundleControl;
import org.osgi.test.cases.webcontainer.util.validate.BundleManifestValidator;

/**
 * @version $Rev$ $Date$
 * 
 *          test class for tw1.war
 */
public class TW1Test extends WebContainerTestBundleControl {
	
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        super.prepare("/tw1");

        // install + start the war file
        String loc = super.getWarURL("tw1.war", this.options);
        if (this.debug) {
            System.out.println("bundleName to be passed into installBundle is " + loc);	
        }
        this.b = installBundle(loc, true);
        
        // make sure we don't run tests until the servletcontext is registered with service registry
        boolean register = super.checkServiceRegistered(this.warContextPath);
        assertThat(register).as("the ServletContext should be registered").isTrue();
    }
    
    @Override
	@AfterEach
	public void tearDown() throws Exception {
        super.tearDown();
        
        // verify tw1.war is cleaned uninstalled and page is not servable
        assertThat(super.ableAccessPath("/tw1")).as("should not be able to access /tw1").isFalse();
    }

    /*
     * set deployOptions to null to rely on the web container service to
     * generate the manifest
     */
    @Test
    public void testBundleManifest() throws Exception {
		Manifest originalManifest = super.getManifest("/tw1.war");
        BundleManifestValidator validator = new BundleManifestValidator(this.b,
                originalManifest, this.options, this.debug);
        validator.validate();
    }

    /*
     * test home page
     */
    @Test
    public void testBasic001() throws Exception {
        final String request = this.warContextPath + "/";
        String response = super.getResponse(request);
        super.checkTW1HomeResponse(response);
    }

    @Test
    public void testBasic002() throws Exception {
        final String request = this.warContextPath + "/BasicTest";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(ConstantsUtil.BASICTESTWAR1).isEqualTo(response);
    }

    @Test
    public void testBasic003() throws Exception {
        final String request = this.warContextPath + "/error404.html";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(ConstantsUtil.ERROR404HTML).isEqualTo(response);
    }

    @Test
    public void testBasic004() throws Exception {
        final String request = this.warContextPath + "/ErrorTest?target=html";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(ConstantsUtil.ERROR404HTML).isEqualTo(response);
    }

    @Test
    public void testBasic005() throws Exception {
        final String request = this.warContextPath + "/ErrorTest";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(response.indexOf(ConstantsUtil.ERROR404JSP)).isGreaterThan(-1);
    }

    @Test
    public void testBasic006() throws Exception {
        final String request = this.warContextPath + "/aaa";
        final URL url = Dispatcher.createURL(request, this.server);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            assertThat(404).isEqualTo(conn.getResponseCode());
            assertThat(conn.getContentType().indexOf("text/html")).isGreaterThan(-1);
            String response = Dispatcher.dispatch(conn);
            if (this.debug) {
                System.out.println(response);
            }
            fail("should be getting an exception");
        } catch (IOException e) {
            // ignore - this can be expected
        } finally {
            conn.disconnect();
        }
    }

    @Test
    public void testBasic007() throws Exception {
        final String request = this.warContextPath + "/image.html";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(ConstantsUtil.IMAGEHTML).isEqualTo(response);
    }

    @Test
    public void testBasic008() throws Exception {
		final String request = this.warContextPath + "/images/osgi.png";
        final URL url = Dispatcher.createURL(request, this.server);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            if (this.debug) {
                System.out.println(conn.getContentType());
            }
            assertThat(200).isEqualTo(conn.getResponseCode());
			assertThat("image/png").isEqualTo(conn.getContentType());
        } finally {
            conn.disconnect();
        }
    }
    
    @Test
    public void testWelcomeJSP() throws Exception {
        super.checkWelcomeJSP(this.warContextPath);
    }
    
    // test unable to serve static page from WEB-INF dir
    @Test
    public void testUnableServePage001() throws Exception {
        final String request = this.warContextPath + "/WEB-INF/non-serving.html";
        System.out.println("verify not able to access the page");
        assertThat(super.ableAccessPath(request)).isFalse();
    }
    
    // test unable to serve static page from META-INF dir
    @Test
    public void testUnableServePage002() throws Exception {
        final String request = this.warContextPath + "/META-INF/non-serving.html";
        System.out.println("verify not able to access the page");
        assertThat(super.ableAccessPath(request)).isFalse();
    }
    
    // test unable to serve static page from OSGI-INF dir
    @Test
    public void testUnableServePage003() throws Exception {
        final String request = this.warContextPath + "/OSGI-INF/non-serving.html";
        System.out.println("verify not able to access the page");
        assertThat(super.ableAccessPath(request)).isFalse();
    }
    
    // test unable to serve static page from OSGI-OPT dir
    @Test
    public void testUnableServePage004() throws Exception {
        final String request = this.warContextPath + "/OSGI-OPT/non-serving.html";
        System.out.println("verify not able to access the page");
        assertThat(super.ableAccessPath(request)).isFalse();
    }
}
