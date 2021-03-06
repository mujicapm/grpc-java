/*
 * Copyright 2015 The gRPC Authors
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

package io.grpc.examples.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.lang.Thread;
import java.lang.Math;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import static io.opentelemetry.api.common.AttributeKey.stringKey;


/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldServer {
  private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());
  private static void loggerHelperMMP (){
    logger.setLevel(Level.FINE);
    ConsoleHandler consoleHandler = new ConsoleHandler();
    consoleHandler.setLevel(Level.FINE);
    SimpleFormatter f = new SimpleFormatter();
    consoleHandler.setFormatter(f);
    logger.addHandler(consoleHandler);

  }

  private static final SdkMeterProvider meterProvider = SdkMeterProvider.builder()
          .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build())
          .build();

  private static final OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
          .setMeterProvider(meterProvider)
          .buildAndRegisterGlobal();// obtain instance of OpenTelemetry

  private static final Meter meter = openTelemetry.meterBuilder("instrumentation-library-name")
          .setInstrumentationVersion("1.0.0")
          .build();

  private static final LongCounter counter = meter
          .counterBuilder("processed_jobs")
          .setDescription("Processed HelloWorld Server jobs")
          .setUnit("1")
          .build();

  private static final Attributes attributes = Attributes.of(stringKey("Key"), "HelloWorld_Server_Call");

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    server = ServerBuilder.forPort(port)
        .addService(new GreeterImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    logger.fine("Started1");
    logger.log(Level.FINE, "Started2");
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          HelloWorldServer.this.stop();
          logger.fine("Ended2");
        } catch (InterruptedException e) {
          e.printStackTrace(System.err);
        }
        System.err.println("*** server shut down");
        logger.fine("Ended3");
      }
    });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    } else {logger.fine("Ended");}
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /**
   * Main launches the server from the command line.
   */
  public static void main(String[] args) throws IOException, InterruptedException {
    loggerHelperMMP ();
    final HelloWorldServer server = new HelloWorldServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      try {
        //randomly delaying by 0-1 seconds
        counter.add(1, attributes);
        Thread.sleep((long)(Math.random() * 1000));
        HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
      } catch(InterruptedException e) { }

    }
  }
}
