package pl.finder.elmer.core;

import pl.finder.elmer.publication.MessagePublisher;
import pl.finder.elmer.subscription.SubscriptionManager;
import pl.finder.elmer.topology.TopologyManager;

public interface MessageBus extends MessagePublisher, SubscriptionManager {

    MessagePublisher createPublisher();

    TopologyManager topology();

    @Override
    void close();
}
