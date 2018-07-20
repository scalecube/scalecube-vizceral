package io.scalcube.vizceral.service.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class VizceralNode {

  @JsonProperty(value = "class")
  ClassType clazz;

  RendererType renderer;
  String name;
  Integer maxVolume;
  Long updated;
  Long serverUpdateTime;

  List<VizceralNode> nodes;
  List<VizceralConnection> connections;

  private String displayName;

  public static class Builder {

    RendererType renderer = RendererType.global;
    String name = "edge";
    ClassType clazz = ClassType.normal;
    
    String displayName;
    Long serverUpdateTime = null;
    Long updated = null;
    Integer maxVolume = null;
    
    List<VizceralNode> nodes = new CopyOnWriteArrayList();
    List<VizceralConnection> connections = new CopyOnWriteArrayList();
   
    public Builder renderer(RendererType rendererType) {
      this.renderer = rendererType;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder nodes(List<VizceralNode> nodes) {
      this.nodes = nodes;
      return this;
    }

    public Builder connections(List<VizceralConnection> connections) {
      this.connections = connections;
      return this;
    }

    public Builder serverUpdateTime(Long serverUpdateTime) {
      this.serverUpdateTime = serverUpdateTime;
      return this;
    }

    public VizceralNode build() {
      return new VizceralNode(this);
    }

    public Builder displayName(String displayName) {
      this.displayName = displayName;
      return this;
    }

    public Builder clazz(ClassType type) {
      this.clazz = type;
      return this;
    }

    public Builder updated(long updated) {
      this.updated = updated;
      return this;
    }

    public Builder maxVolume(int maxVolume) {
      this.maxVolume = maxVolume;
      return this;
    }

  }
  
  public VizceralNode() {}
  
  public VizceralNode(Builder builder) {
    this.renderer = builder.renderer;
    this.name = builder.name;
    this.displayName = builder.displayName;
    this.nodes = builder.nodes;
    this.connections = builder.connections;
    this.serverUpdateTime = builder.serverUpdateTime;
    this.clazz = builder.clazz;
    this.updated = builder.updated;
    this.maxVolume = builder.maxVolume;
  }

  public RendererType renderer() {
    return this.renderer;
  }

  public String displayName() {
    return this.displayName;
  }

  public String name() {
    return this.name;
  }

  public List<VizceralNode> nodes() {
    return this.nodes;
  }

  public List<VizceralConnection> connections() {
    return this.connections;
  }

  public Long updated() {
    return this.updated;
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public void remove(String name) {
    List<VizceralNode> nodesToRemove = this.nodes().stream().filter(node->node.name().equals(name)).collect(Collectors.toList()); 
    nodesToRemove.stream().map(delete->nodes.remove(delete));
    
    List<VizceralConnection> connectionsToRemove = this.connections().stream().filter(node->node.source.equals(name)).collect(Collectors.toList());
    connectionsToRemove.stream().map(delete->nodes.remove(delete));
  }
  
  public static Builder newRegion(String name, String displayName) {
    return VizceralNode.builder()
        .renderer(RendererType.region)
        .displayName(displayName)
        .name(name)
        .clazz(ClassType.normal);
  }

  public static Builder newGlobal() {
    return VizceralNode.builder()
        .renderer(RendererType.global)
        .name("edge");
  }
  
  public static Builder newInternetRegion() {
    return VizceralNode.builder()
        .renderer(RendererType.region)
        .name("INTERNET")
        .clazz(ClassType.normal);
  }

  public static Builder focusedChild(String name) {
    return VizceralNode.builder()
        .name(name)
        .renderer(RendererType.focusedChild)
        .clazz(ClassType.normal);
  }
  
  @Override
  public String toString() {
    return "VizceralNode [clazz=" + clazz + ", renderer=" + renderer + ", name=" + name + ", maxVolume=" + maxVolume
        + ", updated=" + updated + ", serverUpdateTime=" + serverUpdateTime + ", nodes=" + nodes + ", connections="
        + connections + ", displayName=" + displayName + "]";
  }

 

}
