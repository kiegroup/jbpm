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

package org.jbpm.test.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.Map;

public class Structure {

    private Integer id;
    private Map<String, String> cache;
    private LocalDate currentDate;
    private LocalDateTime currentDateTime;
    private Duration daysTimeDuration;
    private LocalTime currentTime;
    private Period yearsMonthsDuration;

    public Structure() {
        // do nothing
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, String> getCache() {
        return cache;
    }

    public void setCache(Map<String, String> cache) {
        this.cache = cache;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public LocalDateTime getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(LocalDateTime currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public Duration getDaysTimeDuration() {
        return daysTimeDuration;
    }

    public void setDaysTimeDuration(Duration daysTimeDuration) {
        this.daysTimeDuration = daysTimeDuration;
    }

    public LocalTime getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(LocalTime currentTime) {
        this.currentTime = currentTime;
    }

    public Period getYearsMonthsDuration() {
        return yearsMonthsDuration;
    }

    public void setYearsMonthsDuration(Period yearsMonthsDuration) {
        this.yearsMonthsDuration = yearsMonthsDuration;
    }
}
