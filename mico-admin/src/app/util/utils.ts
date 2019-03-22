/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Subscription } from 'rxjs';

/**
 * unsubscribes the subscription if possible
 * @param subscription subscription to be unsubscribed
 */
export function safeUnsubscribe(subscription: Subscription) {
    if (subscription != null) {
        subscription.unsubscribe();
    }
}

/**
 * unsubsribes all subscriptions in the list if possible and clears the list
 * @param subscriptionList list of subscriptions to be unsubscribed
 */
export function safeUnsubscribeList(subscriptionList: Subscription[]) {

    subscriptionList.forEach(element => {
        safeUnsubscribe(element);
    });
    // clear list (see https://stackoverflow.com/questions/1232040/how-do-i-empty-an-array-in-javascript)
    subscriptionList.length = 0;
}
