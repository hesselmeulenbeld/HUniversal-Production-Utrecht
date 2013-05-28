/**
 * @file ServiceAgent.java
 * @brief This agent manages services and oversees generation and scheduling of serviceSteps. It also handles
 *        communication with the logistics agent.
 * @date Created: 5 apr. 2013
 * 
 * @author Peter Bonnema
 * 
 * @section LICENSE
 *          License: newBSD
 * 
 *          Copyright © 2013, HU University of Applied Sciences Utrecht.
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
 * 
 **/
package rexos.mas.service_agent;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.net.UnknownHostException;
import java.util.HashMap;

import org.bson.types.ObjectId;

import rexos.libraries.blackboard_client.BlackboardClient;
import rexos.libraries.blackboard_client.BlackboardSubscriber;
import rexos.libraries.blackboard_client.FieldUpdateSubscription;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.blackboard_client.MongoOperation;
import rexos.libraries.blackboard_client.OplogEntry;
import rexos.libraries.log.Logger;
import rexos.mas.data.DbData;
import rexos.mas.equiplet_agent.ProductStep;
import rexos.mas.equiplet_agent.StepStatusCode;
import rexos.mas.service_agent.behaviours.CanDoProductStep;
import rexos.mas.service_agent.behaviours.GetProductStepDuration;
import rexos.mas.service_agent.behaviours.InitialisationFinished;
import rexos.mas.service_agent.behaviours.ScheduleStep;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;

/**
 * This agent manages services and oversees generation and scheduling of
 * serviceSteps. It also handles communication with the logistics agent.
 * 
 * @author Peter Bonnema
 * 
 */
public class ServiceAgent extends Agent implements BlackboardSubscriber {
	/**
	 * @var long serialVersionUID
	 *      The serialVersionUID for this class.
	 */
	private static final long serialVersionUID = -5730473679881365598L;

	/**
	 * @var BlackboardClient productStepBBClient
	 *      The BlackboardClient used to interact with the product steps
	 *      blackboard.
	 */
	private BlackboardClient productStepBBClient;

	/**
	 * @var BlackboardClient serviceStepBBClient
	 *      The BlackboardClient used to interact with the service steps
	 *      blackboard.
	 */
	private BlackboardClient serviceStepBBClient;

	/**
	 * @var FieldUpdateSubscription statusSubscription
	 *      The subscription object used to subscribe this agent on a blackboard
	 *      client to field updates on a blackboard.
	 */
	private FieldUpdateSubscription statusSubscription;

	/**
	 * @var DbData dbData
	 *      Contains connection information for mongoDB database containing the
	 *      productstep and servicestep blackboards.
	 */
	private DbData dbData;

	/**
	 * @var AID equipletAgentAID
	 *      The AID of the equiplet agent of this equiplet.
	 */
	private AID equipletAgentAID;

	/**
	 * @var AID hardwareAgentAID
	 *      The AID of the hardware agent of this equiplet.
	 */
	private AID hardwareAgentAID;

	/**
	 * @var AID logisticsAID
	 *      The AID of the logistics agent.
	 */
	private AID logisticsAID;

	/**
	 * @var ServiceFactory serviceFactory
	 *      The serviceFactory used to create instances of Service.
	 */
	private ServiceFactory serviceFactory;

	/**
	 * @var HashMap<String, Service> convIdServiceMapping
	 *      This maps conversation id's with service objects.
	 */
	private HashMap<String, Service> convIdServiceMapping;

