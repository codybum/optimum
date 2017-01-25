import app.gEdge;
import app.gNode;
import app.gPayload;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jnt.Bench.Applet;
import jnt.Bench.Bench;
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;

import java.util.*;

/**
 * Warehouse Location Problem
 * <p>
 *
 * @author Charles Prud'homme
 * @since 27/05/2016.
 */
public class Optima {

    public static void main(String[] args) {

        //testbenchmark();
        //testDocker();
        //teststats();
        appscheduler();
        //statcalc();
    }

    private static double workloadCalc(String cpuCompositeBenchmarks, String cpuIdles, String cpuLogicalCounts) {
        double workloadUtil = -1;
        try{
            long cpuCompositeBenchmark = Long.parseLong(cpuCompositeBenchmarks);
            double cpuAve = Double.parseDouble(cpuIdles);
            long cpuLogicalCount = Long.parseLong(cpuLogicalCounts);
            long compositeCpuCap = cpuCompositeBenchmark * cpuLogicalCount;
            workloadUtil = (double)compositeCpuCap * (cpuAve/100);
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return workloadUtil;
    }

    private static void statcalc() {

        String cpuCompositeBenchmark = "1000";
        String cpuLogicalCount = "8";
        String cpuIdle = "50.0";

        workloadCalc(cpuCompositeBenchmark,cpuIdle,cpuLogicalCount);

    }

    private static void appscheduler() {
        AppSchedulerEngine ae = new AppSchedulerEngine();
        ae.go();
    }

    private static void testiaas() {
        IaaSEngine iaas = new IaaSEngine();
        iaas.listServers();
        iaas.listImages();
        iaas.listFlavors();
        iaas.listFloatingIp();
        iaas.listFloatingIpPool();

        String instanceId = iaas.createInstance("FIRSTMFVM");

        System.out.println("removing : " + instanceId);
        if (instanceId != null) {
            iaas.deleteInstance(instanceId);
        }
    }

    private static void testbenchmark() {
        Benchmark bmark = new Benchmark();
        BenchMetric bm = bmark.bench();

        System.out.println("Benchmark Runtime: " + bm.getRunTime() + " CPU Performance: " + bm.getCPU());
    }

    private static void teststats() {
        SysInfoBuilder builder = new SysInfoBuilder();

        for(Map.Entry<String, String> entry : builder.getSysInfoMap().entrySet()) {
            //tick.setParam(entry.getKey(), entry.getValue());
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

    }
    private static void testDocker() {
        //Start Docker

        DockerEngine de = new DockerEngine();

        //public String createContainer(String image, List<String> commands, String[] ports) {
        ////.cmd("sh", "-c", "while :; do sleep 100; done")
        List<String> cmd = new ArrayList<>();
        cmd.add("sh");
        cmd.add("-c");
        cmd.add("while :; do sleep 100; done");
        //("sh", "-c", "while :; do sleep 100; done")
        //String[] ports = {"80", "22"};
        String container_id = de.createContainer("gitlab.rc.uky.edu:4567/cresco/cresco-container", null, null);

        de.startContainer(container_id);

        List<ResourceMetric> metrics = new ArrayList<>();

        omnom(de,container_id);

        for (int i = 0; i < 30; i++) {

            try {
                Thread.sleep(1000);
            }
            catch(Exception ex) {
                ex.printStackTrace();
            }
            //metrics.add(de.getResourceMetric(container_id));
            //de.getStats(container_id);
            //String[] command = {"sh", "-c", "ls"};
            //String[] command = {"sh", "-c", "echo woot > woot"};
            //String[] command = {"sh", "-c", "dd if=/dev/urandom of=random.img count=1 bs=10M"};
            //String[] command = {"sh", "-c", "dd if=/dev/urandom count=1 bs=100000M | bzip2 -9 > random.img"};
            //System.out.println(de.containerExeCmd(container_id, command));
            //System.out.println(de.containerExeCmd(container_id, command));
            //String[] commandread = {"sh", "-c", "cat random.img > /dev/null"};
            //System.out.println(de.containerExeCmd(container_id, commandread));
        }

        for(ResourceMetric rm : metrics) {
            System.out.println(rm.toString());
        }

        de.shutdown();
    }

    public static void omnom(final DockerEngine de, final String container_id) {
        new Thread(
                new Runnable() {
                    public void run() {
                        //otherRunMethod();
                        while(true) {
                            System.out.println("----");
                            de.getResourceMetric(container_id);
                            SysInfoBuilder builder = new SysInfoBuilder();
                            Map<String,String> statmap = builder.getSysInfoMap();
                            System.out.println("cpu-idle-load=" + statmap.get("cpu-idle-load"));
                            System.out.println("cpu-physical-count=" + statmap.get("cpu-physical-count"));
                            System.out.println("cpu-logical-count=" + statmap.get("cpu-logical-count"));
                        }
                    }
                }
        ).start();
    }


}