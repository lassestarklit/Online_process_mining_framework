package miners.discovery_miner;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import miners.discovery_miner.view.graph.ColorPalette;
import miners.discovery_miner.view.graph.PMDotModel;
import mqttxes.lib.XesMqttConsumer;
import mqttxes.lib.XesMqttEvent;
import mqttxes.lib.XesMqttEventCallback;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.persistence.Id;

@Component
public class DiscoveryMiner implements Serializable {

    private static final long serialVersionUID = -8496855732046436045L;

    private String id;
    private Map<String, String> latestActivityInCase = new HashMap<String, String>();

    private Map<Pair<String, String>, Double> relations = new HashMap<Pair<String,String>, Double>();


    private Map<String, Double> activities = new HashMap<String, Double>();
    private Double maxActivityFreq = Double.MIN_VALUE;
    private Double maxRelationsFreq = Double.MIN_VALUE;

    private boolean currentlyRunning;


    public String streamName;



    private transient XesMqttConsumer client;

    public DiscoveryMiner() {
    }

    public DiscoveryMiner(String id) {
        super();
        this.id = id;
        this.currentlyRunning = false;
    }

    public String getId(){
        return id;
    }

    public boolean isCurrentlyRunning() {
        return currentlyRunning;
    }

    public void setCurrentlyRunning(boolean currentlyRunning) {
        this.currentlyRunning = currentlyRunning;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void process(String caseId, String activityName) {
        System.out.println(caseId + ": " + activityName);
        Double activityFreq = 1d;
        if (activities.containsKey(activityName)) {
            activityFreq += activities.get(activityName);
            maxActivityFreq = Math.max(maxActivityFreq, activityFreq);
        }
        activities.put(activityName, activityFreq);

        if (latestActivityInCase.containsKey(caseId)) {
            Pair<String, String> relation = new ImmutablePair<String, String>(latestActivityInCase.get(caseId), activityName);
            Double relationFreq = 1d;
            if (relations.containsKey(relation)) {
                relationFreq += relations.get(relation);
                maxRelationsFreq = Math.max(maxRelationsFreq, relationFreq);
            }
            relations.put(relation, relationFreq);
        }
        latestActivityInCase.put(caseId, activityName);
    }

    public ProcessMap mine(double threshold) {
        ProcessMap process = new ProcessMap();
        for (String activity : activities.keySet()) {
            process.addActivity(activity, activities.get(activity) / maxActivityFreq);
        }
        for (Pair<String, String> relation : relations.keySet()) {
            double dependency = relations.get(relation) / maxRelationsFreq;
            if (dependency >= threshold) {
                process.addRelation(relation.getLeft(), relation.getRight(), dependency);
            }
        }
        Set<String> toRemove = new HashSet<String>();
        Set<String> selfLoopsToRemove = new HashSet<String>();
        for (String activity : activities.keySet()) {
            if (process.isStartActivity(activity) && process.isEndActivity(activity)) {
                toRemove.add(activity);
            }
            if (process.isIsolatedNode(activity)) {
                selfLoopsToRemove.add(activity);
            }
        }
        for (String activity : toRemove) {
            process.removeActivity(activity);
        }
//		for (String activity : selfLoopsToRemove) {
//			process.removeActivity(activity);
//			process.removeRelation(activity, activity);
//		}
        return process;
    }
    public String respondToQuery(String threshold1){

            Double threshold = Double.parseDouble(threshold1) / 100d;
            ProcessMap processMap = mine(threshold);
            return new PMDotModel(processMap, ColorPalette.Colors.BLUE).toString();

    }

    public void subscribeStream(){
        this.currentlyRunning = true;
        this.client= new XesMqttConsumer("broker.hivemq.com", streamName);
        client.subscribe(new XesMqttEventCallback() {
            @Override
            public void accept(XesMqttEvent event) {

                process(event.getCaseId(),event.getActivityName());

            }
        });
        client.connect();
    }

    public void disconnectStream(){

        this.currentlyRunning = false;
        client.disconnect();
    }
}