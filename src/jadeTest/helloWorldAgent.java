package jadeTest;

import jade.core.Agent;

public class helloWorldAgent extends Agent {

  protected void setup() {
  	System.out.println("Hello World! My name is "+getLocalName());
  	//doDelete();
  } 
}

