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

import java.util.jar.Manifest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Constants;
import org.osgi.test.cases.webcontainer.util.ConstantsUtil;
import org.osgi.test.cases.webcontainer.util.WebContainerTestBundleControl;
import org.osgi.test.cases.webcontainer.util.validate.BundleManifestValidator;

/**
 * @version $Rev$ $Date$
 *
 *          test able to access BundleContext from ServletContext and use the
 *          OSGi log service.
 */
public class AccessBundleContextTest extends WebContainerTestBundleControl {
    private static final String TW5_SYMBOLIC_NAME = "tw5-accessbundle-test";

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        super.prepare("/tw5");

        // install + start the war file
        System.out.println("install war file: tw5.war at context path " + this.warContextPath);
        this.options.put(Constants.IMPORT_PACKAGE, IMPORTS_OSGI_FRAMEWORK);
        this.options.put(Constants.BUNDLE_SYMBOLICNAME, TW5_SYMBOLIC_NAME);
        String loc = super.getWarURL("tw5.war", this.options);
        if (this.debug) {
            System.out.println("bundleName to be passed into installBundle is " + loc);
        }
        this.b = installBundle(loc, true);

        // make sure we don't run tests until the servletcontext is registered with service registry
        boolean register = super.checkServiceRegistered(this.warContextPath);
        assertThat(register).as("the ServletContext should be registered").isTrue();
    }

	@AfterEach
	public void tearDown() throws Exception {
		super.tearDown();
	}

    /*
     * set deployOptions to null to rely on the web container service to
     * generate the manifest
     */
    @Test
    public void testBundleManifest() throws Exception {
		Manifest originalManifest = super.getManifest("/tw5.war");
        BundleManifestValidator validator = new BundleManifestValidator(this.b,
                originalManifest, this.options, this.debug);
        validator.validate();
    }

    /*
     * test home page
     */
    @Test
    public void testHome() throws Exception {
        final String request = this.warContextPath + "/";
        String response = super.getResponse(request);
        super.checkTW5HomeResponse(response);
    }

	@Test
	public void testContext001() throws Exception {
        final String request = this.warContextPath
                + "/BundleContextTestServlet";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(response.indexOf("BundleContextTestServlet")).isGreaterThan(0);
        assertThat(response.indexOf(Constants.BUNDLE_SYMBOLICNAME + ": "
                + b.getSymbolicName())).isGreaterThan(0);
        assertThat(response.indexOf("null")).isEqualTo(-1);
    }

	@Test
	public void testContext002() throws Exception {
        final String request = this.warContextPath
                + "/BundleContextTestServlet?log=2";
        String response = super.getResponse(request);
        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(response.indexOf("BundleContextTestServlet")).isGreaterThan(0);
        assertThat(response.indexOf("Bundle-Id: " + b.getBundleId())).isGreaterThan(0);
        assertThat(response.indexOf("null")).isEqualTo(-1);
    }

	@Test
	public void testContext003() throws Exception {
        final String request = this.warContextPath
                + "/BundleContextTestServlet?log=3";
        String response = super.getResponse(request);

        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(response.indexOf("BundleContextTestServlet")).isGreaterThan(0);
        assertThat(response.indexOf("Bundle-LastModified: "
                + b.getLastModified())).isGreaterThan(0);
        assertThat(response.indexOf("null")).isEqualTo(-1);
    }

	@Test
	public void testContext004() throws Exception {
        final String request = this.warContextPath
                + "/BundleContextTestServlet?log=4";
        String response = super.getResponse(request);

        // check if content of response is correct
        System.out.println("verify content of response is correct");
        assertThat(response.indexOf("BundleContextTestServlet")).isGreaterThan(0);
        assertThat(response.indexOf(Constants.BUNDLE_VERSION + ": "
                + b.getVersion().toString())).isGreaterThan(0);
        assertThat(response.indexOf("null")).isEqualTo(-1);
    }

    /*
     * test ClasspathTestServlet
     */
    @Test
    public void testClasspassServlet() throws Exception {
        final String request = this.warContextPath
        + "/ClasspathTestServlet";
        String response = super.getResponse(request);
        assertThat(response).as("checking response content").isEqualTo("<html><head><title>ClasspathTestServlet</title></head><body>"
                + ConstantsUtil.ABLEGETLOG + "<br/>" +  ConstantsUtil.ABLEGETSIMPLEHELLO + "<br/></body></html>");
    }

}
