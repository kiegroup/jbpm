package org.jbpm.concurrency.util;

import junit.framework.TestCase;

import javax.persistence.OptimisticLockException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class RunThreadCommands {

    boolean isFailed = false;
    private List<Command> commands = new ArrayList<Command>();
    private boolean testForException = true;
    Logger logger = LoggerFactory.getLogger(RunThreadCommands.class);

    public void addCommand(Command c) {
        commands.add(c);
    }

    public void runCommandsWithoutExceptionTest() {
        testForException = false;
        runCommands();
        testForException = true;
    }

    public void runCommands() {
        GreenLight gr = new GreenLight();
        TestThread t = null;
        List<TestThread> threads = new ArrayList<TestThread>(commands.size());
        for (Command c : commands) {
            t = new TestThread(gr);
            t.setC(c);
            threads.add(t);
            t.start();
        }
        gr.setIsGreen(true);
        for (TestThread th : threads) {
            try {
                th.join();
                if (testForException && th.getException() != null) {
                    Throwable e = th.getException();
                    logger.error(e.getMessage(), e);
                    isFailed = true;
                }

            } catch (InterruptedException ex) {
                TestCase.fail("error during execution");
            }
        }
        if (isFailed) {
            TestCase.fail("error during command execution");
        }
        commands.clear();
    }

    class TestThread extends Thread {

        GreenLight g = null;
        Command c = null;
        Throwable ex = null;

        public TestThread(GreenLight gr) {
            g = gr;
        }

        public void execute() {
            try {
                c.doWork();
            } catch (Throwable e) {
                ex = e;
            }
        }

        public Throwable getException() {
            return ex;
        }

        public void setC(Command c) {
            this.c = c;
        }

        @Override
        public void run() {
            while (!g.isGreen()) {
                yield();
            }
            execute();
        }
    }

    public static interface Command {

        void doWork() throws Exception;
    }
}