	/**
	 * Initializes the agent. This includes creating and starting the hardware agent, creating and configuring two
	 * blackboard clients, subscribing to status updates and adding all behaviours.
	 */
	@Override
	public void setup() {
		Logger.log("I spawned as a service agent.");

		// handle arguments given to this agent
		Object[] args = getArguments();
		if(args != null && args.length > 0) {
			dbData = (DbData) args[0];
			equipletAgentAID = (AID) args[1];
			logisticsAID = (AID) args[2];
		}

		// Create a hardware agent for this equiplet
		Object[] arguments = new Object[] {
				dbData, equipletAgentAID, getAID()
		};
		try {
			AgentController hardwareAgentCnt =
					getContainerController().createNewAgent(equipletAgentAID.getLocalName() + "-hardwareAgent",
							"rexos.mas.hardware_agent.HardwareAgent", arguments);
			hardwareAgentCnt.start();
			hardwareAgentAID = new AID(hardwareAgentCnt.getName(), AID.ISGUID);
		} catch(StaleProxyException e) {
			Logger.log(e);
			doDelete();
		}

		try {
			// create blackboard clients, configure them and subscribe to status
			// changes of any steps
			productStepBBClient = new BlackboardClient(dbData.getIp());
			serviceStepBBClient = new BlackboardClient(dbData.getIp());
			statusSubscription = new FieldUpdateSubscription("status", this);

			productStepBBClient.setDatabase(dbData.getName());
			productStepBBClient.setCollection("ProductStepsBlackBoard");
			// Needs to react on state changes of production steps to WAITING
			productStepBBClient.subscribe(statusSubscription);

			serviceStepBBClient.setDatabase(dbData.getName());
			serviceStepBBClient.setCollection("ServiceStepsBlackBoard");
			// Needs to react on status changes
			serviceStepBBClient.subscribe(statusSubscription);
			serviceStepBBClient.removeDocuments(new BasicDBObject());
		} catch(UnknownHostException | GeneralMongoException | InvalidDBNamespaceException e) {
			Logger.log(e);
			doDelete();
		}

		convIdServiceMapping = new HashMap<String, Service>();
		serviceFactory = new ServiceFactory(equipletAgentAID.getLocalName());

		// Add behaviours
		addBehaviour(new CanDoProductStep(this, serviceFactory));
		addBehaviour(new GetProductStepDuration(this));
		addBehaviour(new ScheduleStep(this));
		addBehaviour(new InitialisationFinished(this));
	}

	/**
	 * Deinitializes the agent by unsubscribing to status field updates, emptying the service step blackboard and
	 * updating
	 * the status of all product steps. It also notifies the equiplet agent and hardware agent that this agent died.
	 */
	//@formatter:off
	@Override
	public void takeDown() {
		productStepBBClient.unsubscribe(statusSubscription);
		serviceStepBBClient.unsubscribe(statusSubscription);
		try {
			serviceStepBBClient.removeDocuments(new BasicDBObject());
			Logger.log("ServiceAgent takedown");

			DBObject update =
					BasicDBObjectBuilder.start("status", StepStatusCode.FAILED.name())
						.push("statusData")
							.add("source", "service agent")
							.add("reason", "died")
							.pop()
						.get();
			productStepBBClient.updateDocuments(new BasicDBObject(), new BasicDBObject("$set", update));

			ACLMessage message = new ACLMessage(ACLMessage.INFORM);
			message.addReceiver(equipletAgentAID);
			message.addReceiver(hardwareAgentAID);
			message.setOntology("ServiceAgentDied");
			send(message);
		} catch(InvalidDBNamespaceException | GeneralMongoException e) {
			Logger.log(e);
		}
	} //@formatter:on

	/**
	 * Maps the specified conversation id with the specified service object.
	 * Once a service object has been created (by the service factory) it's
	 * mapped with the conversation id of the scheduling negotiation of the
	 * corresponding product step. With this mapping behaviours that do not have
	 * a reference to a service object can still get one with
	 * GetServiceForConvId.
	 * 
	 * @param conversationId the conversation id to map the service with.
	 * @param service the service object that the conversation id will be mapped with.
	 */
	public void MapConvIdWithService(String conversationId, Service service) {
		convIdServiceMapping.put(conversationId, service);
	}

	/**
	 * Returns the service object mapped to the specified conversation id.
	 * 
	 * @param conversationId the conversation id to use to get the corresponding service object.
	 * @return the service object mapped to the specified conversation id.
	 */
	public Service GetServiceForConvId(String conversationId) {
		return convIdServiceMapping.get(conversationId);
	}

	/**
	 * Removes the mapping of the specified conversation id with the corresponding service object.
	 * 
	 * @param conversationId
	 */
	public void RemoveConvIdServiceMapping(String conversationId) {
		convIdServiceMapping.remove(conversationId);
	}

