package org.jbpm.executor.commands;

import org.jbpm.executor.ExecutorServiceFactory;
import org.kie.api.executor.CommandAsync;
import org.kie.api.executor.CommandContext;
import org.kie.api.executor.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple async command to call external service without blocking executor thread
 * for example webservice with callback or SOAP over JMS
 * Just for demo purpose.
 *
 */
public class AsyncCommand implements CommandAsync {
    private static final Logger logger = LoggerFactory.getLogger(AsyncCommand.class);

    public void execute(CommandContext ctx, Long requestId) {
        logger.info("Command executed on executor with data {}", ctx.getData());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ExecutorServiceFactory.newExecutorService().scheduleResponse(requestId, new ExecutionResults());
            }
        }).start();
    }
}
