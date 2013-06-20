/**
 * @file rexos/mas/equiplet_agent/behaviours/InitialisationFinished.java
 * @brief Behaviour for handling the messages with the ontology
 *        InitialisationFinished
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
 *          Redistribution and use in source and binary forms, with or without
 *          modification, are permitted provided that the following conditions
 *          are met:
 *          - Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 *          - Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 *          - Neither the name of the HU University of Applied Sciences Utrecht
 *          nor the names of its contributors may be used to endorse or promote
 *          products derived from this software without specific prior written
 *          permission.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *          "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *          LIMITED TO,
 *          THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 *          PARTICULAR PURPOSE
 *          ARE DISCLAIMED. IN NO EVENT SHALL THE HU UNIVERSITY OF APPLIED
 *          SCIENCES UTRECHT
 *          BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *          OR
 *          CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *          SUBSTITUTE
 *          GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *          INTERRUPTION)
 *          HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *          STRICT
 *          LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *          ANY WAY OUT
 *          OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *          SUCH DAMAGE.
 **/
package rexos.mas.equiplet_agent.behaviours;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import rexos.libraries.blackboard_client.GeneralMongoException;
import rexos.libraries.blackboard_client.InvalidDBNamespaceException;
import rexos.libraries.log.Logger;
import rexos.mas.behaviours.ReceiveOnceBehaviour;
import rexos.mas.equiplet_agent.EquipletAgent;
import rexos.mas.equiplet_agent.EquipletDirectoryEntry;

/**
 * A receive once behaviour for receiving messages with ontology: "InitialisationFinished".
 * When the message is received the equiplet agent of this behaviour posts itself on the EquipletDirectory 
 * to advertise itself for the product agents.
 */
public class InitialisationFinished extends ReceiveOnceBehaviour {
	/**
	 * @var static final long serialVersionUID
	 *      The serial version UID for this class
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @var MessageTemplate messageTemplate
	 *      The messageTemplate this behaviour listens to. This behaviour
	 *      listens to the ontology: InitialisationFinished.
	 */
	private static MessageTemplate messageTemplate = MessageTemplate
			.MatchOntology("InitialisationFinished");

	/**
	 * @var EquipletAgent equipletAgent
	 *      The equipletAgent related to this behaviour.
	 */
	private EquipletAgent equipletAgent;

	/**
	 * Instantiates a new can perform step.
	 * 
	 * @param a
	 *            The agent for this behaviour
	 * @param collectiveBBClient
	 *      BlackboardClient for the collective blackboard.
	 */
	public InitialisationFinished(EquipletAgent a) {
		super(a, 2000, messageTemplate);
		equipletAgent = a;
	}

	/**
	 * Function to handle the incoming messages for this behaviour. Handles the
	 * response to the InitialisationFinished.
	 * 
	 * @param message
	 *            The received message.
	 */
	@Override
	public void handle(ACLMessage message) {
		if (message != null) {
			Logger.log("%s received message from %s%n", myAgent.getLocalName(),
					message.getSender().getLocalName(), message.getOntology());

			// inserts himself on the collective blackboard equiplet directory.
			EquipletDirectoryEntry entry = new EquipletDirectoryEntry(
					equipletAgent.getAID(), equipletAgent.getCapabilities(),
					equipletAgent.getDbData());
			try {
				equipletAgent.getCollectiveBBClient().insertDocument(entry.toBasicDBObject());
			} catch (InvalidDBNamespaceException | GeneralMongoException e) {
				Logger.log(e);
				equipletAgent.doDelete();
			}

			// starts the behaviour for receiving messages with the Ontology
			// CanPerformStep.
			equipletAgent.addBehaviour(new CanPerformStep(equipletAgent,
					equipletAgent.getProductStepBBClient()));

			// starts the behaviour for receiving messages with the Ontology
			// GetProductionDuration.
			equipletAgent
					.addBehaviour(new GetProductionDuration(equipletAgent));

			// starts the behaviour for receiving messages with the Ontology
			// ScheduleStep.
			equipletAgent.addBehaviour(new ScheduleStep(equipletAgent, equipletAgent.getProductStepBBClient()));

			// starts the behaviour for receiving messages with the Ontology
			// StartStep.
			equipletAgent.addBehaviour(new StartStep(equipletAgent,
					equipletAgent.getProductStepBBClient()));
		} else {
			Logger.log(equipletAgent.getName()
					+ " - InitialisationFinished timeout!");
			equipletAgent.doDelete();
		}
	}
}
