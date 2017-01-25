import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by vcbumg2 on 1/19/17.
 */
public class GuilderEngine {

    AppSchedulerEngine appSchedulerEngine;

    public GuilderEngine(AppSchedulerEngine appSchedulerEngine) {
    this.appSchedulerEngine = appSchedulerEngine;

    }

    public double getResourceCost(String INodeId) {
        double resourceCost = -1;
        try{
            resourceCost = 1;
        }
        catch(Exception ex) {

        }
        return resourceCost;
    }

    public ResourceProvider getResourceProvider(String INodeId) {
        ResourceProvider rm = null;
        try{
            rm = genResourceProvider();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return rm;
    }

    public List<ResourceProvider> getResourceProviders() {
        List<ResourceProvider> resourceProviders = null;
        try {
            resourceProviders = new ArrayList<>();
            for(int i = 0; i<3; i++) {
                resourceProviders.add(genResourceProvider());
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return resourceProviders;
    }

    public void addResourceProvider(double workloadUtil) {
        System.out.println("Adding Resources");
    }

    private ResourceProvider genResourceProvider() {
        Map<String,String> sm = null;
        ResourceProvider rp = null;
        try {

            Random rand = new Random();
            //int random_integer = rand.nextInt(upperbound-lowerbound) + lowerbound;
            double cpuIdle = ThreadLocalRandom.current().nextDouble(0,99.9);
            int cpuCount = ThreadLocalRandom.current().nextInt(1,80);

            rp = new ResourceProvider(UUID.randomUUID().toString(),1000,cpuIdle,cpuCount,1000, 1000, 2000);
            /*
            sm = new HashMap<>();
            sm.put("benchmark_cpu_composite","1000");
            sm.put("cpu-logical-count","2");
            sm.put("cpu-idle-load","71.7");
            */
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return rp;
    }

}
