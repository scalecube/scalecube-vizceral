package io.scalcube.vizceral.service.impl;

import io.scalecube.gateway.rsocket.websocket.RSocketWebsocketServer;
import io.scalecube.services.Microservices;

import com.codahale.metrics.MetricRegistry;

import io.scalcube.vizceral.service.api.VizceralMetric;

public class VizceralServiceExample {

  public static void main(String[] args) throws InterruptedException {

    DefaultVizceralRegion region = new DefaultVizceralRegion("us-east-1", "US-EAST-1");
    Microservices seed = Microservices.builder().services(region)
        .metrics(new MetricRegistry())
        .startAwait();

    DefaultVizceralReporter apiGateway1 = node("us-east-1", "api-gateway");
    DefaultVizceralReporter apiGateway2 = node("us-east-1", "api-gateway");

    DefaultVizceralReporter marketData1 = node("us-east-1", "MarketData");
    DefaultVizceralReporter marketData2 = node("us-east-1", "MarketData");

    Microservices.builder().services(apiGateway1).seeds(seed.cluster().address()).startAwait();
    Microservices.builder().services(apiGateway2).seeds(seed.cluster().address()).startAwait();
    Microservices.builder().services(marketData1).seeds(seed.cluster().address()).startAwait();
    Microservices.builder().services(marketData2).seeds(seed.cluster().address()).startAwait();

    link(apiGateway1, "INTERNET");
    link(apiGateway2, "INTERNET");

    link(apiGateway1, marketData1);
    link(apiGateway1, marketData2);

    link(apiGateway2, marketData1);
    link(apiGateway2, marketData2);

    RSocketWebsocketServer gateway = new RSocketWebsocketServer(seed, 9090);
    gateway.start();

    Runtime.getRuntime().addShutdownHook(new Thread(gateway::stop));
    Thread.currentThread().join();

  }

  private static DefaultVizceralReporter node(String region, String name) {
    return DefaultVizceralReporter.builder().region(region).displayName(name).build();
  }

  private static void link(DefaultVizceralReporter source, DefaultVizceralReporter target) {
    link(source, target.name());
  }

  private static void link(DefaultVizceralReporter source, String target) {
    source.inbound(target).update(new VizceralMetric(110.0, 10.0));
    source.outbound(target).update(new VizceralMetric(110.0, 10.0));
  }
}
