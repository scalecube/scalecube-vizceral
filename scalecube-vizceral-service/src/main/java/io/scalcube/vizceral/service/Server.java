package io.scalcube.vizceral.service;

import io.scalecube.services.Microservices;

public class Server {

  public static void main(String[] args) {
    
    Microservices.builder().startAwait();
    
    
  }

}
