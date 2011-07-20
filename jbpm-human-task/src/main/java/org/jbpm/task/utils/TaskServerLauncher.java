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
import org.jbpm.task.service.TaskServer;
import org.jbpm.task.service.TaskService;
import org.jbpm.task.service.TaskServiceSession;
import org.jbpm.task.service.hornetq.HornetQTaskServer;
import org.jbpm.task.service.mina.MinaTaskServer;

/**
 *
 * @author salaboy
 */
public class TaskServerLauncher {

    private static TaskServer server;
    protected static TaskService taskService;
    private static EntityManagerFactory emf;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting the Task Server");
        final CommandLineParser cmdLinePosixParser = new PosixParser();
        final Options posixOptions = constructPosixOptions();
        

        
        
        CommandLine commandLine;
        try {
            commandLine = cmdLinePosixParser.parse(posixOptions, args);

            if (commandLine.hasOption("impl")) {
                
                initializeServices();
                if (commandLine.getOptionValue("impl").equals("MINA")) {
                    server = new MinaTaskServer(taskService);
                    startServer(server);
                }
                else if (commandLine.getOptionValue("impl").equals("HORNETQ")) {
                    server = new HornetQTaskServer(taskService, 4554);
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
                if(server.isRunning()){
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

        posixOptions.addOption(OptionBuilder.withLongOpt("impl").withDescription("Select the Implementation that you will use to run the "
                + "                                       Human Task Service: (LOCAL, MINA, HORNETQ)").hasArg().create());
//      posixOptions.addOption("impl", true, 
//              "Select the Implementation that you will use to run the Human Task Service: (LOCAL, MINA, HORNETQ)");
        return posixOptions;
    }

    /**
     * Construct and provide GNU-compatible Options.
     * 
     * @return Options expected from command-line of GNU form.
     */
    public static Options constructGnuOptions() {
        final Options gnuOptions = new Options();
        gnuOptions.addOption("impl", true,
                "Select the Implementation that you will use to run the Human Task Service: (LOCAL, MINA, HORNETQ)");
        return gnuOptions;
    }

    
    public static void startServer(TaskServer server) throws InterruptedException {
        

        
        new Thread(server).start();

        System.out.print("Waiting for the "+server.getName()+" to start");
        while (!server.isRunning()) {
            System.out.print(".");
            Thread.sleep(50);
        }
        System.out.println(server.getName()+"Started!");
        System.out.println(server.getName()+": "+server.getDescription());
    }

    private static void initializeServices() {
        emf = Persistence.createEntityManagerFactory("org.jbpm.task");
        taskService = new TaskService(emf, SystemEventListenerFactory.getSystemEventListener());
        TaskServiceSession createSession = taskService.createSession();
    }
}
