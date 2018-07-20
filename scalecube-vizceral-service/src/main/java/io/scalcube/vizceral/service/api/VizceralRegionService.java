package io.scalcube.vizceral.service.api;

import io.scalecube.services.annotations.Service;
import io.scalecube.services.annotations.ServiceMethod;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Service("io.scalecube.vizceral.region")
public interface VizceralRegionService {
  
  @ServiceMethod
  Flux<VizceralNode> listen();
  
  @ServiceMethod
  public Mono<Boolean> updateMetric(UpdateMetricRequest request);
  
  @ServiceMethod
  public Mono<Boolean> addNode(AddNodeRequest request);
  
}
