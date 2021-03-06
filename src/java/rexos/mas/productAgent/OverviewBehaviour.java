/**
 * @file rexos/mas/productAgent/OverviewBehaviour.java
 * @brief Behaviour in which the product agent calls upon all subbehaviours
 * @date Created: 08-04-2013
 * 
 * @author Alexander Streng
 * @author Mike Schaap
 * @author Arno Derks
 * 
 *         Copyright � 2013, HU University of Applied Sciences Utrecht. All
 *         rights reserved.
 * 
 *         Redistribution and use in source and binary forms, with or without
 *         modification, are permitted provided that the following conditions
 *         are met: - Redistributions of source code must retain the above
 *         copyright notice, this list of conditions and the following
 *         disclaimer. - Redistributions in binary form must reproduce the above
 *         copyright notice, this list of conditions and the following
 *         disclaimer in the documentation and/or other materials provided with
 *         the distribution. - Neither the name of the HU University of Applied
 *         Sciences Utrecht nor the names of its contributors may be used to
 *         endorse or promote products derived from this software without
 *         specific prior written permission.
 * 
 *         THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *         "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *         LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *         A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE HU
 *         UNIVERSITY OF APPLIED SCIENCES UTRECHT BE LIABLE FOR ANY DIRECT,
 *         INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *         (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *         SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *         HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *         STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 *         IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *         POSSIBILITY OF SUCH DAMAGE.
 **/

package rexos.mas.productAgent;

import rexos.mas.data.AgentStatus;
import rexos.mas.data.BehaviourStatus;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.SequentialBehaviour;

public class OverviewBehaviour extends Behaviour implements BehaviourCallback {

	private static final long serialVersionUID = 1L;
	private ProductAgent _productAgent;

	/* Behaviour */
	private PlannerBehaviour _plannerBehaviour;
	private InformerBehaviour _informerBehaviour;
	private SchedulerBehaviour _schedulerBehaviour;
	private ProduceBehaviour _produceBehaviour;
	private SocketBehaviour _socketBehaviour;
	private HeartBeatBehaviour _heartBeatBehaviour;
	private RescheduleBehaviour _rescheduleBehaviour;

	private ParallelBehaviour _parallelBehaviour;
	private SequentialBehaviour _sequentialBehaviour;

	private boolean _producing = false;

	private boolean _overviewCompleted = false;
	private boolean _rescheduling = false;

	/**
	 * Constructs the OverviewBehaviour
	 * @param myAgent
	 */
	public OverviewBehaviour(Agent myAgent) {
		super(myAgent);
		_productAgent = (ProductAgent) myAgent;
		System.out
				.println("Overview behaviour created. Starting all behaviours to the agents.");
		_productAgent.setStatus(AgentStatus.INITIALIZING);
		this.initialize();
		_productAgent.setStatus(AgentStatus.DONE_INITIALIZING);
	}

	/**
	 * Initialize the sub behaviors
	 */
	private void initialize() {

		System.out.println("Creating the SocketBehaviour");
		_socketBehaviour = new SocketBehaviour(myAgent, _productAgent
				.getProperties().getCallback());

		_heartBeatBehaviour = new HeartBeatBehaviour(myAgent, 5000,
				_socketBehaviour);
		_socketBehaviour.setHeartBeatBehaviour(_heartBeatBehaviour);

		System.out.println("Creating the PlannerBehaviour");
		_plannerBehaviour = new PlannerBehaviour(myAgent, this);

		System.out.println("Creating the InformerBehaviour");
		_informerBehaviour = new InformerBehaviour(myAgent, this);

		System.out.println("Creating the ScheduleBehaviour");
		_schedulerBehaviour = new SchedulerBehaviour(myAgent, this);

		System.out.println("Creating the ProductBehaviour");
		_produceBehaviour = new ProduceBehaviour(myAgent, this);

		System.out.println("Creating the RescheduleBehaviour");
		_rescheduleBehaviour = new RescheduleBehaviour(myAgent, this);

		System.out.println("Creating the ParallelBehaviour");
		_parallelBehaviour = new ParallelBehaviour(ParallelBehaviour.WHEN_ALL);

		System.out.println("Creating the SequentialBehaviour");
		_sequentialBehaviour = new SequentialBehaviour();

		System.out
				.println("Addding a SequentialBehaviour to the Product Agent");
		myAgent.addBehaviour(_sequentialBehaviour);

		System.out
				.println("Adding the SocketBehaviour to the ParallelBehaviour");
		_parallelBehaviour.addSubBehaviour(_socketBehaviour);
		_parallelBehaviour.addSubBehaviour(_heartBeatBehaviour);

		System.out.println("Addding a ParallelBehaviour to the ProductAgent");
		myAgent.addBehaviour(_parallelBehaviour);
	}

