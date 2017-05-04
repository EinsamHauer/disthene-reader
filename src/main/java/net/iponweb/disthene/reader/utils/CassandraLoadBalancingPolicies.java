package net.iponweb.disthene.reader.utils;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.LatencyAwarePolicy;
import com.datastax.driver.core.policies.LoadBalancingPolicy;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;

public class CassandraLoadBalancingPolicies {

    public static final String roundRobinPolicy = "RoundRobinPolicy";
    public static final String tokenAwarePolicy = "TokenAwarePolicy";
    public static final String tokenDcAwareRoundRobinPolicy = "TokenDcAwareRoundRobinPolicy";
    public static final String dcAwareRoundRobinPolicy = "DcAwareRoundRobinPolicy";
    public static final String latencyAwarePolicy = "LatencyAwarePolicy";
    
    public static LoadBalancingPolicy getLoadBalancingPolicy(String policy) {
        LoadBalancingPolicy loadBalancingPolicy;
        switch (policy) {
            case roundRobinPolicy:
                loadBalancingPolicy = new RoundRobinPolicy();
                break;
            case tokenAwarePolicy:
                loadBalancingPolicy = new TokenAwarePolicy(new RoundRobinPolicy());
                break;
            case tokenDcAwareRoundRobinPolicy:
                loadBalancingPolicy = new TokenAwarePolicy(new DCAwareRoundRobinPolicy());
                break;
            case dcAwareRoundRobinPolicy:
                loadBalancingPolicy = new DCAwareRoundRobinPolicy();
                break;
            case latencyAwarePolicy:
                loadBalancingPolicy = LatencyAwarePolicy.builder(new RoundRobinPolicy()).build();
                break;
            default:
                throw new IllegalArgumentException("Cassandra load balancing policy can be " + roundRobinPolicy + " ," + tokenAwarePolicy 
                       + " ," + dcAwareRoundRobinPolicy);
        }
        return loadBalancingPolicy;
    }
}
