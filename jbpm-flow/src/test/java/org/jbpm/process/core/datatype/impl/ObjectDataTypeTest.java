/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates.
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
package org.jbpm.process.core.datatype.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.jbpm.process.core.datatype.impl.type.ObjectDataType;
import org.junit.Test;


public class ObjectDataTypeTest {

    @Test
    public void testReadValueNull() {
        ObjectDataType dateType = new ObjectDataType(Date.class.getCanonicalName());
        assertThat(dateType.readValue(null)).isNull();
        assertThat(dateType.valueOf(null)).isNull();
        assertThat(dateType.verifyDataType(null)).isTrue();
    }

    @Test
    public void testReadValueDate() {
        ObjectDataType dateType = new ObjectDataType(Date.class.getCanonicalName());
        assertThat(dateType.readValue("2012-02-02")).isInstanceOf(Date.class);
        assertThat(dateType.readValue("12:12:12")).isInstanceOf(Date.class);
        assertThat(dateType.valueOf("2012-02-02")).isInstanceOf(Date.class);
        assertThat(dateType.valueOf("12:12:12")).isInstanceOf(Date.class);
        assertThat(dateType.valueOf("2016-01-11T01:06:32")).isInstanceOf(Date.class)
                .asInstanceOf(InstanceOfAssertFactories.DATE).hasHourOfDay(1).hasMinute(6).hasSecond(32);
        assertThat(dateType.valueOf("pepe")).isInstanceOf(String.class);
        assertThatThrownBy(() -> dateType.readValue("pepe")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testReadValueLocalDate() {
        ObjectDataType dateType = new ObjectDataType(LocalDate.class.getCanonicalName());
        assertThat(dateType.readValue("2012-02-02")).isInstanceOf(LocalDate.class);
        assertThat(dateType.valueOf("2012-02-02")).isInstanceOf(LocalDate.class);
    }

    @Test
    public void testReadValueLocalDateTime() {
        ObjectDataType dateType = new ObjectDataType(LocalDateTime.class.getCanonicalName());
        assertThat(dateType.readValue("2012-02-02T12:12:12")).isInstanceOf(LocalDateTime.class);
        assertThat(dateType.valueOf("2012-02-02T12:12:12")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    public void testReadValueZonedDateTime() {
        ObjectDataType dateType = new ObjectDataType(ZonedDateTime.class.getCanonicalName());
        assertThat(dateType.readValue("2012-02-02T12:12:12+00:01")).isInstanceOf(ZonedDateTime.class);
        assertThat(dateType.valueOf("2012-02-02T12:12:12+00:01")).isInstanceOf(ZonedDateTime.class);
    }
}
