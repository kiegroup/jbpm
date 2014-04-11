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

package org.jbpm.services.task.audit.query;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hans Lund
 */
public class WildCardFilter<K> extends TermFilter<K> {

    public static final char DEFAULT_SINGLE_CHAR_WILDCARD = '?';
    public static final char DEFAULT_GENERIC_WILDCARD = '*';
    public static final char DEFAULT_ESCAPE_CHAR = '\\';
    private char characterWildCard;
    private char stringWildCard;
    private char escapeChar;

    public WildCardFilter(Occurs occurs, String field, String... terms) {
        super(occurs, field, terms);
        this.characterWildCard = DEFAULT_SINGLE_CHAR_WILDCARD;
        this.stringWildCard = DEFAULT_GENERIC_WILDCARD;
        this.escapeChar = DEFAULT_ESCAPE_CHAR;
    }

    public WildCardFilter(Occurs occurs, String field, char single,
        char generic, char escape, String... terms) {
        super(occurs, field, terms);
        this.characterWildCard = single;
        this.stringWildCard = generic;
        this.escapeChar = escape;
    }

    public char getCharacterWildCard() {
        return characterWildCard;
    }

    public char getStringWildCard() {
        return stringWildCard;
    }

    public char getEscapeChar() {
        return escapeChar;
    }

    public String escape(String argument) {
        StringBuilder builder = new StringBuilder();
        char[] org = argument.toCharArray();
        for (char c : org) {
            if (isSpecial(c)) {
                builder.append(escapeChar);
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public String[] getFormattedTerms(char single, char generic, char escape) {
        StringBuilder builder = new StringBuilder();
        List<String> formatted = new ArrayList<String>();
        for (String term : getMatches()) {
            char[] org = term.toCharArray();
            int i = 0;
            while (i < org.length) {
                char next = org[i];
                boolean isSpecial = isSpecial(next);
                boolean isNewSpecial = isContainedIn(next, single, generic, escape);
                if (isSpecial) {
                    char readAhead = org[i];
                    if (i < org.length - 1) { readAhead = org[i + 1]; }
                    if (next == escapeChar) {
                        if (isSpecial(readAhead) && !isContainedIn(readAhead, single, generic, escape)) {
                            builder.append(readAhead);
                            i += 2;
                        } else if (isSpecial(readAhead)) {
                            builder.append(escape).append(newChar(readAhead, single, generic, escape));
                            i += 2;
                        } else if (!isSpecial(readAhead)) {
                            if (isNewSpecial) {
                                builder.append(escape);
                            }
                            builder.append(next);
                            i += 1;
                        }
                    } else {
                        builder.append(newChar(next, single, generic, escape));
                        i += 1;
                    }
                } else if (isNewSpecial) {
                    builder.append(escape).append(next);
                    i += 1;
                } else {
                    builder.append(next);
                    i += 1;
                }
            }
            formatted.add(builder.toString());
            builder.setLength(0);
        }
        return formatted.toArray(new String[formatted.size()]);
    }

    public boolean matches(Object value) {
        for (String pattern : getMatches()) {
            value = occurs == Occurs.SHOULD ? ((String) value).toLowerCase()
                : value;
            pattern = occurs == Occurs.SHOULD ? pattern.toLowerCase() : pattern;
            if (match((String) value, pattern)) {
                return true;
            }
        }
        return false;
    }

    private boolean match(String string, String pattern) {
        return match(string, pattern, 0, 0);
    }

    private boolean match(String s, String pattern, int offset, int poffset) {

        char patternChar, stringChar;
        int sLen = s.length(), pLen = pattern.length();

        while (offset < sLen && poffset < pLen) {
            patternChar = pattern.charAt(poffset);
            stringChar = s.charAt(offset);
            if (patternChar == characterWildCard) {
                ++offset;
                ++poffset;
                continue;
            }
            if (patternChar == stringWildCard) {
                if (poffset == pattern.length() - 1) {
                    return true;
                }
                while (offset < s.length()) {
                    if (match(s, pattern, offset, poffset + 1)) {
                        return true;
                    }
                    ++offset;
                }
                return false;
            }
             if (patternChar == escapeChar) {
                ++poffset;
                if (poffset == pattern.length()) {
                    return false;
                }
                patternChar = pattern.charAt(poffset);
            }
            if (patternChar != stringChar) {
                return false;
            }
            ++poffset;
            ++offset;
        }
        while (poffset < pattern.length() && pattern.charAt(poffset) == stringWildCard) {
            ++poffset;
        }
        return (offset == s.length()) && (poffset == pattern.length());
    }

    private char newChar(char old, char single, char generic, char escape) {
        if (old == escapeChar) {
            return escape;
        }
        if (old == characterWildCard) {
            return single;
        }
        if (old == stringWildCard) {
            return generic;
        }
        return old;
    }

    private boolean isContainedIn(char needle, char... haystack) {
        for (char hay : haystack) {
            if (needle == hay) {
                return true;
            }
        }
        return false;
    }

    private boolean isSpecial(char c) {
        return c == escapeChar || c == stringWildCard || c == characterWildCard;
    }
}
