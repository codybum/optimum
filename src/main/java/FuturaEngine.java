import app.gNode;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by vcbumg2 on 1/19/17.
 */
public class FuturaEngine {

    AppSchedulerEngine appSchedulerEngine;

    public FuturaEngine(AppSchedulerEngine appSchedulerEngine) {
    this.appSchedulerEngine = appSchedulerEngine;
    }

    public Map<Integer,List<String>> getClusters(int clusterK, int clusterType) {
        Map<Integer,List<String>> clusterMap = null;

        //clusterK
        //The number of clusters to generate

        //clusterTypes
        //0 = all ResourceMetrics
        //1 = Network ResourceMetrics
        //2 = CPU ResourceMetrics
        //3 = Mem ResourceMetrics
        //4 = Disk ResourceMetrics

        try {
            clusterMap = new HashMap<>();
            List<ResourceMetric> metrics = getResourceMetrics();
            List<ResourceMetricWrapper> clusterInput = new ArrayList<>(metrics.size());
            for (ResourceMetric rm : metrics) {
                clusterInput.add(new ResourceMetricWrapper(rm,clusterType));
            }

            // initialize a new clustering algorithm.
            // we use KMeans++ with 10 clusters and 10000 iterations maximum.
            // we did not specify a distance measure; the default (euclidean distance) is used.
            KMeansPlusPlusClusterer<ResourceMetricWrapper> clusterer = new KMeansPlusPlusClusterer<ResourceMetricWrapper>(clusterK, 10000);
            List<CentroidCluster<ResourceMetricWrapper>> clusterResults = clusterer.cluster(clusterInput);

            // output the clusters
            for (int i = 0; i < clusterResults.size(); i++) {

                clusterMap.put(i,new ArrayList<String>());

                for (ResourceMetricWrapper metricWrapper : clusterResults.get(i).getPoints()) {
                    clusterMap.get(i).add(metricWrapper.getResourceMetric().getINodeId());
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return clusterMap;
    }

    public ResourceMetric getResourceMetric(String pluginname) {
        ResourceMetric rm = null;
        try {
            Map<String, ResourceMetric> containerMetrics = getContainerMetricMap();
            /*
            for (Map.Entry<String, ResourceMetric> entry : containerMetrics.entrySet())
            {
                System.out.println(entry.getKey() + "/" + entry.getValue().toString());
            }
            */
            if(containerMetrics.containsKey(pluginname)) {
                rm = containerMetrics.get(pluginname);
            }
            else {
                rm = new ResourceMetric(pluginname,500);

            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return rm;
    }

    public Map<String,ResourceMetric> getContainerMetricMap() {
        Map<String,ResourceMetric> containerMetricMap = null;
        try {
            containerMetricMap = new HashMap<>();
            for(ResourceMetric rm : getResourceMetrics()) {
                if(containerMetricMap.containsKey(rm.getINodeId())) {
                    ResourceMetric rmq = containerMetricMap.get(rm.getINodeId());
                    rmq.addCpuAve(rm.getCpuAve());
                    rmq.addMemory(rm.getMemAve());
                    rmq.addDiskRead(rm.getDiskRead());
                    rmq.addDiskWrite(rm.getDiskWrite());
                    rmq.addNetworkRx(rm.getNetworkRx());
                    rmq.addNetworkTx(rm.getNetworkTx());
                }
                else {
                    containerMetricMap.put(rm.getINodeId(),rm);
                }
            }

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return containerMetricMap;
    }

    public List<ResourceMetric> getResourceMetrics() {
        List<ResourceMetric> resourceMetrics = null;
        try {
            resourceMetrics = new ArrayList<>();
            String[] plugins = {"p0","p1","p2"};
            for(String plugin : plugins) {
                int  genMetricCount = ThreadLocalRandom.current().nextInt(0, 10);
                for(int i = 0; i < genMetricCount; i++) {
                    ResourceMetric rm = genResourceMetric();
                    rm.addWorkloadCost(workloadCalcUtil(rm,appSchedulerEngine.ge.getResourceProvider(plugin)));
                    rm.setINodeId(plugin);
                    resourceMetrics.add(rm);
                }
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return resourceMetrics;
    }

    private double workloadCalcUtil(ResourceMetric rm, ResourceProvider rp) {
        double workloadCost = -1;
        try{
            long cpuCompositeBenchmark = rp.getCpuCompositeBenchmark();
            long cpuLogicalCount = rp.getCpuLogicalCount();
            long compositeCpuCapacity = cpuCompositeBenchmark * cpuLogicalCount;
            double cpuAve = rm.getCpuAve();
            workloadCost = (double)compositeCpuCapacity * (cpuAve/100);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return workloadCost;
    }

    private ResourceMetric genResourceMetric() {
        ResourceMetric rm = null;
        try {
            String INodeId = java.util.UUID.randomUUID().toString();
            Random rand = new Random();
            //int random_integer = rand.nextInt(upperbound-lowerbound) + lowerbound;
            long runTime = ThreadLocalRandom.current().nextLong(0, 1000000);
            double cpuAve = ThreadLocalRandom.current().nextDouble(0,99.9);
            long memLimit = ThreadLocalRandom.current().nextLong(0, 1000000);
            long memMax = ThreadLocalRandom.current().nextLong(0, memLimit);
            long memAve = ThreadLocalRandom.current().nextLong(0, memMax);
            long memCurrent = ThreadLocalRandom.current().nextLong(0, memMax);
            long diskReadTotal = ThreadLocalRandom.current().nextLong(0, 1000000);
            long diskWriteTotal = ThreadLocalRandom.current().nextLong(0, 1000000);
            long networkRxTotal = ThreadLocalRandom.current().nextLong(0, 1000000);
            long networkTxTotal = ThreadLocalRandom.current().nextLong(0, 1000000);

            //public ResourceMetric(String INodeId, long runTime, long cpuTotal,
            // long memCurrent, long memAve, long memLimit, long memMax,
            // long diskReadTotal, long diskWriteTotal, long networkRxTotal,
            // long networkTxTotal) {
            rm = new ResourceMetric(INodeId,runTime,cpuAve,memCurrent,memAve,memLimit,memMax,diskReadTotal,diskWriteTotal,networkRxTotal,networkTxTotal);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return rm;
    }

}
