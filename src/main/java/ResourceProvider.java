
public class ResourceProvider {

    private String INodeId;
    private long sysUptime;
    private double cpuIdle;
    private int cpuCompositeBenchmark;
    private long memCurrent;
    private long memLimit;
    private int cpuLogicalCount;

    public ResourceProvider(String INodeId, long sysUptime, double cpuIdle, int cpuLogicalCount, int cpuCompositeBenchmark, long memCurrent, long memLimit) {
        this.INodeId = INodeId;
        this.sysUptime = sysUptime;
        this.cpuIdle = cpuIdle;
        this.cpuLogicalCount = cpuLogicalCount;
        this.cpuCompositeBenchmark = cpuCompositeBenchmark;
        this.memCurrent = memCurrent;
        this.memLimit = memLimit;

    }

    public long getSysUptime() {
        return sysUptime;
    }

    public double getCpuIdle() { return  cpuIdle; }

    public int getCpuLogicalCount() { return cpuLogicalCount; }

    public int getCpuCompositeBenchmark() { return cpuCompositeBenchmark; }

    public long getMemLimit() {
        return memLimit;
    }

    public long getMemCurrent() {
        return memCurrent;
    }

    public String getINodeId() {
        return INodeId;
    }

    @Override
    public String toString() {
        return String.format("id=" + getINodeId() + "sysuptime=" + getSysUptime() + " cpuidle=" + getCpuIdle() + " cpucount=" + getCpuLogicalCount() + " cpubench=" + getCpuCompositeBenchmark() + " memcurrent=" + getMemCurrent() + " memlimit=" + getMemLimit());
    }

}