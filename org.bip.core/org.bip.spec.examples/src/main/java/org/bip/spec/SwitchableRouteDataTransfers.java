/*
 * Copyright (c) 2012 Crossing-Tech TM Switzerland. All right reserved.
 * Copyright (c) 2012, RiSD Laboratory, EPFL, Switzerland.
 *
 * Author: Simon Bliudze, Alina Zolotukhina, Anastasia Mavridou, and Radoslaw Szymanek
 * Date: 10/15/12
 */

package org.bip.spec;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.RoutePolicy;
import org.bip.annotations.bipComponentType;
import org.bip.annotations.bipData;
import org.bip.annotations.bipGuard;
import org.bip.annotations.bipPort;
import org.bip.annotations.bipPorts;
import org.bip.annotations.bipTransition;
import org.bip.api.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * [DONE] ensure guards atomicity There is a potential problem that isFinished and notFinished are executing on alive separate thread and they both
 * can give answer true if called in order notFinished, isFinished and in between the calls the thread changes. How we can protect against it, or
 * discover the situation and re-evaluate guards again? State machine assumes some invariants among guards which may be violated due to race
 * conditions and unstable state of Camel context when queried.
 * 
 */

// TODO all the usecases should implement MutableIdentification

@bipPorts({ @bipPort(name = "end", type = "spontaneous"), @bipPort(name = "on", type = "enforceable"), @bipPort(name = "off", type = "enforceable"), @bipPort(name = "finished", type = "enforceable") })
@bipComponentType(initial = "off", name = "org.bip.spec.SwitchableRouteDataTransfers")
public class SwitchableRouteDataTransfers implements CamelContextAware, InitializingBean, DisposableBean {

	public ModelCamelContext camelContext;

	public String routeId;

	private boolean workDone = false;
	Logger logger = LoggerFactory.getLogger(SwitchableRouteDataTransfers.class);

	private Executor executor;
	private RoutePolicy notifier;

	private int deltaMemory = 100;

	public void setCamelContext(CamelContext camelContext) {
		// TODO, find a better way to obtain ModelCamelContext, instead of relaying that DefaultCamelContext is provided.
		this.camelContext = (ModelCamelContext) camelContext;
	}

	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

	public CamelContext getCamelContext() {
		return camelContext;
	}

	public SwitchableRouteDataTransfers(String routeId) {
		this.routeId = routeId;
	}

	/**
	 * In some cases you may want to execute
	 */
	// @bipPort(name="end", type="spontaneous")
	public void workDone() {
		logger.debug("Port handler for end port is executing.");
		workDone = true;
	}

	/*
	 * Check what are the conditions for throwing the exception.
	 */
	@bipTransition(name = "off", source = "on", target = "wait", guard = "")
	public void stopRoute() throws Exception {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.debug("Stop transition handler for {} is being executed.", routeId);
		camelContext.suspendRoute(routeId);

	}

	@bipTransition(name = "end", source = "wait", target = "done", guard = "!isFinished")
	public void spontaneousEnd() {
		logger.debug("Received end notification for the route {}.", routeId);
	}

	@bipTransition(name = "", source = "wait", target = "done", guard = "isFinished")
	public void internalEnd() {
		logger.debug("Transitioning to done state directly since work within {} is already finished.", routeId);
	}

	@bipTransition(name = "finished", source = "done", target = "off", guard = "")
	public void finishedTransition() {
		logger.debug("Transitioning to off state from done for {}.", routeId);
	}

	@bipTransition(name = "on", source = "off", target = "on", guard = "")
	public void startRoute() throws Exception {
		logger.debug("Start transition handler for {} is being executed.", routeId);
		camelContext.resumeRoute(routeId);
	}

	@bipGuard(name = "isFinished")
	public boolean isFinished() {
		return camelContext.getInflightRepository().size(routeId) == 0;
	}

	// It is inferred that this is Data Out as the annotation is provided within a function that returns smth. The
	// type of the DataOut is the type of the return.
	@bipData(name = "deltaMemoryOnTransition", accessTypePort = "allowed", ports = { "on", "finished" })
	public int deltaMemoryOnTransition() {
		return deltaMemory;
	}

	public void afterPropertiesSet() throws Exception {
		RouteDefinition routeDefinition = camelContext.getRouteDefinition(routeId);

		if (routeDefinition == null)
			throw new IllegalStateException("The route with a given id " + routeId + " can not be found in the CamelContext.");

		List<RoutePolicy> routePolicyList = routeDefinition.getRoutePolicies();

		if (routePolicyList == null) {
			routePolicyList = new ArrayList<RoutePolicy>();
		}
		final Executor finalExecutor = executor;
		notifier = new RoutePolicy() {

			public void onInit(Route route) {
			}

			public void onExchangeBegin(Route route, Exchange exchange) {
			}

			public void onExchangeDone(Route route, Exchange exchange) {
				finalExecutor.inform("end");
			}

			@Override
			public void onRemove(Route arg0) {
			}

			@Override
			public void onResume(Route arg0) {
			}

			@Override
			public void onStart(Route arg0) {
			}

			@Override
			public void onStop(Route arg0) {
			}

			@Override
			public void onSuspend(Route arg0) {
			}
		};

		routePolicyList.add(notifier);
		routeDefinition.setRoutePolicies(routePolicyList);

	}

	public void destroy() throws Exception {

		RouteDefinition routeDefinition = camelContext.getRouteDefinition(routeId);

		if (routeDefinition != null) {

			List<RoutePolicy> routePolicyList = routeDefinition.getRoutePolicies();

			routePolicyList.remove(notifier);
			routeDefinition.setRoutePolicies(routePolicyList);

		}

	}

}
