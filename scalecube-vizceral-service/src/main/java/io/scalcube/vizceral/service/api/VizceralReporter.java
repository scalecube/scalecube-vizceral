package io.scalcube.vizceral.service.api;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service(VizceralReporter.NAME)
public interface VizceralReporter {

  public static String NAME = "io.scalecube.vizceral.reporter";

  @ServiceMethod
  Flux<VizceralConnection> metrics();

  @ServiceMethod
  Mono<String> name();
}
