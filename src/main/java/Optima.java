import jnt.Bench.Applet;
import jnt.Bench.Bench;
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;

import java.util.ArrayList;
import java.util.List;

/**
 * Warehouse Location Problem
 * <p>
 *
 * @author Charles Prud'homme
 * @since 27/05/2016.
 */
public class Optima {

    public static void main(String[] args) {

        IaaSEngine iaas = new IaaSEngine();
        iaas.listServers();
        iaas.listImages();
        iaas.listFlavors();
        iaas.listFloatingIp();
        iaas.listFloatingIpPool();

        String instanceId = iaas.createInstance("FIRSTMFVM");
        //String instanceId = iaas.getInstanceFromName("FIRSTMFVM");

        System.out.println("removing : " + instanceId);
        if(instanceId != null) {
            iaas.deleteInstance(instanceId);
        }



        System.exit(0);

        Benchmark bmark = new Benchmark();
        BenchMetric bm = bmark.bench();

        System.out.println("Benchmark Runtime: " + bm.getRunTime() + " CPU Performance: " + bm.getCPU());

        //System.exit(0);
        //WarehouseLocation wh = new WarehouseLocation();
        //wh.modelAndSolve();

        //Cluster cl = new Cluster();

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
        String container_id = de.createContainer("busybox:latest", cmd);

        de.startContainer(container_id);

        List<ResourceMetric> metrics = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            metrics.add(de.getResourceMetric(container_id));
            //de.getStats(container_id);
            //String[] command = {"sh", "-c", "ls"};
            //String[] command = {"sh", "-c", "echo woot > woot"};
            String[] command = {"sh", "-c", "dd if=/dev/urandom of=random.img count=1 bs=10M"};
            //String[] command = {"sh", "-c", "dd if=/dev/urandom | bzip2 -9 > woot"};
            System.out.println(de.containerExeCmd(container_id, command));

        }
        de.shutdown();




        for(ResourceMetric metric : metrics) {
            double[] points = new double[] { metric.getCPU(), metric.getMemory(), metric.getDiskRead(), metric.getWriteRead(), metric.getNetworkRx(), metric.getNetworkTx()};
            for(double m : points) {
                System.out.println(m);
            }
        }

        //End Docker

        Cluster cl = new Cluster(metrics);
        cl.run();

        for(ResourceMetric rm : metrics) {
            System.out.println("cpuAVE=" + rm.getCPU());
            System.out.println("cpuComposite=" + bm.getCPU());
            double cpuValue = (rm.getCPU() * bm.getCPU())/1000000;
            System.out.println("cpuValue=" + cpuValue);
        }

    }

}