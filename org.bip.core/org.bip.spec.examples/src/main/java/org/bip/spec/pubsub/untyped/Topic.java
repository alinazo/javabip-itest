package org.bip.spec.pubsub.untyped;

import java.util.HashMap;
import java.util.HashSet;

import org.bip.annotations.ComponentType;
import org.bip.annotations.Data;
import org.bip.annotations.Port;
import org.bip.annotations.Ports;
import org.bip.annotations.Transition;
import org.bip.api.BIPActor;
import org.bip.api.PortType;

@Ports({ @Port(name = "getName", type = PortType.spontaneous), @Port(name = "addClient", type = PortType.spontaneous),
		@Port(name = "removeClient", type = PortType.spontaneous), @Port(name = "publish", type = PortType.spontaneous) })
@ComponentType(initial = "0", name = "org.bip.spec.pubsub.untyped.Topic")
public class Topic
{
    private String name;
    private HashSet<BIPActor> clients; 
    
    public Topic(String name) {

    	this.name = name;
        this.clients = new HashSet<BIPActor>();
    }

	@Transition(name = "addClient", source = "0", target = "0")
    public void addClient(@Data(name="client") BIPActor client) {
    	
        if(! clients.contains(client)){
        		
        	clients.add(client);
        	
        	try {
        		HashMap<String, Object> data = new HashMap<String, Object>();
        		data.put("topic", name);
        		client.inform("addTopic", data);
        	}
        	catch(Exception ex) {}

        }
    }

	@Transition(name = "removeClient", source = "0", target = "0")
	public void removeClient(@Data(name="client") BIPActor client) {

        if( clients.contains(client) ){
  
            this.clients.remove(client);
            
            try {
        		HashMap<String, Object> data = new HashMap<String, Object>();
        		data.put("topic", name);
        		client.inform("removeTopic", data);
            }
            catch (Exception ex) {
			}
       	}
    }

	@Transition(name = "publish", source = "0", target = "0")
	public void publish(@Data(name="client") BIPActor publishingClient, @Data(name="msg") String message) {

		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("msg", message);

		for (BIPActor currentClient : clients) {
        	try {
        		
        		currentClient.inform("write", data);

        	}
        	catch(Exception ex) {}
        }
		
    }
    
}
