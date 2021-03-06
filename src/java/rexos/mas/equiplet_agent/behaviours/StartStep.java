/**
 * @file rexos/mas/equiplet_agent/behaviours/StartStep.java
 * @brief Behaviour for handling the messages with the ontology StartStep
 * @date Created: 2013-04-02
 * 
 * @author Hessel Meulenbeld
 * 
 * @section LICENSE
 *          License: newBSD
 * 
 *          Copyright � 2013, HU University of Applied Sciences Utrecht.
 *          All rights reserved.
 * 
 *          Redistribution and use in source and binary forms, with or without modification, are permitted provided that
 *          the following conditions are met:
 *          - Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *          following disclaimer.
 *          - Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *          following disclaimer in the documentation and/or other materials provided with the distribution.
 *          - Neither the name of the HU University of Applied Sciences Utrecht nor the names of its contributors may be
 *          used to endorse or promote products derived from this software without specific prior written permission.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *          "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *          THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *          ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED SCIENCES UTRECHT
 *          BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *          CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 *          GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *          HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *          LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *          OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/
package rexos.mas.equiplet_agent.behaviours;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import org.bson.types.ObjectId;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.FieldUpdateSubscription;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.blackboard_client.MongoOperation;
import rexos.libraries.blackboard_client.OplogEntry;
import rexos.libraries.blackboard_client.FieldUpdateSubscription.MongoUpdateLogOperation;
import rexos.libraries.log.Logger;
import rexos.mas.behaviours.ReceiveBehaviour;
import rexos.mas.data.EquipletState;
import rexos.mas.data.EquipletStateEntry;
import rexos.mas.data.StepStatusCode;
import rexos.mas.equiplet_agent.EquipletAgent;
import rexos.mas.equiplet_agent.NextProductStepTimer;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * Receive behaviour for receiving messages with the ontology: "StartStep".
 * Starts the product step linked to the conversationId.
 * Starts a timer for the next product step.
 */
public class StartStep extends ReceiveBehaviour implements BlackboardSubscriber {
	/**
	 * @var static final long serialVersionUID
	 *      The serial version UID for this class
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @var MessageTemplate messageTemplate
	 *      The messageTemplate this behaviour listens to.
	 *      This behaviour listens to the ontology: StartStep.
	 */
	private static MessageTemplate messageTemplate = MessageTemplate.MatchOntology("StartStep");

	/**
	 * @var EquipletAgent equipletAgent
	 *      The equipletAgent related to this behaviour.
	 */
	private EquipletAgent equipletAgent;

	private FieldUpdateSubscription stateUpdateSubscription;

	private ObjectId productStepId;

	/**
	 * Instantiates a new can perform step.
	 * 
	 * @param a The agent for this behaviour
	 * @param equipletBBClient The BlackboardClient for this equiplet's blackboard.
	 */
	public StartStep(Agent a) {
		super(a, messageTemplate);
		equipletAgent = (EquipletAgent) a;
		stateUpdateSubscription = new FieldUpdateSubscription("state", this);
		stateUpdateSubscription.addOperation(MongoUpdateLogOperation.SET);
	}

	/**
	 * Function to handle the incoming messages for this behaviour. Handles the response to the StartStep.
	 * 
	 * @param message The received message.
	 */
	@Override
	public void handle(ACLMessage message) {
		Logger.log("%s received message from %s (%s)%n", myAgent.getLocalName(), message.getSender().getLocalName(),
				message.getOntology());

		// Gets the productStepId and updates all the productsteps on the blackboard the status to waiting.
		try {
			ObjectId productStepId = equipletAgent.getRelatedObjectId(message.getConversationId());
			if(equipletAgent.getEquipletStateEntry().getEquipletState() != EquipletState.NORMAL) {
				Logger.log("%d Equiplet agent - changing state%n", EquipletAgent.getCurrentTimeSlot());

				//equipletAgent.getStateBBClient().subscribe(stateUpdateSubscription);
				//equipletAgent.setDesiredEquipletState(EquipletState.NORMAL);
				equipletAgent.getProductStepBBClient().updateDocuments(new BasicDBObject("_id", productStepId),
						new BasicDBObject("$set", new BasicDBObject("status", StepStatusCode.WAITING.name())));
				
				equipletAgent.getTimer().rescheduleTimer();
			} else {
				Logger.log("%d Equiplet agent - Starting prod. step.%n", EquipletAgent.getCurrentTimeSlot());
				equipletAgent.getProductStepBBClient().updateDocuments(new BasicDBObject("_id", productStepId),
						new BasicDBObject("$set", new BasicDBObject("status", StepStatusCode.WAITING.name())));
				
				equipletAgent.getTimer().rescheduleTimer();
			}
		} catch(InvalidDBNamespaceException | GeneralMongoException e1) {
			Logger.log(e1);
			//TODO handle error
		}


	}

	@Override
	public void onMessage(MongoOperation operation, OplogEntry entry) {
		try {
			BlackboardClient stateBBClient = equipletAgent.getStateBBClient();
			DBObject dbObject = stateBBClient.findDocumentById(entry.getTargetObjectId());
			if(dbObject != null) {
				EquipletStateEntry state = new EquipletStateEntry((BasicDBObject) dbObject);
				if(state.getEquipletState() == EquipletState.NORMAL) {
					Logger.log("%d Equiplet agent - equip. state changed to NORMAL. Starting prod. step.", EquipletAgent.getCurrentTimeSlot());

					equipletAgent.getProductStepBBClient().updateDocuments(new BasicDBObject("_id", productStepId),
							new BasicDBObject("$set", new BasicDBObject("status", StepStatusCode.WAITING.name())));

					equipletAgent.getTimer().rescheduleTimer();
					
					stateBBClient.unsubscribe(stateUpdateSubscription);
				}
			}
		} catch(InvalidDBNamespaceException | GeneralMongoException e) {
			e.printStackTrace();
			//TODO handle error
		}
	}
}
