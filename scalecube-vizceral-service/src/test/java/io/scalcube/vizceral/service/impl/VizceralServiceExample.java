package io.scalcube.vizceral.service.impl;

import io.scalecube.gateway.rsocket.websocket.RSocketWebsocketServer;
import io.scalecube.services.Microservices;

import com.codahale.metrics.MetricRegistry;

import io.scalcube.vizceral.service.api.AddNodeRequest;
import io.scalcube.vizceral.service.api.UpdateMetricRequest;
import io.scalcube.vizceral.service.api.VizceralMetric;
import io.scalcube.vizceral.service.api.VizceralRegionService;

public class VizceralServiceExample {

  public static void main(String[] args) throws InterruptedException {

    DefaultVizceralRegion region = new DefaultVizceralRegion("us-east-1", "US-EAST-1");
    DefaultVizceralReporter apiGateway1 = DefaultVizceralReporter.builder().displayName("api-gateway").build();
    DefaultVizceralReporter apiGateway2 = DefaultVizceralReporter.builder().displayName("api-gateway").build();

    DefaultVizceralReporter marketData1 = DefaultVizceralReporter.builder().displayName("MarketData").build();
    DefaultVizceralReporter marketData2 = DefaultVizceralReporter.builder().displayName("MarketData").build();

    Microservices seed = Microservices.builder().services(apiGateway1).startAwait();
    Microservices.builder().services(apiGateway2).seeds(seed.cluster().address()).startAwait();
    
    Microservices.builder().services(marketData1).seeds(seed.cluster().address()).startAwait();
    Microservices.builder().services(marketData2).seeds(seed.cluster().address()).startAwait();
    
    
    Microservices.builder().services(region)
      .metrics(new MetricRegistry())
      .seeds(seed.cluster()
          .address()).startAwait();

    apiGateway1.target("INTERNET").update(new VizceralMetric(1000.1, 10.2));
    apiGateway2.target("INTERNET").update(new VizceralMetric(1000.1, 10.2));

    region.connectTo("api-gateway");

    // VizceralRegionService service = ms.call().create().api(VizceralRegionService.class);
    //
    //
    // service.addNode(new AddNodeRequest("us-east-1", "3", "api-gateway")).subscribe();
    // service.addNode(new AddNodeRequest("us-east-1", "1", "market-data")).subscribe();
    // service.addNode(new AddNodeRequest("us-east-1", "2", "trades")).subscribe();
    //
    // service.updateMetric(UpdateMetricRequest.builder()
    // .region("us-east-1")
    // .source("INTERNET")
    // .target("api-gateway")
    // .metrics(new VizceralMetric(11150.23, 11.1))
    // .build()).subscribe();
    //
    // service.updateMetric(UpdateMetricRequest.builder()
    // .region("us-east-1")
    // .source("3")
    // .target("INTERNET")
    // .metrics(new VizceralMetric(11150.23, 11.1))
    // .build()).subscribe();
    //
    // service.updateMetric(UpdateMetricRequest.builder()
    // .region("us-east-1")
    // .source("3")
    // .target("1")
    // .metrics(new VizceralMetric(1250.23, 11.1))
    // .build()).subscribe();
    //
    // service.updateMetric(UpdateMetricRequest.builder()
    // .region("us-east-1")
    // .source("1")
    // .target("2")
    // .metrics(new VizceralMetric(1250.23, 11.1))
    // .build()).subscribe();
    //
    // service.updateMetric(UpdateMetricRequest.builder()
    // .region("us-east-1")
    // .source("2")
    // .target("3")
    // .metrics(new VizceralMetric(250.23, 11.1))
    // .build()).subscribe();

    RSocketWebsocketServer gateway = new RSocketWebsocketServer(seed, 9090);

    gateway.start();

    Runtime.getRuntime().addShutdownHook(new Thread(gateway::stop));

    Thread.currentThread().join();

  }

}
