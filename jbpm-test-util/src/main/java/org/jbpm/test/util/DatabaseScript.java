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

package org.jbpm.test.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class DatabaseScript implements Comparable<DatabaseScript> {

    private String name;
    private Version from;
    private Version to;
    private String qualifier;
    private File script;

    public DatabaseScript(File script) {
        this(script.getName());
        this.script = script;
    }

    public DatabaseScript(String value) {
        Pattern pattern = Pattern.compile("(.*)-(\\d+\\.\\d+)-to-(\\d+\\.\\d+)-?(.*)?.sql");
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches() && matcher.groupCount() >= 3) {
            this.name = matcher.group(1);
            this.from = new Version(matcher.group(2));
            this.to = new Version(matcher.group(3));
            if(matcher.groupCount() > 3) {
                this.qualifier = matcher.group(4);
            }
        } else {
            this.name = value;
        }
    }

    public File getScript() {
        return script;
    }

    public String getName() {
        return name;
    }

    public Version getTo() {
        return to;
    }

    public Version getFrom() {
        return from;
    }

    @Override
    public int compareTo(DatabaseScript o) {
        if (this.name.equals(o.name)) {
            if (from == null || o.from == null) {
                return this.name.compareTo(o.name);
            }
            return from.compareTo(o.from);
        } else {
            return this.name.compareTo(o.name);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((script == null) ? 0 : script.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DatabaseScript) {
            return this.compareTo((DatabaseScript) obj) == 0;
        }
        return false;
    }

    @Override
    public String toString() {
        return from != null && to != null ? "Script " + this.name + " from version " + from + " to " + to : "Script " + this.name;
    }

    public String getQualifier() {
        return qualifier;
    }

}
