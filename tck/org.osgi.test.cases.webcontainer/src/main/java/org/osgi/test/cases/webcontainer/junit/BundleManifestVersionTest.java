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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.test.cases.webcontainer.util.ManifestHeadersTestBundleControl;

/**
 * @version $Rev$ $Date$
 * 
 *          test Bundle-ManifestVersion manifest header processed correctly with
 *          various scenarios
 */
public class BundleManifestVersionTest extends ManifestHeadersTestBundleControl {

    private Map<String, Object> createOptions(String version, String cp) {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put(Constants.BUNDLE_MANIFESTVERSION, version);
        options.put(WEB_CONTEXT_PATH, cp);
        return options;
    }
    
    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    @Test
    public void testBundleManifestVersion001() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw1");
        this.b = super.installWar(options, "tw1.war", false);
        generalHeadersTest(options, "tw1.war", false, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion.  test case insensitive
     */
    @Test
    public void testBundleManifestVersion001_1() throws Exception {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put("BUNDLE-MANIFESTVERSION", MANIFESTVERSION1);
        options.put(WEB_CONTEXT_PATH, "/tw1");
        this.b = super.installWar(options, "tw1.war", false);
        options.remove("BUNDLE-MANIFESTVERSION");
        options.put(Constants.BUNDLE_MANIFESTVERSION, MANIFESTVERSION1);
        generalHeadersTest(options, "tw1.war", false, this.b);
    }
    
    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    @Test
    public void testBundleManifestVersion004() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw4");
        this.b = super.installWar(options, "tw4.war", false);
        super.generalHeadersTest(options, "tw4.war", false, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    @Test
    public void testBundleManifestVersion005() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1,
                "/tw5");
        try {
            this.b = super.installWar(options, "wmtw5.war", false);
            fail("install bundle should fail");
        } catch (BundleException e){
            // expected since this is a bundle
        }
        
        assertThat(super.ableAccessPath("/tw5")).as("should not be able to access page").isFalse();

    }
    
    /*
     * verify valid deployOptions overwrite original manifest
     * Bundle-ManifestVersion
     */
    @Test
    public void testBundleManifestVersion006() throws Exception {
        final Map<String, Object> options = createOptions(MANIFESTVERSION1, "/tw1");
        try {
            this.b = super.installWar(options, "wmtw1.war", false);
            fail("install bundle should fail");
        } catch (BundleException e){
            // expected since this is a bundle
        }
        
        assertThat(super.ableAccessPath("/tw1")).as("should not be able to access page").isFalse();

    }
    
    /*
     * verify null bundle-manifestVersion
     */
    @Test
    public void testBundleManifestVersion011() throws Exception {
        final Map<String, Object> options = createOptions(null, "/tw5");
        this.b = super.installWar(options, "wmtw5.war", false);
        
        assertThat(super.ableAccessPath("/tw5")).as("should not be able to access page").isFalse();
    }

    /*
     * verify invalid Bundle-ManifestVersion in deployOptions
     */
    @Test
    public void testBundleManifestVersionError001() throws Exception {
        // specify install options
        final Map<String, Object> options = createOptions(MANIFESTVERSION3, "/tw1");
        // install the war file
        System.out.println("install war file: tw1.war at context path /tw1");
        try {
            this.b = installBundle(super.getWarURL("tw1.war", options), true);
            fail("bundle install should fail: " + Constants.BUNDLE_MANIFESTVERSION
                    + " = " + MANIFESTVERSION3);
        } catch (BundleException be) {
            // expected unless the framework supports Bundle-ManifestVersion
            // greater than 2
            System.out.println("this is expected if the framework doesn't support the "
                    + Constants.BUNDLE_MANIFESTVERSION + " = " + MANIFESTVERSION3);
        }
        assertThat(this.b).as("Bundle b should be null").isNull();

        // test unable to access /tw2 yet as it is not started
        assertThat(super
                .ableAccessPath("/tw1/")).as("should not be able to access /tw1").isFalse();
    }

    /*
     * verify invalid Bundle-ManifestVersion in deployOptions
     */
    @Test
    public void testBundleManifestVersionError002() throws Exception {
        // specify install options
        final Map<String, Object> options = createOptions(MANIFESTVERSION3, "/tw4");
        // install the war file
        System.out.println("install and start war file: tw4.war at context path /tw4");
        try {
            this.b = installBundle(super.getWarURL("tw4.war", options), false);
            fail("bundle install should fail: " + Constants.BUNDLE_MANIFESTVERSION
                    + " = " + MANIFESTVERSION3);
        } catch (BundleException be) {
            // expected;
        }
        assertThat(this.b).as("Bundle b should be null").isNull();

        // test unable to access /tw3 yet as it is not started
        assertThat(super
                .ableAccessPath("/tw4/")).as("should not be able to access /tw4").isFalse();
    }
}
