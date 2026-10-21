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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.test.cases.webcontainer.util.ConstantsUtil;
import org.osgi.test.cases.webcontainer.util.Dispatcher;
import org.osgi.test.cases.webcontainer.util.WebContainerTestBundleControl;
import org.osgi.test.cases.webcontainer.util.validate.BundleManifestValidator;

/**
 * @version $Rev$ $Date$
 * 
 *          test class for tw6.war which is same as tw1.war except it is signed
 */
public class TW6Test extends WebContainerTestBundleControl {
	
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        super.prepare("/tw6");

        // install + start the war file
        String loc = super.getWarURL("tw6.war", this.options);
        if (this.debug) {
            System.out.println("bundleName to be passed into installBundle is " + loc);	
        }
        this.b = installBundle(loc, true);
        /*URL url = new URL(loc);
        URLConnection conn = url.openConnection();
        InputStream is = conn.getInputStream();
        this.b = getContext().installBundle(loc, is);*/
        // make sure we don't run tests until the servletcontext is registered with service registry
        boolean register = super.checkServiceRegistered(this.warContextPath);
        assertThat(register).as("the ServletContext should be registered").isTrue();
    }

    /*
     * set deployOptions to null to rely on the web container service to
     * generate the manifest
     */
    @Test
    public void testBundleManifest() throws Exception {
		Manifest originalManifest = super.getManifest("/tw6.war");
        BundleManifestValidator validator = new BundleManifestValidator(this.b,
                originalManifest, this.options, this.debug);
        validator.validate();
    }
    
    /**
     * this test verifies the remove of the .RSA and .SF signature file by the url handler
     * @throws Exception
     */
    @Test
    public void testRemoveSignedFiles() throws Exception {
        assertThat(this.b.getEntry("/META-INF/MANIFEST.MF")).as("/META-INF/MANIFEST.MF should be exist").isNotNull();
        assertThat(this.b.getEntry("/META-INF/TEST.RSA")).as("/META-INF/TEST.RSA should be removed by web url handler").isNull();
        assertThat(this.b.getEntry("/META-INF/TEST.SF")).as("/META-INF/TEST.SF should be removed by web url handler").isNull();
    }
    
    /**
     * this test verifies the removal of the manifest name sections that 
     * contain the resource’s check sums 
     * @throws Exception
     */
    @Test
    public void testRemovedSignedPortionInManifest() throws Exception {
        URL url = this.b.getEntry("/META-INF/MANIFEST.MF");
        InputStream is = null;
        
        try {
            is = url.openStream();
            assertThat(is).as("is should not be null").isNotNull();
            Manifest mf = new Manifest(is);
            Attributes attrs = mf.getAttributes("welcome.jsp");
            String val = null;
            if (attrs != null) {
				val = attrs.getValue("SHA-256-Digest");
            }
            assertThat(val).isNull();  
            
            // verify signature did exist in the original war.
            mf = super.getManifestFromWarName("tw6.war");
			val = mf.getAttributes("welcome.jsp").getValue("SHA-256-Digest");
            assertThat(val).isNotNull();  
        } finally {
            if (is != null) {
                is.close();
            }
        }
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
