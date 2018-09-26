package io.stardog.starwizard.services.common;

import io.dropwizard.lifecycle.Managed;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * A shared service for executing lightweight asynchronous processes and managing their thread pool using Dropwizard
 * lifecycle.
 */
@Singleton
public class AsyncService implements Managed {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

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
