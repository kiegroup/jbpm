/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.services.task.audit.index;

import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jbpm.services.task.audit.marshalling.AuditMarshaller;

/**
 *@author Hans Lund
 */
public abstract class ModelIndexImpl<T> implements ModelIndex<T>  {

    protected Document createDocument(T object) {
        Document doc = new Document();
        addKeyWordField("ALL", "ALL", doc, false);
        addKeyWordField("id", getId(object),doc, false);
        addKeyWordField("type", getType(), doc, false);
        addBinary(object,doc);
        return doc;
    }


    abstract protected String getType();

    @Override
    public byte[] toBytes(T object) {
        return AuditMarshaller.marshall(object);
    }

    protected void addBinary(T object, Document d) {
        d.add(new StoredField(LuceneIndexService.BINARY, toBytes(object)));
    }

    protected void addDefaultField(String name, String value, Document d,
        boolean includeInFreeText) {
        if (value == null || "".equals(value)) {
            return;
        }
        d.add(new TextField(name, value, Field.Store.NO));
        if (includeInFreeText) {
            addFreeTextField(value, d);
        }
    }

    protected void addFreeTextField(String s, Document doc) {
        if (s != null && s.length() > 0) {
            doc.add(new TextField("freetext", s, Field.Store.NO));
        }
    }

    protected void addKeyWordField(String name, String value, Document d,
        boolean includeInFreeText) {
        if (value == null || "".equals(value)) {
            return;
        }
        d.add(new StringField(name, value, Field.Store.NO));
        if (includeInFreeText) {
            addFreeTextField(value, d);
        }
    }

    protected void addDateField(String name, Date value, Document d,
        boolean includeInFreeText) {
        if (value == null) {
            return;
        }
        String strVal = DateTools.dateToString(value, DateTools.Resolution.DAY);
        LongField field = new LongField(name, value.getTime(), Field.Store.NO);
        StringField sf = new StringField(name + LuceneQueryBuilder.STR, strVal, Field.Store.NO);
        d.add(field);
        d.add(sf);
        if (includeInFreeText) {
            addFreeTextField(strVal, d);
        }
    }

    protected void addIntField(String name, int value, Document d,
        boolean includeInFreeText) {
        String strVal = String.valueOf(value);
        IntField field = new IntField(name, value, Field.Store.NO);
        StringField sf = new StringField(name + LuceneQueryBuilder.STR, strVal, Field.Store.NO);
        d.add(field);
        d.add(sf);
        if (includeInFreeText) {
            addFreeTextField(strVal, d);
        }
    }

    protected void addLongField(String name, Long value, Document doc,
        boolean includeInFreeText) {
        if (value == null) {
            return;
        }
        String strVal = value.toString();
        LongField field = new LongField(name, value, Field.Store.NO);
        StringField sf = new StringField(name + LuceneQueryBuilder.STR, strVal, Field.Store.NO);
        doc.add(field);
        doc.add(sf);
        if (includeInFreeText) {
            addFreeTextField(strVal, doc);
        }
    }
}
