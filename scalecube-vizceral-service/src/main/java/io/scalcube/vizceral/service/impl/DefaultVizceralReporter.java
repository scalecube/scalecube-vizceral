package io.scalcube.vizceral.service.impl;

import io.scalecube.cluster.membership.IdGenerator;
import io.scalecube.services.Microservices;
import io.scalecube.services.annotations.AfterConstruct;
import io.scalecube.services.annotations.Inject;

import io.scalcube.vizceral.service.api.Node;
import io.scalcube.vizceral.service.api.ReporterEvent;
import io.scalcube.vizceral.service.api.VizceralConnection;
import io.scalcube.vizceral.service.api.VizceralRegionService;
import io.scalcube.vizceral.service.api.VizceralReporter;

import org.jctools.maps.NonBlockingHashMap;

import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;

import java.time.Duration;

public class DefaultVizceralReporter implements VizceralReporter {

  private String name;
  private Duration interval;

  private NonBlockingHashMap<String, VizceralConnection> connections = new NonBlockingHashMap<>();

  @Inject
  Microservices ms;

  VizceralRegionService resionService;

  private String displayName;
  private String region;
  private String id;

  public static class Builder {
    Duration interval = Duration.ofSeconds(5);
    private String displayName;
    private String region;

    DefaultVizceralReporter build() {
      return new DefaultVizceralReporter(this);
    }

    public Builder region(String region) {
      this.region = region;
      return this;
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }
  }

  @AfterConstruct
  public void start() {
    this.name = ms.id();
    this.resionService = this.ms.call().create().api(VizceralRegionService.class);

    this.resionService.registerReporter(Flux.just(new ReporterEvent(this.region,
        ms.id(),
        new Node(this.region, this.name, this.displayName))))
        .subscribe();

    DirectProcessor<ReporterEvent> events = DirectProcessor.create();

    Flux.interval(Duration.ofSeconds(5)).subscribe(next -> {
      
      Flux.fromStream(connections.values().stream())
          .map(item -> new ReporterEvent(this.region, this.name(), item))
          .subscribe(event -> {
            events.onNext(event);
          });
    });

    this.resionService.registerReporter(events).subscribe();

  }

  public static Builder builder() {
    return new Builder();
  }

  private DefaultVizceralReporter(Builder builder) {
    this.interval = builder.interval;
    this.displayName = builder.displayName;
    this.region = builder.region;
  }


  public String name() {
    return name;
  }

  // inter
  public VizceralConnection inbound(String source) {
    return connections.computeIfAbsent(key(source, this.name),
        newSource -> compute(source, this.name));
  }

  // inter
  public VizceralConnection outbound(String target) {
    return connections.computeIfAbsent(key(this.name, target),
        newTarget -> compute(this.name, target));
  }

  private static String key(String source, String target) {
    return source + "=>" + target;
  }

  private VizceralConnection compute(String source, String target) {
    return VizceralConnection.builder()
        .source(source)
        .target(target)
        .build();
  }
}
