import { Injectable } from '@angular/core';
import { Subscription } from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UtilsService {

    constructor(
    ) { }

    /**
     * unsubscribes the subscription if possible
     * @param subscription subscription to be unsubscribed
     */
    public safeUnsubscribe(subscription: Subscription) {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }
}
