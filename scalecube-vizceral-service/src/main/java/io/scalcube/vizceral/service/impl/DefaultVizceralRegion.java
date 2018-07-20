package io.scalcube.vizceral.service.impl;

import io.scalcube.vizceral.service.api.Node;
import io.scalcube.vizceral.service.api.ReporterEvent;
import io.scalcube.vizceral.service.api.VizceralConnection;
import io.scalcube.vizceral.service.api.VizceralNode;
import io.scalcube.vizceral.service.api.VizceralRegionService;

import org.jctools.maps.NonBlockingHashMap;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

public class DefaultVizceralRegion implements VizceralRegionService {

  private static final String INTERNET = "INTERNET";

  private DirectProcessor<VizceralNode> processor = DirectProcessor.create();

  final VizceralNode global;
  private String regionName;
  private Map<String, VizceralConnection> connections = new NonBlockingHashMap<>();

  public DefaultVizceralRegion(String regionName, String regionDisplayName) {
    this.regionName = regionName;
    global = VizceralNode.newGlobal().build();
    global.nodes().add(VizceralNode.newInternetRegion()
        .build());

    // REGION "US-EAST-1
    VizceralNode regoin = VizceralNode.newRegion(regionName, regionDisplayName)
        .maxVolume(50000)
        .updated(System.currentTimeMillis())
        .build();

    regoin.nodes().add(VizceralNode.focusedChild(INTERNET).build());
    global.nodes().add(regoin);

    global.connections().add(VizceralConnection
        .normal(INTERNET, regionName)
        .build());
  }

  public Flux<VizceralNode> listen() {
    return processor
        .interval(Duration.ofSeconds(5))
        .map(mapper -> global);
  }

  @Override
  public Flux<Boolean> registerReporter(Flux<ReporterEvent> events) {
    
    events.subscribe(request -> {
      Optional<VizceralNode> optionalRegion = findRegion(request.region());
      if (optionalRegion.isPresent()) {
        VizceralNode region = optionalRegion.get();
        if (request.node() != null) {
          addNode(region, request.node());
        }
        
        if (request.connection() != null) {
          VizceralConnection connection = connections.computeIfAbsent(request.connection().id(),
              newConnection -> addConnection(region, request.connection()));
          if (request.connection().metrics() != null) {
            connection.update(request.connection().metrics());
          }
        }
      }
      

    });
    
    return Flux.never();
  }

  private VizceralConnection addConnection(VizceralNode node, VizceralConnection connection) {
    node.connections().add(connection);
    return connection;
  }

  private boolean addNode(VizceralNode region, Node request) {
    return createNode(region, request.name(), request.displayName());
  }

  private boolean createNode(VizceralNode region, String name, String displayName) {

    if (region != null) {
      region.nodes().add(VizceralNode.focusedChild(name)
          .displayName(displayName)
          .build());

      return true;
    } else {
      throw new IllegalArgumentException("unknown region: " + regionName);
    }

  }

  private Optional<VizceralNode> findRegion(String region) {
    return global.nodes()
        .stream()
        .filter(item -> item.name().equals(region))
        .findFirst();
  }
}
