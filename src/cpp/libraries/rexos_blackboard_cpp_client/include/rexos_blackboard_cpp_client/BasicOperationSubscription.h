/**
 * @file BasicOperationSubscription.h
 * @brief Subscription for one of the basic Mongo CRUD operations.
 * @date Created: 3 jun. 2013
 *
 * @author Jan-Willem Willebrands
 *
 * @section LICENSE
 * License: newBSD
 *
 * Copyright © 2013, HU University of Applied Sciences Utrecht.
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

#ifndef BASICOPERATIONSUBSCRIPTION_H_
#define BASICOPERATIONSUBSCRIPTION_H_

#include "rexos_blackboard_cpp_client/BlackboardSubscription.h"
#include "rexos_blackboard_cpp_client/MongoOperation.h"

namespace Blackboard {

/**
 * Subscription for one of the basic Mongo CRUD operations.
 */
class BasicOperationSubscription : public BlackboardSubscription {
public:
	/**
	 * Constructs a subscriptions for the specified operation and subscriber.
	 *
	 * @param operation The operation to respond to.
	 * @param subscriber The BlackboardSubscriber that should receive a callback when an event occurs.
	 */
	BasicOperationSubscription(MongoOperation operation, BlackboardSubscriber & subscriber);

	/**
	 * @see BlackboardSubscription::getQuery(mongo::Query *)
	 */
	bool getQuery(mongo::Query * query_out) const;

	/**
	 * @see BlackboardSubscription::matchesWithEntry(const OplogEntry&)
	 */
	bool matchesWithEntry(const OplogEntry& entry) const;

private:
	/**
	 * @var MongoOperation operation
	 * The operation to respond to.
	 */
	MongoOperation operation;
};

} /* namespace Blackboard */
#endif /* BASICOPERATIONSUBSCRIPTION_H_ */
