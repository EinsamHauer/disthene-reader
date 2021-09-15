package net.iponweb.disthene.reader.utils;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LatencyAwarePolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

public class CassandraLoadBalancingPolicies {

    public static final String tokenAwarePolicy = "TokenAwarePolicy";
    public static final String tokenDcAwareRoundRobinPolicy = "TokenDcAwareRoundRobinPolicy";
    public static final String tokenLatencyAwarePolicy = "TokenLatencyAwarePolicy";

    public static LoadBalancingPolicy getLoadBalancingPolicy(String policy) {
        LoadBalancingPolicy loadBalancingPolicy = switch (policy) {
            case tokenAwarePolicy -> new TokenAwarePolicy(new RoundRobinPolicy());
            case tokenDcAwareRoundRobinPolicy -> new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build());
            case tokenLatencyAwarePolicy -> new TokenAwarePolicy(LatencyAwarePolicy.builder(new RoundRobinPolicy()).build());
            default -> throw new IllegalArgumentException("Cassandra load balancing policy can be " + tokenAwarePolicy + " ," + tokenLatencyAwarePolicy
                    + " ," + tokenDcAwareRoundRobinPolicy);
        };
        return loadBalancingPolicy;
    }
}
