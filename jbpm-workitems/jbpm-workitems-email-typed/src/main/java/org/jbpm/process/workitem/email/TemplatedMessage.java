/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.process.workitem.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TemplatedMessage implements Message {

    private Recipients recipients;
    private String from;
    private String replyTo;
    private String subject;
    private String bodyTemplate;
    private String documentFormat = "html";
    private List<String> attachments;

    private TemplateManager templateManager;
    private Map<String, Object> templateParameters;

    public TemplatedMessage(TemplateManager templateManager) {
        this.recipients = new Recipients();
        this.setAttachments(new ArrayList<String>());
        this.templateManager = templateManager;
        this.templateParameters = new HashMap<>();
    }

    public TemplatedMessage() {
        this(null);
    }

    public void setTemplateManager(TemplateManager templateManager) {
        this.templateManager = templateManager;
    }

    public Recipients getRecipients() {
        return recipients;
    }

    public void setRecipients(Recipients recipients) {
        this.recipients = recipients;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String getDocumentFormat() {
        return documentFormat;
    }

    public void setDocumentFormat(String documentFormat) {
        this.documentFormat = documentFormat;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplate() {
        return bodyTemplate;
    }

    public void setTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public void setTemplateParameters(Map<String, Object> params) {
        this.templateParameters.putAll(params);
    }

    @Override
    public String getBody() {
        Objects.requireNonNull(templateManager, "TemplateManager should be set");
        return templateManager.render(this.bodyTemplate, this.templateParameters);
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public boolean hasAttachment() {
        return !this.attachments.isEmpty();
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bodyTemplate == null) ? 0 : bodyTemplate.hashCode());
        result = prime * result + ((documentFormat == null) ? 0 : documentFormat.hashCode());
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((recipients == null) ? 0 : recipients.hashCode());
        result = prime * result + ((replyTo == null) ? 0 : replyTo.hashCode());
        result = prime * result + ((subject == null) ? 0 : subject.hashCode());
        result = prime * result + ((attachments == null) ? 0 : attachments.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Message other = (Message) obj;

        if (!getBody().equals(other.getBody())) {
            return false;
        }
        if (documentFormat == null) {
            if (other.getDocumentFormat() != null) {
                return false;
            }
        } else if (!documentFormat.equals(other.getDocumentFormat())) {
            return false;
        }
        if (from == null) {
            if (other.getFrom() != null) {
                return false;
            }
        } else if (!from.equals(other.getFrom())) {
            return false;
        }
        if (recipients == null) {
            if (other.getRecipients() != null) {
                return false;
            }
        } else if (!recipients.equals(other.getRecipients())) {
            return false;
        }
        if (replyTo == null) {
            if (other.getReplyTo() != null) {
                return false;
            }
        } else if (!replyTo.equals(other.getReplyTo())) {
            return false;
        }
        if (subject == null) {
            if (other.getSubject() != null) {
                return false;
            }
        } else if (!subject.equals(other.getSubject())) {
            return false;
        }
        if (attachments == null) {
            if (other.getAttachments() != null) {
                return false;
            }
        } else if (!attachments.equals(other.getAttachments())) {
            return false;
        }
        return true;
    }
}