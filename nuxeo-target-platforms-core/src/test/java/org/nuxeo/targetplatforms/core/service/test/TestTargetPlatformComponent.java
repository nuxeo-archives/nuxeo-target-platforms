/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and others.
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
 * Contributors:
 *     Anahide Tchertchian
 */
package org.nuxeo.targetplatforms.core.service.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.runtime.model.RuntimeContext;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.RuntimeFeature;
import org.nuxeo.runtime.test.runner.RuntimeHarness;
import org.nuxeo.targetplatforms.api.TargetPackage;
import org.nuxeo.targetplatforms.api.TargetPlatform;
import org.nuxeo.targetplatforms.api.service.TargetPlatformService;
import org.nuxeo.targetplatforms.core.service.DirectoryUpdater;

/**
 * @since 5.7.1
 */

@RunWith(FeaturesRunner.class)
@Features(RuntimeFeature.class)
@Deploy({ "org.nuxeo.runtime.jtajca", "org.nuxeo.runtime.datasource", "org.nuxeo.ecm.core",
        "org.nuxeo.ecm.core.schema", "org.nuxeo.targetplatforms.core", "org.nuxeo.ecm.directory",
        "org.nuxeo.ecm.directory.sql" })
@LocalDeploy({ "org.nuxeo.targetplatforms.core:OSGI-INF/test-datasource-contrib.xml",
        "org.nuxeo.targetplatforms.core:OSGI-INF/test-targetplatforms-contrib.xml" })
public class TestTargetPlatformComponent {

    private static final String BUNDLE = "org.nuxeo.targetplatforms.core";

    @Inject
    protected TargetPlatformService service;

    @Inject
    protected RuntimeHarness harness;

    @Test
    public void testPlatformRegistration() {
        TargetPlatform tp = service.getTargetPlatform("cap-5.8");
        assertNotNull(tp);
        assertTrue(tp.isEnabled());
    }

    @Test
    public void testPackageRegistration() {
        TargetPackage tp = service.getTargetPackage("nuxeo-dm-5.8");
        assertNotNull(tp);
        assertTrue(tp.isEnabled());
    }

    @Test
    public void testOverrideDirectoryRegistration() throws Exception {
        assertEquals(DirectoryUpdater.DEFAULT_DIR, service.getOverrideDirectory());
        String contrib = "OSGI-INF/test-targetplatforms-dir-override-contrib.xml";
        URL url = getClass().getClassLoader().getResource(contrib);
        RuntimeContext ctx = null;
        try {
            ctx = harness.deployTestContrib("org.nuxeo.targetplatforms.core", url);
            assertEquals("test", service.getOverrideDirectory());
        } finally {
            if (ctx != null) {
                ctx.undeploy(url);
            }
        }

    }

    @Test
    public void testPlatformRegistrationOverride() throws Exception {
        TargetPlatform tpOld = service.getTargetPlatform("dm-5.3.0");
        assertNotNull(tpOld);
        assertFalse(tpOld.isEnabled());

        TargetPlatform tpNew = service.getTargetPlatform("cap-5.9.2");
        assertNotNull(tpNew);
        assertTrue(tpNew.isEnabled());

        String contrib = "OSGI-INF/test-targetplatforms-override-contrib.xml";
        URL url = getClass().getClassLoader().getResource(contrib);
        RuntimeContext ctx = null;
        try {
            ctx = harness.deployTestContrib("org.nuxeo.targetplatforms.core", url);

            tpOld = service.getTargetPlatform("dm-5.3.0");
            assertNotNull(tpOld);
            assertTrue(tpOld.isEnabled());

            tpNew = service.getTargetPlatform("cap-5.9.2");
            assertNotNull(tpNew);
            assertFalse(tpNew.isEnabled());
        } finally {
            if (ctx != null) {
                ctx.undeploy(url);
            }
        }

    }

    @Test
    public void testPackageRegistrationOverride() throws Exception {
        TargetPackage tp = service.getTargetPackage("nuxeo-dm-5.8");
        assertNotNull(tp);
        assertTrue(tp.isEnabled());

        String contrib = "OSGI-INF/test-targetplatforms-override-contrib.xml";
        URL url = getClass().getClassLoader().getResource(contrib);
        RuntimeContext ctx = null;
        try {
            ctx = harness.deployTestContrib(BUNDLE, url);
            tp = service.getTargetPackage("nuxeo-dm-5.8");
            assertNotNull(tp);
            assertFalse(tp.isEnabled());

        } finally {
            if (ctx != null) {
                ctx.undeploy(url);
            }
        }
    }

}
