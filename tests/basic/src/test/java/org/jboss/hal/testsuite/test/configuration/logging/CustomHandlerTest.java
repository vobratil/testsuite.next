/*
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.hal.testsuite.test.configuration.logging;

import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.graphene.page.Page;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.hal.testsuite.Console;
import org.jboss.hal.testsuite.creaper.ManagementClientProvider;
import org.jboss.hal.testsuite.creaper.ResourceVerifier;
import org.jboss.hal.testsuite.fragment.AddResourceDialogFragment;
import org.jboss.hal.testsuite.fragment.FormFragment;
import org.jboss.hal.testsuite.fragment.TableFragment;
import org.jboss.hal.testsuite.page.configuration.LoggingConfigurationPage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

import static org.jboss.hal.dmr.ModelDescriptionConstants.CLASS;
import static org.jboss.hal.dmr.ModelDescriptionConstants.LEVEL;
import static org.jboss.hal.dmr.ModelDescriptionConstants.MODULE;
import static org.jboss.hal.dmr.ModelDescriptionConstants.NAME;
import static org.jboss.hal.testsuite.test.configuration.logging.LoggingFixtures.*;
import static org.junit.Assert.assertEquals;

@RunWith(Arquillian.class)
public class CustomHandlerTest {

    private static final OnlineManagementClient client = ManagementClientProvider.createOnlineManagementClient();
    private static final Operations operations = new Operations(client);

    @BeforeClass
    public static void beforeClass() throws Exception {
        operations.add(customHandlerAddress(CUSTOM_HANDLER_READ),
                Values.of(MODULE, MODULE_VALUE).and(CLASS, CLASS_VALUE));
        operations.add(customHandlerAddress(CUSTOM_HANDLER_UPDATE),
                Values.of(MODULE, MODULE_VALUE).and(CLASS, CLASS_VALUE));
        operations.add(customHandlerAddress(CUSTOM_HANDLER_DELETE),
                Values.of(MODULE, MODULE_VALUE).and(CLASS, CLASS_VALUE));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        operations.removeIfExists(customHandlerAddress(CUSTOM_HANDLER_CREATE));
        operations.removeIfExists(customHandlerAddress(CUSTOM_HANDLER_READ));
        operations.removeIfExists(customHandlerAddress(CUSTOM_HANDLER_UPDATE));
        operations.removeIfExists(customHandlerAddress(CUSTOM_HANDLER_DELETE));
    }

    @Inject private Console console;
    @Page private LoggingConfigurationPage page;
    private TableFragment table;
    private FormFragment form;

    @Before
    public void setUp() throws Exception {
        page.navigate();
        console.verticalNavigation().selectSecondary(LOGGING_HANDLER_ITEM,
                "logging-handler-custom-item");
        table = page.getCustomHandlerTable();
        form = page.getCustomHandlerForm();
        table.bind(form);
    }

    @Test
    public void create() throws Exception {
        AddResourceDialogFragment dialog = table.add();
        dialog.getForm().text(NAME, CUSTOM_HANDLER_CREATE);
        dialog.getForm().text(MODULE, MODULE_VALUE);
        dialog.getForm().text(CLASS, CLASS_VALUE);
        dialog.add();

        console.verifySuccess();
        new ResourceVerifier(customHandlerAddress(CUSTOM_HANDLER_CREATE), client)
                .verifyExists();
    }

    @Test
    public void read() throws Exception {
        table.select(CUSTOM_HANDLER_READ);
        assertEquals(MODULE_VALUE, form.value(MODULE));
        assertEquals(CLASS_VALUE, form.value(CLASS));
    }

    @Test
    public void update() throws Exception {
        table.select(CUSTOM_HANDLER_UPDATE);
        form.edit();
        form.select(LEVEL, "CONFIG");
        form.save();

        console.verifySuccess();
        new ResourceVerifier(customHandlerAddress(CUSTOM_HANDLER_UPDATE), client)
                .verifyAttribute(LEVEL, "CONFIG");
    }

    @Test
    public void reset() throws Exception {
        table.select(CUSTOM_HANDLER_UPDATE);
        form.reset();

        console.verifySuccess();
        new ResourceVerifier(customHandlerAddress(CUSTOM_HANDLER_UPDATE), client)
                .verifyReset();
    }

    @Test
    public void delete() throws Exception {
        table.remove(CUSTOM_HANDLER_DELETE);

        console.verifySuccess();
        new ResourceVerifier(customHandlerAddress(CUSTOM_HANDLER_DELETE), client)
                .verifyDoesNotExist();
    }
}