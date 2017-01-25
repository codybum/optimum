import app.gEdge;
import app.gNode;
import app.gPayload;
import jnr.ffi.annotations.In;
import org.apache.commons.math3.stat.StatUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vcbumg2 on 1/19/17.
 */
public class AppSchedulerEngine {

    public OptimaEngine oe;
    public FuturaEngine fe;
    public GuilderEngine ge;


    public AppSchedulerEngine() {
        oe = new OptimaEngine(this);
        fe = new FuturaEngine(this);
        ge = new GuilderEngine(this);
    }

    public void goClusters() {
        Map<Integer,List<String>> cluters = fe.getClusters(4,1);
        for (Map.Entry<Integer, List<String>> entry : cluters.entrySet())
        {
            for(String nid : entry.getValue()) {
                System.out.println(entry.getKey() + "/" + nid);
            }
            System.out.println("--");

        }
    }
    public void go() {

        gPayload gpay = getPipeline();

        Map<String,List<gNode>> schedulemaps = buildNodeMaps(gpay);

        List<gNode> assignedNodes = schedulemaps.get("assigned");
        List<gNode> unAssignedNodes = schedulemaps.get("unassigned");
        List<gNode> badNodes = schedulemaps.get("error");

        System.out.println("Assigned Nodes : " + assignedNodes.size());
        System.out.println("unAssigned Nodes : " + unAssignedNodes.size());
        System.out.println("badNodes : " + badNodes.size());

        if (badNodes.size() != 0) {
            System.out.println("Bad Node assignments... dead dead deadsky!");
        } else if (unAssignedNodes.size() != 0) {
            System.out.println("We need to find some resources for these request.");

            Map<String,List<gNode>> scheduleAssignments = oe.scheduleAssignment(unAssignedNodes);

            if(scheduleAssignments.get("unassigned").size() != 0) {
                //nodify guilder to get resources
                double workloadResources = 0;
                for(gNode gnode : scheduleAssignments.get("noresources")) {
                    workloadResources += gnode.workloadUtil;
                }
                ge.addResourceProvider(workloadResources);
            }
            else if(scheduleAssignments.get("assigned").size() == unAssignedNodes.size()) {
                //rebuild payload
                gpay.nodes.clear();
                gpay.nodes.addAll(scheduleAssignments.get("assigned"));
                Map<String,List<gNode>> schedulemapsOpt = buildNodeMaps(gpay);

                List<gNode> assignedNodesOpt = schedulemapsOpt.get("assigned");
                List<gNode> unAssignedNodesOpt = schedulemapsOpt.get("unassigned");
                List<gNode> badNodesOpt = schedulemapsOpt.get("error");

                System.out.println("Assigned Nodes : " + assignedNodesOpt.size());
                System.out.println("unAssigned Nodes : " + unAssignedNodesOpt.size());
                System.out.println("badNodes : " + badNodesOpt.size());

                if((unAssignedNodesOpt.size() == 0) && (badNodesOpt.size() == 0)) {
                    System.out.println("WOOT");
                }

                ////gpay.nodes.addAll(assignedNodes);

            }


        } else if ((unAssignedNodes.size() == 0) && (badNodes.size() == 0)) {
            System.out.println("Woot! Schedule This thing!");
        } else {
            System.out.println("WTF are you doing here?");
        }
    }

    private Map<String,List<gNode>> buildNodeMaps(gPayload gpay) {

        Map<String,List<gNode>> nodeResults = null;
        try {

            nodeResults = new HashMap<>();

            List<gNode> assignedNodes = new ArrayList<>();
            List<gNode> unAssignedNodes = new ArrayList<>(gpay.nodes);
            List<gNode> errorNodes = new ArrayList<>();

            //verify predicates
            for (gNode node : gpay.nodes) {

                if (node.params.containsKey("location_region") && node.params.containsKey("location_agent")) {
                    if (nodeExist(node.params.get("location_region"), node.params.get("location_agent"))) {
                        unAssignedNodes.remove(node);
                        assignedNodes.add(node);
                    } else {
                        errorNodes.add(node);
                    }
                } else if (node.params.containsKey("location")) {
                    if (locationExist(node.params.get("location"))) {
                        unAssignedNodes.remove(node);
                        assignedNodes.add(node);
                    } else {
                        errorNodes.add(node);
                    }
                }
            }
            nodeResults.put("assigned",assignedNodes);
            nodeResults.put("unassigned", unAssignedNodes);
            nodeResults.put("error",errorNodes);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return nodeResults;
    }

    public boolean nodeExist(String region, String agent) {
        return true;
    }

    public boolean locationExist(String location) {
        return true;
    }

    public gPayload getPipeline() {

        Map<String,String> n0Params = new HashMap<>();
        n0Params.put("pluginname","p0");
        n0Params.put("jarfile","cresco-sysinfo-plugin-0.5.0.jar");
        n0Params.put("location","0");

        Map<String,String> n1Params = new HashMap<>();
        n1Params.put("pluginname","p1");
        n1Params.put("jarfile","cresco-sysinfo-plugin-0.5.0.jar");

        Map<String,String> n2Params = new HashMap<>();
        n2Params.put("pluginname","p2");
        n2Params.put("jarfile","cresco-sysinfo-plugin-0.5.0.jar");

        Map<String,String> n3Params = new HashMap<>();
        n3Params.put("pluginname","p3");
        n3Params.put("jarfile","cresco-sysinfo-plugin-0.5.0.jar");


        gNode n0 = new gNode("dummy", "node0", "0", n0Params);
        gNode n1 = new gNode("dummy", "node1", "1", n1Params);
        gNode n2 = new gNode("dummy", "node2", "2", n2Params);
        gNode n3 = new gNode("dummy", "node3", "3", n3Params);

        List<gNode> gNodes = new ArrayList<>();
        gNodes.add(n0);
        gNodes.add(n1);
        gNodes.add(n2);


        gEdge e0 = new gEdge("0","0","1");

        List<gEdge> gEdges = new ArrayList<>();
        gEdges.add(e0);

        gPayload gpay = new gPayload(gNodes,gEdges);
        gpay.pipeline_id = "0";
        gpay.pipeline_name = "demo_pipeline";
        return gpay;
    }


}
