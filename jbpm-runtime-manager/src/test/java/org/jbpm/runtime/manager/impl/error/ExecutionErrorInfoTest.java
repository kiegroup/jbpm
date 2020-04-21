/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.runtime.manager.impl.error;

import java.util.Random;

import org.assertj.core.api.Assertions;
import org.jbpm.runtime.manager.impl.jpa.ExecutionErrorInfo;
import org.junit.After;
import org.junit.Test;

public class ExecutionErrorInfoTest {

    @After
    public void tearUp() {
        System.clearProperty("org.kie.jbpm.error.log.length");
    }
    @Test
    public void testTrimmedErrorMessage() {
        ExecutionErrorInfo info = new ExecutionErrorInfo();
        String randomString = randomString(300);
        info.setErrorMessage(randomString);
        Assertions.assertThat(info.getErrorMessage().length()).isEqualTo(255);
        Assertions.assertThat(info.getErrorMessage()).isEqualTo(randomString.substring(0, 255));
    }

    @Test
    public void testTrimmedNotDefaultErrorMessage() {
        System.setProperty("org.kie.jbpm.error.log.length", "5");
        String randomString = randomString(100);
        ExecutionErrorInfo info = new ExecutionErrorInfo();
        info.setErrorMessage(randomString);
        Assertions.assertThat(info.getErrorMessage().length()).isEqualTo(5);
        Assertions.assertThat(info.getErrorMessage()).isEqualTo(randomString.substring(0, 5));
    }

    @Test
    public void testDefaultErrorMessage() {
        String randomString = randomString(100);
        ExecutionErrorInfo info = new ExecutionErrorInfo();
        info.setErrorMessage(randomString);
        Assertions.assertThat(info.getErrorMessage().length()).isEqualTo(100);
        Assertions.assertThat(info.getErrorMessage()).isEqualTo(randomString);
    }

    private String randomString(int size) {
        char[] array = new char[size];
        Random random = new Random();
        for (int i = 0; i < array.length; i++) {
            array[i] = (char) random.nextInt();
        }
        return String.valueOf(array);
    }
}
