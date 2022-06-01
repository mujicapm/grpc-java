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

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.*;
import java.util.Collections;
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
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  public static void loggerHelperMMP() {
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
          .setDescription("Processed HelloWorld Client jobs")
          .setUnit("1")
          .build();

//  private static final BoundLongCounter hwClientBound =
//          counter.getBound(Collections.singletonList("HelloWorldClient"));

  private static final Attributes attributes = Attributes.of(stringKey("Key"), "HelloWorld_Client_Call");

//  public static void metricHelperMMP (){
//    SdkMeterProvider meterProvider = SdkMeterProvider.builder()
//            .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build())
//            .build();
//
//    OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
//            .setMeterProvider(sdkMeterProvider)
//            .buildAndRegisterGlobal();// obtain instance of OpenTelemetry

  // Gets or creates a named meter instance
//    Meter meter = openTelemetry.meterBuilder("instrumentation-library-name")
//            .setInstrumentationVersion("1.0.0")
//            .build();


  // Build counter e.g. LongCounter
//    LongCounter counter = meter
//            .counterBuilder("processed_jobs")
//            .setDescription("Processed HelloWorld Client jobs")
//            .setUnit("1")
//            .build();

  // It is recommended that the API user keep a reference to Attributes they will record against
//    Attributes attributes = Attributes.of(stringKey("Key"), "SomeWork");
//}

  private final GreeterGrpc.GreeterBlockingStub blockingStub;

  /** Construct client for accessing HelloWorld server using the existing channel. */
  public HelloWorldClient(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
    blockingStub = GreeterGrpc.newBlockingStub(channel);
  }

  /** Say hello to server. */
  public void greet(String name) {
    logger.info("Will try to greet " + name + " ...");
    HelloRequest request = HelloRequest.newBuilder().setName(name).build();
    HelloReply response;
    try {
      response = blockingStub.sayHello(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting. The second argument is the target server.
   */
  public static void main(String[] args) throws Exception {
    loggerHelperMMP ();
    String user = "world";
    // Access a service running on the local machine on port 50051
    String target = "localhost:50051";
    // Allow passing in the user and target strings as command line arguments
    if (args.length > 0) {
      if ("--help".equals(args[0])) {
        System.err.println("Usage: [name [target]]");
        System.err.println("");
        System.err.println("  name    The name you wish to be greeted by. Defaults to " + user);
        System.err.println("  target  The server to connect to. Defaults to " + target);
        System.exit(1);
      }
      user = args[0];
    }
    if (args.length > 1) {
      target = args[1];
    }

    // Create a communication channel to the server, known as a Channel. Channels are thread-safe
    // and reusable. It is common to create channels at the beginning of your application and reuse
    // them until the application shuts down.
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
        // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
        // needing certificates.
        .usePlaintext()
        .build();
    try {
      HelloWorldClient client = new HelloWorldClient(channel);
      logger.fine("Started");
      client.greet(user);
      counter.add(1, attributes);
    } finally {
      // ManagedChannels use resources like threads and TCP connections. To prevent leaking these
      // resources the channel should be shut down when it will no longer be used. If it may be used
      // again leave it running.
      logger.fine("Ended");
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
