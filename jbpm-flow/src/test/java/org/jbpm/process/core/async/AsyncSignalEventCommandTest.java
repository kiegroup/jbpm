/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
 */

package org.jbpm.process.core.async;

import org.junit.Test;
import org.kie.api.executor.CommandContext;

public class AsyncSignalEventCommandTest {

    // check we don't get when processInstanceId comes from a REST call with json (non type-safe
    @Test(expected=IllegalArgumentException.class)
    public void testAsyncSignalEventInteger1Command() throws Exception {
        AsyncSignalEventCommand command = new AsyncSignalEventCommand();
        CommandContext ctx = new CommandContext();
        ctx.setData("processInstanceId", Integer.valueOf(2));
        command.execute(ctx);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAsyncSignalEventInteger2Command() throws Exception {
        AsyncSignalEventCommand command = new AsyncSignalEventCommand();
        CommandContext ctx = new CommandContext();
        ctx.setData("ProcessInstanceId", Integer.valueOf(2));
        command.execute(ctx);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAsyncSignalEventLong1Command() throws Exception {
        AsyncSignalEventCommand command = new AsyncSignalEventCommand();
        CommandContext ctx = new CommandContext();
        ctx.setData("processInstanceId", Long.valueOf(2L));
        command.execute(ctx);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAsyncSignalEventLong2Command() throws Exception {
        AsyncSignalEventCommand command = new AsyncSignalEventCommand();
        CommandContext ctx = new CommandContext();
        ctx.setData("ProcessInstanceId", Long.valueOf(2L));
        command.execute(ctx);
    }
}
