package com.dclab.api;

import com.dclab.model.LoadBalancingStatus;
import com.dclab.service.LoadBalancerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/load-balancing")
@CrossOrigin(origins = "*")
public class LoadBalancingController {

    private final LoadBalancerService loadBalancer;

    @Autowired
    public LoadBalancingController(LoadBalancerService loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @GetMapping("/status")
    public LoadBalancingStatus getStatus() {
        return loadBalancer.getStatus();
    }

    @PostMapping("/rebalance")
    public ResponseEntity<String> rebalance() {
        loadBalancer.rebalance();
        return ResponseEntity.ok("Rebalance triggered successfully");
    }
}
