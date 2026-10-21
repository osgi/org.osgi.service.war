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
 *          test Bundle-Version manifest header processed correctly with various
 *          scenarios
 */
public class BundleVersionTest extends ManifestHeadersTestBundleControl {

    private Map<String, Object> createOptions(String version, String cp) {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put(Constants.BUNDLE_VERSION, version);
        options.put(WEB_CONTEXT_PATH, cp);
        return options;
    }
    
    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     */
    @Test
    public void testBundleVersion001() throws Exception {
        final Map<String, Object> options = createOptions(VERSION1, "/tw1");
        this.b = super.installWar(options, "tw1.war", false);
        super.generalHeadersTest(options, "tw1.war", false, this.b);
    }
    
    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     * test case insensitive
     */
    @Test
    public void testBundleVersion001_1() throws Exception {
        final Map<String, Object> options = new HashMap<String, Object>();
        options.put("bundle-version", VERSION1);
        options.put(WEB_CONTEXT_PATH, "/tw1");
        this.b = super.installWar(options, "tw1.war", false);
        options.remove("bundle-version");
        options.put(Constants.BUNDLE_VERSION, VERSION1);
        super.generalHeadersTest(options, "tw1.war", false, this.b);
    }


    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     */
    @Test
    public void testBundleVersion004() throws Exception {
        final Map<String, Object> options = createOptions(VERSION3, "/tw4");
        this.b = super.installWar(options, "tw4.war", true);
        super.generalHeadersTest(options, "tw4.war", true, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     */
    @Test
    public void testBundleVersion005() throws Exception {
        final Map<String, Object> options = createOptions(VERSION3, "/tw5");
        this.b = super.installWar(options, "tw5.war", false);
        super.generalHeadersTest(options, "tw5.war", false, this.b);
    }

    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     */
    @Test
    public void testBundleVersion006() throws Exception {
        final Map<String, Object> options = createOptions(VERSION1, "/tw1");
        try {
            this.b = super.installWar(options, "wmtw1.war", false);
            fail("install bundle should fail");
        } catch (BundleException e){
            // expected since this is a bundle
        }
        assertThat(super.ableAccessPath("/tw1")).as("should not be able to access page").isFalse();

    }

    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     */
    @Test
    public void testBundleVersion009() throws Exception {
        final Map<String, Object> options = createOptions(VERSION3, "/tw4");
        try {
            this.b = super.installWar(options, "wmtw4.war", true);
            fail("install bundle should fail");
        } catch (BundleException e){
            // expected since this is a bundle
        }
        assertThat(super.ableAccessPath("/tw4")).as("should not be able to access page").isFalse();

    }

    /*
     * verify valid deployOptions overwrite original manifest Bundle-Version
     */
    @Test
    public void testBundleVersion010() throws Exception {
        final Map<String, Object> options = createOptions(VERSION3, "/tw5");
        try {
            this.b = super.installWar(options, "wmtw5.war", false);
            fail("install bundle should fail");
        } catch (BundleException e){
            // expected since this is a bundle
        }
        assertThat(super.ableAccessPath("/tw5")).as("should not be able to access page").isFalse();

    }
    
    /*
     * error case, when version specified by deployer is invalid
     */
    @Test
    public void testBundleVersionError001() throws Exception {
        // specify install options
        final Map<String, Object> options = createOptions(VERSION4, "/tw4");
        // install the war file
        System.out.println("install war file: tw4.war at context path /tw4");
        // may not be able to installBundle correctly if version is specified
        // improperly??
        try {
            this.b = installBundle(super.getWarURL("tw4.war", options), false);
            fail("should be getting install BundleException as version format is invalid");
        } catch (BundleException be) {
            // expected
        }
        assertThat(this.b).as("Bundle b should be null").isNull();

        // test unable to access /tw4 yet as it is not installed
        assertThat(super
                .ableAccessPath("/tw4/")).as("should not be able to access /tw4").isFalse();
    }

    /*
     * error case, when version specified by deployer is invalid
     */
    @Test
    public void testBundleVersionError002() throws Exception {
        // specify install options
        final Map<String, Object> options = createOptions(VERSION5, "/tw5");
        // install the war file
        System.out.println("install war file: tw5.war at context path /tw5");
        // may not be able to installBundle correctly if version is specified
        // improperly
        try {
            this.b = installBundle(super.getWarURL("tw5.war", options), false);
            fail("should be getting install BundleException as version format is invalid");
        } catch (BundleException be) {
            // expected
        }
        assertThat(this.b).as("Bundle b should be null").isNull();

        // test unable to access /tw5 yet as it is not installed
        assertThat(super
                .ableAccessPath("/tw5/")).as("should not be able to access /tw5").isFalse();
    }
}
