package org.bip.spec;

import org.bip.annotations.ComponentType;
import org.bip.annotations.Data;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.Transition;
import org.bip.api.PortType;
import org.bip.api.DataOut.AccessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ports({ @Port(name = "a", type = PortType.enforceable), @Port(name = "b", type = PortType.enforceable)})
@ComponentType(initial = "zero", name = "org.bip.spec.TwoDataProvider1")
public class TwoDataProvider1 {
	Logger logger = LoggerFactory.getLogger(TwoDataProvider1.class);

	private int memoryY = 20;
	private int memoryQ = 90;

	public int noOfTransitions;

	@Transition(name = "a", source = "zero", target = "zero")
	public void componentBTransitionA() {
		logger.debug("Transition a of TwoDataProvider1 has been performed");
		noOfTransitions++;
	}

	@Transition(name = "b", source = "zero", target = "zero")
	public void componentBTransitionB() {
		logger.debug("Transition b of TwoDataProvider1 has been performed");
		noOfTransitions++;
	}

	@Data(name = "memoryY", accessTypePort = AccessType.any)
	public int memoryY() {
		return memoryY;
	}
	
	@Data(name = "memoryQ", accessTypePort = AccessType.any)
	public int memoryQ() {
		return memoryQ;
	}
}
