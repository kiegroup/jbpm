/*
 * Copyright 2011 JBoss Inc..
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
package org.jbpm.task.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.drools.SystemEventListenerFactory;
import org.jbpm.task.Group;
import org.jbpm.task.User;
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.hornetq.HornetQTaskServer;
import org.jbpm.task.service.mina.MinaTaskServer;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author salaboy
 */
public class TaskServerLauncher {

    private static TaskServer server;
    private static TaskService taskService;
    private static EntityManagerFactory emf;
    private static TaskServiceSession taskSession;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting the Task Server ... ");
        final CommandLineParser cmdLinePosixParser = new PosixParser();
        final Options posixOptions = constructPosixOptions();
        int port = 9123; // Default Port for the Human Task Service



        CommandLine commandLine;
        try {
            commandLine = cmdLinePosixParser.parse(posixOptions, args);
            initializeServices();
            if (commandLine.hasOption("users")) {
                initializeUsers("");
            }
            if (commandLine.hasOption("groups")) {
                initializeGroups("");
            }
            if (commandLine.hasOption("port")) {
                port = Integer.valueOf(commandLine.getOptionValue("port"));
            }

            if (commandLine.hasOption("impl")) {

                if (commandLine.getOptionValue("impl").equals("LOCAL")) {
                    System.out.println("Not Supported Yet!!! But a JNDI Implemention will be suported!");
                    
                    throw new NotImplementedException();
                }
                if (commandLine.getOptionValue("impl").equals("MINA")) {
                    server = new MinaTaskServer(taskService, port);
                    startServer(server);
                }
                if (commandLine.getOptionValue("impl").equals("HORNETQ")) {
                    server = new HornetQTaskServer(taskService, port);
                    startServer(server);
                }
            }


        } catch (ParseException parseException) // checked exception  
        {
            System.err.println(
                    "Encountered exception while parsing using PosixParser:\n"
                    + parseException.getMessage());
        }
        System.out.println("Shutdown the Server with CTRL+C ...");
        Thread shutdownThread = new Thread() {

            public void run() {
                if (server.isRunning()) {
                    System.out.println("Shuting down the server");
                    try {
                        server.stop();
                    } catch (Exception ex) {
                        Logger.getLogger(TaskServerLauncher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }

    /**
     * Construct and provide Posix-compatible Options.
     * 
     * @return Options expected from command-line of Posix form.
     */
    public static Options constructPosixOptions() {
        final Options posixOptions = new Options();

        posixOptions.addOption(OptionBuilder
                                    .withLongOpt("impl")
                                    .withDescription("Select the Implementation that you will use to run the "
                + "                                       Human Task Service: (LOCAL, MINA, HORNETQ)")
                                    .hasArg()
                                    .create());
        posixOptions.addOption(OptionBuilder
                                .withLongOpt("port")
                                .withDescription("Set a port to the Task Server (1025 - 9999)")
                                .hasArg()
                                .create());
        posixOptions.addOption(OptionBuilder
                                .withLongOpt("users")
                                .withDescription("File where the users mappings are defined")
                                .hasArg()
                                .create());
        
        posixOptions.addOption(OptionBuilder
                                .withLongOpt("groups")
                                .withDescription("File where the group mappings are defined")
                                .hasArg()
                                .create());

        return posixOptions;
    }

    public static void startServer(TaskServer server) throws InterruptedException {



        new Thread(server).start();

        System.out.print("Waiting for the " + server.getName() + " to start");
        while (!server.isRunning()) {
            System.out.print(".");
            Thread.sleep(50);
        }
        System.out.println(server.getName() + "Started!");
        System.out.println(server.getName() + ": " + server.getDescription());
    }

    private static void initializeServices() {
        emf = Persistence.createEntityManagerFactory("org.jbpm.task");
        taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        taskSession = taskService.createSession();
    }

    private static void initializeUsers(String usersLocation) {
        Map<String, Object> vars = new HashMap();

        Reader reader = new InputStreamReader(TaskServerLauncher.class.getResourceAsStream(usersLocation));
        Map<String, User> users = (Map<String, User>) eval(reader, vars);
        for (User user : users.values()) {
            taskSession.addUser(user);
        }
    }

    private static void initializeGroups(String groupsLocation) {
        Map<String, Object> vars = new HashMap();
        Reader reader = new InputStreamReader(TaskServerLauncher.class.getResourceAsStream(groupsLocation));
        Map<String, Group> groups = (Map<String, Group>) eval(reader, vars);
        for (Group group : groups.values()) {
            taskSession.addGroup(group);
        }
    }

    //Utility Methods, should I place these methods in a separate utility class??
    public static Object eval(Reader reader, Map<String, Object> vars) {
        try {
            return eval(toString(reader), vars);
        } catch (IOException e) {
            throw new RuntimeException("Exception Thrown", e);
        }
    }

    public static String toString(Reader reader) throws IOException {
        int charValue = 0;
        StringBuffer sb = new StringBuffer(1024);
        while ((charValue = reader.read()) != -1) {
            // result = result + (char) charValue;
            sb.append((char) charValue);
        }
        return sb.toString();
    }

    public static Object eval(String str, Map<String, Object> vars) {
        ExpressionCompiler compiler = new ExpressionCompiler(str.trim());

        ParserContext context = new ParserContext();
        context.addPackageImport("org.drools.task");
        context.addPackageImport("org.drools.task.service");
        context.addPackageImport("org.drools.task.query");
        context.addPackageImport("java.util");

        vars.put("now", new Date());
        return MVEL.executeExpression(compiler.compile(context), vars);
    }
}
