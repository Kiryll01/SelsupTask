package com.pisps;

import com.google.gson.Gson;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CrptApi {
    private static final String createDocumentUri = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private static final HttpClient client = HttpClient.newHttpClient();

    public record Document(String description, String docId, String docStatus, String docType, boolean importRequest,
                           String ownerInn, String participantInn, String producerInn, String productionDate,
                           String productionType, List<Product> products, String regDate, String regNumber){
    }
    public record Product(String certificateDocument, String certificateDocumentDate, String certificateDocumentNumber,
                          String ownerInn, String producerInn, String productionDate, String tnvedCode, String uitCode,
                          String uituCode) {
    }
    public CrptApi(int requestLimitPerPeriod, TimeUnit period) {
        this.requestLimitPerPeriod = requestLimitPerPeriod;
        this.period = period;
        this.requestQueue = new LinkedBlockingQueue<>();
        this.executor = Executors.newFixedThreadPool(requestLimitPerPeriod);
        schedulePeriodicTask();
    }
    private final AtomicInteger requestsInCurrentPeriod = new AtomicInteger(0);
    private final int requestLimitPerPeriod;
    private final TimeUnit period;
    private final BlockingQueue<Runnable> requestQueue;
    private final ExecutorService executor;

    private void schedulePeriodicTask() {
        try (ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            scheduler.scheduleAtFixedRate(this::scheduledTask, 0, 1, period);
        }
    }
    private void scheduledTask() {
        resetRequestsInCurrentPeriod();
        processWaitingRequestsFromQueue();
    }
    private void processWaitingRequestsFromQueue() {
        while(requestQueue.size() != 0 && requestsInCurrentPeriod.getAndIncrement() <= requestLimitPerPeriod) {
            processNextRequestFromQueue();
        }
    }
    private void processNextRequestFromQueue() {
        Runnable nextRequest = requestQueue.poll();
        if (nextRequest != null) {
            System.out.println("Processing task from the queue queue size " + requestQueue.size());
            executor.submit(nextRequest);
        }
    }
    private void resetRequestsInCurrentPeriod() {
        requestsInCurrentPeriod.set(0);
    }
    public void createDoc(Document document, String signature) {
        if (requestsInCurrentPeriod.get() >= requestLimitPerPeriod) {
            System.out.println("Request limit exceeded. Adding request to the queue. queue size " + requestQueue.size());
            boolean requestAdded = requestQueue.offer(() -> createDoc(document, signature));
            if (!requestAdded) System.out.println("request was not added to the queue");
            return;
        }
        requestsInCurrentPeriod.incrementAndGet();
        executor.submit(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(createDocumentUri))
                        .POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(document)))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response status code: " + response.statusCode());
            } catch (Exception e) {
                System.out.println("Exception in API request: " + e);
            }
        });
    }
}
