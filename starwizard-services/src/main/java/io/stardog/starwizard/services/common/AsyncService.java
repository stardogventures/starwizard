package io.stardog.starwizard.services.common;

import io.dropwizard.lifecycle.Managed;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A shared service for executing lightweight asynchronous processes and managing their thread pool using Dropwizard
 * lifecycle. Essentially just a wrapper around ExecutorService that implements the Dropwizard lifecycle.
 */
@Singleton
public class AsyncService implements Managed {
    private final ExecutorService executorService;

    @Inject
    public AsyncService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void stop() throws Exception {
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.SECONDS);
        executorService.shutdownNow();
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public Future<?> submit(Runnable runnable) {
        return executorService.submit(runnable);
    }
}
