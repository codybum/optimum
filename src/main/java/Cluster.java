import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vcbumg2 on 1/19/17.
 */
public class Cluster {
    private List<ResourceMetric> metrics;

    public void run() {

        List<ResourceMetricWrapper> clusterInput = new ArrayList<>(metrics.size());
        for(ResourceMetric rm : metrics) {
            clusterInput.add(new ResourceMetricWrapper(rm,1));
        }

        // initialize a new clustering algorithm.
        // we use KMeans++ with 10 clusters and 10000 iterations maximum.
        // we did not specify a distance measure; the default (euclidean distance) is used.
        KMeansPlusPlusClusterer<ResourceMetricWrapper> clusterer = new KMeansPlusPlusClusterer<ResourceMetricWrapper>(4, 10000);
        List<CentroidCluster<ResourceMetricWrapper>> clusterResults = clusterer.cluster(clusterInput);

        // output the clusters
        for (int i=0; i<clusterResults.size(); i++) {
            System.out.println("Cluster " + i);
            for (ResourceMetricWrapper metricWrapper : clusterResults.get(i).getPoints())
                System.out.println(metricWrapper.getResourceMetric().getINodeId());
            System.out.println();
        }

    }
    public Cluster(List<ResourceMetric> metrics) {
        this.metrics = metrics;
    }

}
