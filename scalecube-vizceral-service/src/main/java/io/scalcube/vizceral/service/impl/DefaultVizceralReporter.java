package io.scalcube.vizceral.service.impl;

import io.scalecube.services.Microservices;
import io.scalecube.services.annotations.AfterConstruct;
import io.scalecube.services.annotations.Inject;

import io.scalcube.vizceral.service.api.VizceralConnection;
import io.scalcube.vizceral.service.api.VizceralReporter;

import org.jctools.maps.NonBlockingHashMap;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class DefaultVizceralReporter implements VizceralReporter {

  private String name;
  private Duration interval;

  private NonBlockingHashMap<String, VizceralConnection> connections = new NonBlockingHashMap<>();

  @Inject
  Microservices ms;
  private String displayName;

  public static class Builder {
    Duration interval = Duration.ofSeconds(5);
    private String displayName;

    DefaultVizceralReporter build() {
      return new DefaultVizceralReporter(this);
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }
  }

  @AfterConstruct
  public void start() {
    this.name = ms.id();
  }

  public static Builder builder() {
    return new Builder();
  }

  private DefaultVizceralReporter(Builder builder) {
    this.interval = builder.interval;
    this.displayName = builder.displayName;
  }

  @Override
  public Mono<String> name() {
    return Mono.just(displayName == null ? name : displayName);
  }

  @Override
  public Flux<VizceralConnection> metrics() {
    return Flux.interval(this.interval)
        .transform(transformer -> Flux.fromStream(connections.values().stream())
            .filter(untouched -> untouched.touch()));
  }

  public VizceralConnection target(String target) {
    return connections.computeIfAbsent(target,
        newTarget -> compute(this.name, newTarget));
  }

  private VizceralConnection compute(String source, String target) {
    return VizceralConnection.builder()
        .source(source)
        .target(target)
        .build();
  }
}
