
public class ResourceMetric2 {

    private double cpu;
    private double memory;
    private double disk;
    private double network;
    private String metricName;

    public ResourceMetric2(String metricName, double cpu, double memory, double disk, double network) {
        this.metricName = metricName;
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
        this.network = network;
    }

    public double getCPU() {
        return cpu;
    }

    public double getMemory() {
        return memory;
    }

    public double getDisk() {
        return disk;
    }

    public double getNetwork() {
        return network;
    }

    public String getMetricName() {
        return metricName;
    }
}