import org.apache.commons.math3.ml.clustering.Clusterable;


public class ResourceMetricWrapper implements Clusterable {

    private double[] points;
    private ResourceMetric metric;

    public ResourceMetricWrapper(ResourceMetric metric) {
        this.metric = metric;
        this.points = new double[] { metric.getCPU(), metric.getMemory(), metric.getDiskRead(), metric.getWriteRead(), metric.getNetworkRx(), metric.getNetworkTx()};

    }

    public ResourceMetric getResourceMetric() {
        return metric;
    }

    public double[] getPoint() {
        return points;
    }

}