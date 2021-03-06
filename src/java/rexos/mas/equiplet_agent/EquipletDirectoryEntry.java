/**
 * @file rexos/mas/equiplet_agent/EquipletDirectoryEntry.java
 * @brief Provides an entry class for the EquipletDirectory blackboard.
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
package rexos.mas.equiplet_agent;

import jade.core.AID;

import java.util.ArrayList;

import rexos.mas.data.DbData;
import rexos.mas.data.MongoSaveable;

import com.mongodb.BasicDBObject;

/**
 * Representation of an entry in the EquipletDirectory blackboard.
 **/
public class EquipletDirectoryEntry implements MongoSaveable {
	/**
	 * @var AID AID
	 * The AID of the equipletAgent
	 */
	private AID AID;

	/**
	 * @var ArrayList<Integer> capabilities
	 * The capabilities of the equipletAgent
	 */
	private ArrayList<Integer> capabilities;
	
	/**
	 * @var DbData db
	 * The information about the database of the equipletAgent.
	 */
	private DbData db;

	/**
	 * Constructor for an entry in the equipletDirectory.
	 * 
	 * @param AID the AID of the equipletAgent.
	 * @param capabilities the capabilities of the equipletAgent.
	 * @param db the information about the database of the equipletAgent.
	 */
	public EquipletDirectoryEntry(AID AID, ArrayList<Integer> capabilities, DbData db){
		this.AID = AID;
		this.capabilities = capabilities;
		this.db = db;
	}
	
	/**
	 * Constructor for an entry in the equipletDirectory.
	 * @param object BasicDBObject to fill this class with.
	 */
	public EquipletDirectoryEntry(BasicDBObject object){
		fromBasicDBObject(object);
	}
	
	/**
	 * Function to fill this class with a BasicDBObject.
	 */
	@Override
	public void fromBasicDBObject(BasicDBObject object){
		BasicDBObject copy = (BasicDBObject) object.copy();
		this.AID = new AID((String)copy.remove("AID"), jade.core.AID.ISGUID);
		this.capabilities = (ArrayList<Integer>) copy.remove("capabilities");
		this.db = new DbData((BasicDBObject)copy.remove("db"));
		if(!copy.isEmpty()){
			throw new IllegalArgumentException();
		}
	}
	
	/**
	 * Function to get the BasicDBObject of this class.
	 * @return BasicDBObject from this class
	 */
	@Override
	public BasicDBObject toBasicDBObject(){
		BasicDBObject entry = new BasicDBObject("AID", this.AID.getName());
		entry.put("capabilities", capabilities);
		entry.put("db", db.toBasicDBObject());
		return entry;
	}
	
	/**
	 * Getter for the AID
	 * @return the AID
	 */
	public AID getAID() {
		return AID;
	}
}
