package org.bip.spec;

import org.bip.annotations.ComponentType;
import org.bip.annotations.Data;
import org.bip.annotations.Guard;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.Transition;
import org.bip.api.PortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ports({ @Port(name = "getData", type = PortType.enforceable), 
		 @Port(name = "useData", type = PortType.enforceable) })
@ComponentType(initial = "zero", name = "org.bip.spec.Consumer")
public class Consumer {
	Logger logger = LoggerFactory.getLogger(Consumer.class);
	
	final private int memoryLimit;

	private int currentCapacity = 0;

	public int noOfTransitions = 0;
	
	public Consumer(int memoryLimit) {
		this.memoryLimit = memoryLimit;
	}

	@Transition(name = "getData", source = "zero", target = "one", guard = "hasCapacity")
	public void addRoute(@Data(name = "memoryUsage") Integer deltaMemory) {
		currentCapacity += deltaMemory;
		logger.debug("Current capacity: " + currentCapacity + ", limit: " + memoryLimit);
		noOfTransitions++;		
	}

	@Transition(name = "useData", source = "one", target = "zero")
	public void addRoute() {
		currentCapacity = 0;
		logger.debug("Current capacity: " + currentCapacity + ", limit: " + memoryLimit);
		noOfTransitions++;		
	}

	@Guard(name = "hasCapacity")
	public boolean hasCapacity(@Data(name = "memoryUsage") Integer memoryUsage) {
		logger.debug("currentCapacity + memoryUsage < memoryLimit: " + currentCapacity + " + " + memoryUsage + " < " + memoryLimit + " " + (currentCapacity + memoryUsage < memoryLimit));
		return currentCapacity + memoryUsage < memoryLimit;
	}

}
