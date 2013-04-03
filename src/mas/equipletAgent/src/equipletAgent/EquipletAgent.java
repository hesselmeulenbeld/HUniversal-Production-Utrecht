/**
 * @file EquipletAgent.java
 * @brief Provides an equiplet agent that communicates with product agents and with its own service agent.
 * @date Created: 2013-04-02
 *
 * @author Hessel Meulenbeld
 *
 * @section LICENSE
 * License: newBSD
 *
 * Copyright � 2013, HU University of Applied Sciences Utrecht.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 * - Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package equipletAgent;

import com.mongodb.*;
import com.google.gson.Gson;

import jade.core.Agent;
import nl.hu.client.BlackboardClient;

import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * EquipletAgent that communicates with product agents and with its own service agent.
 **/
public class EquipletAgent extends Agent {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	//This is the collective database used by all product agents and equiplets and contains the collection EquipletDirectory.
	private DB collectiveDb = null;
	private String collectiveDbIp = "localhost";
	private int collectiveDbPort = 27017;
	private String collectiveDbName = "CollectiveDb";
	//private DBCollection equipletDirectory = null;
	private String equipletDirectoryName = "EquipletDirectory";
	
    //This is the database specific for this equiplet, this database contains all the collections of the equiplet, service and hardware agents.
	private DB equipletDb = null;
	private String equipletDbIp = "localhost";
	private int equipletDbPort = 27017;
	private String equipletDbName = "";
	private DBCollection productSteps = null;
	private String productStepsName = "ProductSteps";
	
	private BlackboardClient client;
	
    public void setup() {
    	
    	//TODO: Reconfiguration
    	/*//If there are any arguments, put them in the database_ip and the database_port
    	Object[] args = getArguments();
    	if (args != null && args.length > 0){
			database_ip = (String)args[0];
			if(args.length > 1){
				database_port = (int)args[1];
			}
		}*/
    	
    	//set the database name to the name of the equiplet
    	equipletDbName = getAID().getLocalName();
    	Gson gson = new Gson();
    	try {
			Mongo collectiveDbMongoClient = new Mongo(collectiveDbIp, collectiveDbPort);
			collectiveDb = collectiveDbMongoClient.getDB(collectiveDbName);
			
			client = new BlackboardClient(collectiveDbIp, null);
	        try {
	            client.setDatabase(collectiveDbName);
	            client.setCollection(equipletDirectoryName);
	            
	            ArrayList<Long> capabilities = new ArrayList<Long>();
	            capabilities.add(5l);
	            capabilities.add(3l);
	            DbData dbData = new DbData();
	            EquipletDirectoryMessage entry = new EquipletDirectoryMessage(getAID(), capabilities, dbData);
	            client.insertJson(gson.toJson(entry));
	        } catch (Exception e) {
	            this.doDelete();
	        }
			//TODO: write to Collection EquipletDirectory
			collectiveDbMongoClient.close();
			
			Mongo equipletDbMongoClient = new Mongo(equipletDbIp, equipletDbPort);
			equipletDb = equipletDbMongoClient.getDB(equipletDbName);
			productSteps = equipletDb.getCollection(productStepsName);
			equipletDbMongoClient.close();
		} catch (UnknownHostException e1) {
			this.doDelete();
		}
    }
    
    public void takeDown(){
		try {
			Mongo collectiveDbMongoClient = new Mongo(collectiveDbIp, collectiveDbPort);
			collectiveDb = collectiveDbMongoClient.getDB(collectiveDbName);
			//TODO: delete own data of Collection EquipletDirectory
			collectiveDbMongoClient.close();
			
			//TODO: message to PA's
			
			Mongo equipletDbMongoClient = new Mongo(equipletDbIp, equipletDbPort);
			equipletDb = equipletDbMongoClient.getDB(equipletDbName);
			equipletDb.dropDatabase();
			equipletDbMongoClient.close();
		} catch (UnknownHostException e) {}
		
    }
}