	/*
	 * (non-Javadoc) This behaviour should have 3 parallel sub-behaviours. One
	 * where the normal flow ( plan, inform, schedule, produce ) is followed,
	 * and one where it listens to incoming messages. MIST NOG 1 MOGELIJKHEID!
	 */
	/**
	 * Construct the action of the overviewBehavior which sets the status of the overview behavior
	 */
	@Override
	public void action() {
		try {
			AgentStatus as = _productAgent.getStatus();
			switch (as) {
			case INITIALIZING:
				// THis should never be the case
				break;
			case DONE_INITIALIZING:
				this.startPlanning();
				break;
			case DONE_PLANNING:
				this.startInforming();
				break;
			case DONE_INFORMING:
				this.startScheduling();
				break;
			case DONE_SCHEDULING:
				System.out.println("Done Scheduling");
				_productAgent.setStatus(AgentStatus.PRODUCING);
				break;
			case DONE_PRODUCING:
				this.cleanBehaviour();
				_overviewCompleted = true;
				break;
			case DONE_RESCHEDULING:
				this.startPlanning();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns true when the behavior is done
	 * @return
	 */
	@Override
	public boolean done() {
		return _overviewCompleted;
	}

	/**
	 * Not used
	 */
	/**
	 * Starts the planning behavior
	 */
	public void startPlanning() {
		_productAgent.setStatus(AgentStatus.PLANNING);
		System.out.println("Add a PlannerBehaviour");
		myAgent.addBehaviour(_plannerBehaviour);
	}

	/**
	 * Starts the informer behavior
	 */
	public void startInforming() {
		_productAgent.setStatus(AgentStatus.INFORMING);
		System.out.println("Add an InformerBehaviour");
		myAgent.addBehaviour(_informerBehaviour);
	}

	/**
	 * Starts the scheduling behavior
	 */
	public void startScheduling() {
		_productAgent.setStatus(AgentStatus.SCHEDULING);
		System.out.println("Add a SchedulerBehaviour");
		myAgent.addBehaviour(_schedulerBehaviour);
	}

	/**
	 * Starts the produce behavior
	 */
	public void startProducing() {
		System.out.println("Add a ProduceBehaviour");
		if (_produceBehaviour.done() == false)
			myAgent.addBehaviour(_produceBehaviour);
	}

	public void startRescheduling() {
		_productAgent.setStatus(AgentStatus.RESCHEDULING);
		_plannerBehaviour.reset();
		_informerBehaviour.reset();
		_schedulerBehaviour.reset();
		System.out.println("Add a RescheduleBehaviour");
		myAgent.addBehaviour(_rescheduleBehaviour);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rexos.mas.productAgent.BehaviourCallback#handleCallback()
	 */
	/**
	 * Handles the callback to the parent behaviour
	 * @param bs
	 * @param callbackArgs
	 */
	@Override
	public void handleCallback(BehaviourStatus bs, Object[] callbackArgs) {
		AgentStatus as = _productAgent.getStatus();
		if (bs == BehaviourStatus.COMPLETED) {
			switch (as) {
			case PLANNING:
				System.out.println("Done planning.");
				_productAgent.setStatus(AgentStatus.DONE_PLANNING);
				// Check if there was an error. Do this for all cases
				break;
			case INFORMING:
				System.out.println("Done Informing.");
				_productAgent.setStatus(AgentStatus.DONE_INFORMING);
				break;
			case SCHEDULING:
				System.out.println("Done scheduling.");
				_productAgent.setStatus(AgentStatus.PRODUCING);
				break;
			case PRODUCING:
				System.out.println("Done producing.");
				_productAgent.setStatus(AgentStatus.DONE_PRODUCING);
				break;
			case RESCHEDULING:
				System.out.println("Done rescheduling.");
				_rescheduling = false;
				_productAgent.setStatus(AgentStatus.DONE_RESCHEDULING);
				break;
			default:
				System.out.println("Unknown status. Status: " + as.toString());
				break;
			}
		} else if (bs == BehaviourStatus.RUNNING) {
			switch (as) {
			case SCHEDULING:
				startProducing();
				break;
			default:
				break;
			}
		} else if (bs == BehaviourStatus.ERROR) {
			if (!_rescheduling) {
				_rescheduling = true;
				startRescheduling();
			}
		}
	}

	/**
	 * Stops the socket
	 */
	public void cleanBehaviour() {
		_socketBehaviour.write(false, "Product Completed.", "1");
		System.out.println("Done overview, stopping SocketBehaviour.");
		myAgent.removeBehaviour(_parallelBehaviour);
		if (_socketBehaviour != null)
			_socketBehaviour.stop();
	}
}
