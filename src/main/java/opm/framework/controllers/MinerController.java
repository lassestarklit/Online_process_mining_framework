package opm.framework.controllers;



import miners.discovery_miner.DiscoveryMiner;
import org.springframework.web.bind.annotation.*;


import java.util.*;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/v1")
public class MinerController {

    private List<DiscoveryMiner> miners = new ArrayList<>();

    //get all employees
    @GetMapping("/miners")
    public List<DiscoveryMiner> getAllMiners(){

        return miners;
    }

    @GetMapping("/miner")
    @ResponseBody
    public DiscoveryMiner findMinerWithId(@RequestParam String id){
        return getMinerById(id);

    }
    //Create new miner instance
    @PostMapping("/miners")
    public DiscoveryMiner createMiner(@RequestBody DiscoveryMiner miner){
        miners.add(miner);
        return miner;
    }

    //Create new miner instance
    @PostMapping("/miner/subscribeStream")
    public String subscribeStream(@RequestParam String id){
        DiscoveryMiner miner = getMinerById(id);
        miner.subscribeStream();
        return "Stream begun";
    }

    //Disconnect client from miner
    @PostMapping("/miner/stopStream")
    public String stopStream(@RequestParam String id){
        DiscoveryMiner miner = getMinerById(id);
        miner.disconnectStream();
        return "Stream cancelled";
    }

    //Change stream base
    @PostMapping("/miner/changeStream")
    public String changeStream(@RequestParam String id, @RequestParam String base){
        DiscoveryMiner miner = getMinerById(id);
        miner.setStreamName(base);
        return "Stream changed";
    }

    @PostMapping("/miner/getGraph")
    public String getGraph(@RequestParam String id,@RequestParam String threshold){
        DiscoveryMiner miner = getMinerById(id);
        return miner.respondToQuery(threshold);
    }

    private DiscoveryMiner getMinerById(String id){
        for (DiscoveryMiner miner : miners){
            if (miner.getId().equals(id)){
                return miner;
            }
        }

        return null;
    }





}
