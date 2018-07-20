package io.scalcube.vizceral.service.impl;

import io.scalecube.services.Microservices;
import io.scalecube.services.ServiceCall;
import io.scalecube.services.ServiceReference;
import io.scalecube.services.annotations.AfterConstruct;
import io.scalecube.services.annotations.Inject;
import io.scalecube.services.api.ServiceMessage;
import io.scalecube.services.metrics.Metrics;
import io.scalecube.transport.Address;

import io.scalcube.vizceral.service.api.AddNodeRequest;
import io.scalcube.vizceral.service.api.UpdateMetricRequest;
import io.scalcube.vizceral.service.api.VizceralConnection;
import io.scalcube.vizceral.service.api.VizceralNode;
import io.scalcube.vizceral.service.api.VizceralRegionService;
import io.scalcube.vizceral.service.api.VizceralReporter;

import org.jctools.maps.NonBlockingHashMap;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultVizceralRegion implements VizceralRegionService {

  private static final String REPORTER_NAME_QUALIFIER = "/" + VizceralReporter.NAME + "/name";
  private static final ServiceMessage REPORTER_NAME_MESSAGE =
      ServiceMessage.builder().qualifier(REPORTER_NAME_QUALIFIER).build();


  private static final String REPORTER_METRICS_QUALIFIER = "/" + VizceralReporter.NAME + "/metrics";
  private static final ServiceMessage REPORTER_METRICS_MESSAGE =
      ServiceMessage.builder().qualifier(REPORTER_METRICS_QUALIFIER).build();


  private static final String INTERNET = "INTERNET";

  private DirectProcessor<VizceralNode> processor = DirectProcessor.create();


  @Inject
  Microservices microservices;

  @Inject
  Metrics metrics;
  
  final VizceralNode global;

  private String regionName;
  private Map<String, VizceralConnection> targets = new NonBlockingHashMap<>();

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

 
  Map<String, VizceralConnection> connectTo(String connectTo) {
    this.newRegionTargets("INTERNET", connectTo).forEach(target -> {
      targets.putIfAbsent(target.source(), target);
    });
    return this.targets;
  }

  @AfterConstruct
  void start() {
    System.err.println(metrics);
    ServiceCall reporter = microservices.call().create();
    List<ServiceReference> endpoints = microservices.serviceRegistry().lookupService(REPORTER_NAME_QUALIFIER);
        
    microservices.cluster().listenMembership().subscribe(event -> {
      if (event.isAdded()) {
        Flux.fromStream(endpoints.stream())
            .doOnNext(ref -> {
              Address address = Address.create(ref.host(), ref.port());
              reporter.requestOne(REPORTER_NAME_MESSAGE, String.class, address)
                  .subscribe(next -> {
                    createNode(regionName, ref.endpointId(), next.data());
                    reporter.requestMany(REPORTER_METRICS_MESSAGE, VizceralConnection.class, address)
                        .subscribe(onNext -> {
                          VizceralConnection cnn = onNext.data();
                          this.updateMetric(
                              UpdateMetricRequest.builder()
                                  .region(this.regionName)
                                  .source(cnn.source())
                                  .target(cnn.target())
                                  .metrics(cnn.metrics())
                                  .build());
                        });

                  });
            }).subscribe();
      } else if(event.isRemoved()) {
        List<ServiceReference> prev = microservices.serviceRegistry().lookupService(REPORTER_NAME_QUALIFIER);
        endpoints.forEach(ref->{
          if(!prev.contains(ref)) {
            global.remove(ref.endpointId());
          }
        });
      }
    });
  }

  public Flux<VizceralNode> listen() {
    return processor
        .interval(Duration.ofSeconds(5))
        .map(mapper -> global);
  }

  public Mono<Boolean> addNode(AddNodeRequest request) {
    return Mono.just(createNode(request.region(), request.name(), request.displayName()));
  }

  private boolean createNode(String regionName, String name, String displayName) {
    if (findNode(regionName, name).isPresent()) {
      return true;
    } else {
      Optional<VizceralNode> region = findRegion(regionName);
      if (region.isPresent()) {
        region.get().nodes().add(VizceralNode.focusedChild(name)
            .displayName(displayName)
            .build());

        return true;
      } else {
        throw new IllegalArgumentException("unknown region: " + regionName);
      }
    }
  }

  public List<VizceralConnection> newRegionTargets(String source, String target) {
    VizceralNode region = findRegion(this.regionName).get();
    return region.nodes().stream()
        .filter(predicate -> target.equals(predicate.displayName()))
        .map(next -> VizceralConnection.builder()
            .source(source)
            .target(next.name())
            .build())
        .map(node -> {
          region.connections().add(node);
          return node;
        }).collect(Collectors.toList());

  }

  public List<VizceralConnection> targets(String target) {
    VizceralNode region = findRegion(this.regionName).get();
    List<VizceralNode> targets = region.nodes().stream()
        .filter(predicate -> target.equals(predicate.displayName()))
        .collect(Collectors.toList());

    return targets.stream().map(mapper -> findConnection(region, this.regionName, mapper.name()))
        .filter(predicate -> predicate.isPresent())
        .map(mapper -> mapper.get())
        .collect(Collectors.toList());

  }

  public Mono<Boolean> updateMetric(UpdateMetricRequest request) {

    Optional<VizceralNode> node = findRegion(request.region());
    if (node.isPresent()) {
      Optional<VizceralConnection> cnn = findConnection(node.get(), request.source(), request.target());
      if (cnn.isPresent()) {
        cnn.get().update(request.metrics());
        return Mono.just(true);
      } else {
        node.get().connections().add(VizceralConnection.builder()
            .source(request.source())
            .target(request.target())
            .metrics(request.metrics())
            .build());
        return Mono.just(true);
      }
    }
    return Mono.just(false);
  }

  Optional<VizceralConnection> findConnection(VizceralNode node, String source, String target) {
    return node.connections().stream().filter(cnn -> cnn.source().equals(source) &&
        cnn.target().equals(target)).findFirst();


  }

  Optional<VizceralNode> findNode(String region, String nodeName) {
    Optional<VizceralNode> opRegion = findRegion(region);
    if (opRegion.isPresent()) {
      return opRegion.get().nodes().stream().filter(item -> item.name().equals(nodeName)).findFirst();
    } else {
      return Optional.empty();
    }
  }

  Optional<VizceralNode> findRegion(String region) {
    return global.nodes()
        .stream()
        .filter(item -> item.name().equals(region))
        .findFirst();
  }



}
