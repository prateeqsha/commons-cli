/**
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.commons.cli2.commandline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli2.Argument;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.WriteableCommandLine;

/**
 * A WriteableCommandLine implementation allowing Options to write their
 * processed information to a CommandLine.
 */
public class WriteableCommandLineImpl extends CommandLineImpl implements WriteableCommandLine {

    private final Properties properties = new Properties();
    private final List options = new ArrayList();
    private final Map nameToOption = new HashMap();
    private final Map values = new HashMap();
    private final Map switches = new HashMap();
    private final Map defaults = new HashMap();
    private final List normalised;
    private final Set prefixes;

    private CommandLine defaultCommandLine = null;

    /**
     * Creates a new WriteableCommandLineImpl rooted on the specified Option, to
     * hold the parsed arguments.
     *
     * @param rootOption the CommandLine's root Option
     * @param arguments the arguments this CommandLine represents
     */
    public WriteableCommandLineImpl(final Option rootOption, final List arguments) {
        this.prefixes = rootOption.getPrefixes();
        this.normalised = arguments;
    }

    public void addOption(Option option) {
        options.add(option);
        nameToOption.put(option.getPreferredName(), option);
        for (Iterator i = option.getTriggers().iterator(); i.hasNext();) {
            nameToOption.put(i.next(), option);
        }
    }

    public void addValue(final Option option, final Object value) {
        if (option instanceof Argument) {
            addOption(option);
        }
        List valueList = (List)values.get(option);
        if (valueList == null) {
            valueList = new ArrayList();
            values.put(option, valueList);
        }
        valueList.add(value);
    }

    public void addSwitch(final Option option, final boolean value) {
        addOption(option);
        if (switches.containsKey(option)) {
            throw new IllegalStateException("Switch already set");
        }
        else {
            switches.put(option, value ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    public boolean hasOption(final Option option) {
        final boolean present = options.contains(option);
        if (!present && defaultCommandLine != null) {
            return defaultCommandLine.hasOption(option);
        }
        else {
            return present;
        }
    }
    
    public Option getOption(final String trigger) {
        return (Option)nameToOption.get(trigger);
    }

    //TODO Document the order of values and defaults
    public List getValues(final Option option, final List defaultValues) {

        // First grab the command line values
        List valueList = (List)values.get(option);

        // Secondly try alternate CommandLines
        if ((valueList == null || valueList.isEmpty())
        && defaultCommandLine != null) {
            valueList = defaultCommandLine.getValues(option, null);
        }

        // Thirdly try the defaults supplied to the method
        if (valueList == null || valueList.isEmpty()) {
            valueList = defaultValues;
        }

        // Fourthly try the option's default values
        if (valueList == null || valueList.isEmpty()) {
            valueList = (List)this.defaults.get(option);
        }

        // Finally use an empty list
        if (valueList == null) {
            valueList = Collections.EMPTY_LIST;
        }

        return valueList;
    }
    
    public Boolean getSwitch(final Option option, final Boolean defaultValue) {
        // First grab the command line values
        Boolean bool = (Boolean)switches.get(option);

        // Secondly try alternate CommandLines
        if (bool == null && defaultCommandLine != null) {
            bool = defaultCommandLine.getSwitch(option);
        }

        // Thirdly try the defaults supplied to the method
        if (bool == null) {
            bool = defaultValue;
        }

        // Fourthly try the option's default values
        //????

        return bool;
    }

    public void addProperty(final String property, final String value) {
        properties.setProperty(property, value);
    }
    
    public String getProperty(final String property, final String defaultValue) {
        return properties.getProperty(property,defaultValue);
    }

    public Set getProperties() {
        if (defaultCommandLine == null) {
            return Collections.unmodifiableSet(properties.keySet());
        }
        else {
            final Set props = new HashSet();
            props.addAll(properties.keySet());
            props.addAll(defaultCommandLine.getProperties());
            return Collections.unmodifiableSet(props);
        }
    }
    
    public boolean looksLikeOption(final String trigger) {
        for (final Iterator i = prefixes.iterator(); i.hasNext();) {
            final String prefix = (String)i.next();
            if (trigger.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    public String toString() {
        final StringBuffer buffer = new StringBuffer();

        // need to add group header

        for (final Iterator i = normalised.iterator(); i.hasNext();) {
            final String arg = (String)i.next();
            if (arg.indexOf(' ') >= 0) {
                buffer.append("\"").append(arg).append("\"");
            }
            else {
                buffer.append(arg);
            }
            if (i.hasNext()) {
                buffer.append(' ');
            }
        }

        return buffer.toString();
    }
    
    public List getOptions() {
        return Collections.unmodifiableList(options);
    }

    public Set getOptionTriggers() {
        return Collections.unmodifiableSet(nameToOption.keySet());
    }
}