	/**
	 * This method is called by the blackboard client when certain (or any) CRUD operations are performed on the status
	 * field of a product-/servicestep on a blackboard. For now the service agent just passes the message on to another
	 * agent by updating the status field of the corresponding steps on the other blackboard. (e.g. changes the status
	 * of all service steps to ABORTED when the status of a product step is changed to ABORTED).
	 */
	@Override
	public void onMessage(MongoOperation operation, OplogEntry entry) {
		try {
			switch(entry.getNamespace().split("\\.")[1]) {
				case "ProductStepsBlackBoard":
					ProductStep productionStep =
							new ProductStep((BasicDBObject) productStepBBClient.findDocumentById(entry
									.getTargetObjectId()));
					switch(operation) {
						case UPDATE:
							StepStatusCode status = productionStep.getStatus();
							switch(status) {
								case WAITING:
									// TODO zet eerste serviceStep van productStep op waiting
									break;
								case ABORTED:
									serviceStepBBClient.updateDocuments(
											new BasicDBObject("productStepId", entry.getTargetObjectId()),
											new BasicDBObject("$set", new BasicDBObject("status", status).append(
													"statusData", productionStep.getStatusData())));
									break;
								default:
									break;
							}
							break;
						default:
							break;
					}
					break;
				case "ServiceStepsBlackBoard":
					ServiceStep serviceStep =
							new ServiceStep((BasicDBObject) serviceStepBBClient.findDocumentById(entry
									.getTargetObjectId()));
					ObjectId productStepId = serviceStep.getProductStepId();
					switch(operation) {
						case UPDATE:
							StepStatusCode status = serviceStep.getStatus();
							switch(status) {
								case DONE:
									if(serviceStep.getNextStep() != null) {
										serviceStepBBClient.updateDocuments(
												new BasicDBObject("_id", serviceStep.getNextStep()), new BasicDBObject(
														"$set", new BasicDBObject("status", StepStatusCode.WAITING.name())));
										break;
									}

									
									BasicDBObject log = new BasicDBObject("step0", serviceStep.toBasicDBObject());
									ObjectId currentStepId = serviceStep.getNextStep();
									BasicDBObject currentStep;
									int i = 1;
									while(currentStepId != null) {
										currentStep = (BasicDBObject) serviceStepBBClient.findDocumentById(currentStepId);
										log.append("step" + i++, currentStep);
										currentStepId = currentStep.getObjectId("nextStep");
									}

									productStepBBClient.updateDocuments(
											new BasicDBObject("_id", productStepId),
											new BasicDBObject("$set", new BasicDBObject("status", status).append(
													"statusData", log)));
									break;
								case IN_PROGRESS:
								case SUSPENDED_OR_WARNING:
								case FAILED:
									productStepBBClient.updateDocuments(
											new BasicDBObject("_id", productStepId),
											new BasicDBObject("$set", new BasicDBObject("status", status).append(
													"statusData", serviceStep.getStatusData())));
									break;
								default:
									break;
							}
							break;
						default:
							break;
					}
					break;
				default:
					break;
			}
		} catch(InvalidDBNamespaceException | GeneralMongoException e) {
			Logger.log(e);
			doDelete();
		}
	}

	/**
	 * the DbData containing connection info for the blackboard mongoDB database.
	 * 
	 * @return the DbData containing connection info for the blackboard mongoDB database.
	 */
	public DbData getDbData() {
		return dbData;
	}

	/**
	 * Returns the AID of the equipletAgent of this equiplet.
	 * 
	 * @return the AID of the equipletAgent of this equiplet.
	 */
	public AID getEquipletAgentAID() {
		return equipletAgentAID;
	}

	/**
	 * Returns the AID of the logisticsAgent.
	 * 
	 * @return the AID of the logisticsAgent.
	 */
	public AID getLogisticsAID() {
		return logisticsAID;
	}

	/**
	 * Returns the AID of the hardwareAgent of this equiplet.
	 * 
	 * @return the AID of the hardwareAgent of this equiplet.
	 */
	public AID getHardwareAgentAID() {
		return hardwareAgentAID;
	}

	/**
	 * Returns the productSteps blackboard client.
	 * 
	 * @return the productSteps blackboard client.
	 */
	public BlackboardClient getProductStepBBClient() {
		return productStepBBClient;
	}

	/**
	 * Returns the serviceSteps blackboard client.
	 * 
	 * @return the serviceSteps blackboard client.
	 */
	public BlackboardClient getServiceStepBBClient() {
		return serviceStepBBClient;
	}
}
