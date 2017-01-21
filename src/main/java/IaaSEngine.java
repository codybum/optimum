/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Closeables;
import com.google.inject.Module;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.openstack.keystone.v2_0.KeystoneApi;
import org.jclouds.openstack.keystone.v2_0.domain.Tenant;
import org.jclouds.openstack.keystone.v2_0.domain.User;
import org.jclouds.openstack.keystone.v2_0.extensions.TenantAdminApi;
import org.jclouds.openstack.keystone.v2_0.extensions.UserAdminApi;
import org.jclouds.openstack.keystone.v2_0.options.CreateTenantOptions;
import org.jclouds.openstack.keystone.v2_0.options.CreateUserOptions;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.domain.*;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPPoolApi;
import org.jclouds.openstack.nova.v2_0.features.FlavorApi;
import org.jclouds.openstack.nova.v2_0.features.ImageApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

public class IaaSEngine implements Closeable {

    private KeystoneApi keystoneApi;

    private  NovaApi novaApi;
    //private Set<String> regions;
    private String region = "DefaultRegion";
    private String floatingIPPool = "bright-external-flat-openstack_vm_private_10_33_4_0";

    private void setNovaAPI(String serverAddress, String tenantName, String userName, String password) {
        System.out.format("%s%n", this.getClass().getName());

        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        String provider = "openstack-nova";

        novaApi = ContextBuilder.newBuilder(provider)
                .endpoint("http://" + serverAddress + ":5000/v2.0/")
                .credentials(tenantName + ":" + userName, password)
                .modules(modules)
                .buildApi(NovaApi.class);
        //regions = novaApi.getConfiguredRegions();
    }

    private void setKeystoneApi(String serverAddress, String tenantName, String userName, String password) {
        System.out.format("%s%n", this.getClass().getName());

        Iterable<Module> modules = ImmutableSet.<Module>of(new SLF4JLoggingModule());

        String provider = "openstack-keystone";
        String identity = tenantName + ":"  + userName;

        keystoneApi = ContextBuilder.newBuilder(provider)
                .endpoint("http://" + serverAddress + ":35357/v2.0/")
                .credentials(identity, password)
                .modules(modules)
                .buildApi(KeystoneApi.class);

    }

    public IaaSEngine() {

        String serverAddress = "127.0.0.1";

        String tenantName = "CrescoDynamic";
        String userName = "cresco";
        String password = "u$cresco01";

        setNovaAPI(serverAddress,tenantName,userName,password);


         }

    public Boolean deleteInstance(String instanceId){
        ServerApi serverApi = novaApi.getServerApi(region);
        removeFloatingIp(instanceId);
        return serverApi.delete(instanceId);

    }

    public String createInstance(String instanceName){
        ServerApi serverApi = novaApi.getServerApi(region);
        String imageId = "1942b83e-125b-417e-b9b4-1b2a5503aa5c";
        String flavorId = "3";
        ServerCreated ser = serverApi.create(instanceName, imageId, flavorId);

        Server server = serverApi.get(ser.getId());
        while(server.getStatus().value().equals("ACTIVE") == false) {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex) {}
            server = serverApi.get(ser.getId());
        }

        addFloatingIp(ser.getId());

        System.out.println(ser.getId());

        return ser.getId();
    }

    public boolean addFloatingIp(String instanceId) {
        boolean isAdded = false;
        try {

            FloatingIPApi floatingIPApi = novaApi.getFloatingIPApi(region).get();

            FloatingIP floatIp = floatingIPApi.allocateFromPool("bright-external-flat-openstack_vm_private_10_33_4_0");
            //System.out.println("Float Address From Pool : id=" + floatIp.getId() + " address=" + floatIp.getIp());

            floatingIPApi.addToServer(floatIp.getIp(), instanceId);
        }
        catch (Exception ex) {
            System.out.println("addFloatingIP " + ex.getMessage());
        }
        return isAdded;
    }

    public boolean removeFloatingIp(String instanceId) {
        boolean isRemoved = false;
        try {
            FloatingIPApi floatingIPApi = novaApi.getFloatingIPApi(region).get();

            FloatingIP fipRemove = null;
            for (FloatingIP fip : floatingIPApi.list()) {
                if (instanceId.equals(fip.getInstanceId())) {
                    fipRemove = fip;
                }
            }


            if(fipRemove != null) {
                floatingIPApi.removeFromServer(fipRemove.getIp(), instanceId);
                floatingIPApi.delete(fipRemove.getId());
            }

            isRemoved = true;
        }
        catch (Exception ex) {
            System.out.println("removeFloatingIP " + ex.getMessage());
        }
        return isRemoved;
    }

    public void listFloatingIp() {

      FloatingIPApi floatingIPApi = novaApi.getFloatingIPApi(region).get();
        System.out.println("Floating IP in " + region);

        for(FloatingIP fip : floatingIPApi.list()) {
            System.out.println("    Floating IP Address:" + fip.getIp());
            System.out.println("    Floating IP ID:" + fip.getId());
            System.out.println("    Floating IP InstanceID" + fip.getInstanceId());

        }
    }

    public void listFloatingIpPool() {

        FloatingIPPoolApi floatingIPPoolApi = novaApi.getFloatingIPPoolApi(region).get();

        System.out.println("Floating IPPool in " + region);

        for(FloatingIPPool fip : floatingIPPoolApi.list()) {
            System.out.println("    " + fip.getName());
        }
        //bright-external-flat-openstack_vm_private_10_33_4_0
    }

    public String getInstanceFromName(String InstanceName) {
        String instanceName = null;
        try {
            ServerApi serverApi = novaApi.getServerApi(region);
            for (Server server : serverApi.listInDetail().concat()) {
                if(InstanceName.equals(server.getName())) {
                    instanceName = server.getId();
                }
            }
        }
        catch(Exception ex) {
            System.out.println("getInstanceFromName : " + ex.getMessage());
        }
        return instanceName;
    }

    public void listServers() {
        //for (String region : regions) {
            ServerApi serverApi = novaApi.getServerApi(region);


            System.out.println("Servers in " + region);

            for (Server server : serverApi.listInDetail().concat()) {
                System.out.println("  " + server);


            }


        //}
    }

    public void listImages() {
        ImageApi imageApi = novaApi.getImageApi(region);

        for (Image image : imageApi.listInDetail().concat()) {
            System.out.println(" " + image);
        }
    }

    public void listFlavors() {
        FlavorApi flavorApi = novaApi.getFlavorApi(region);

        for (Flavor flavor : flavorApi.listInDetail().concat()) {
            System.out.println(" " + flavor);
        }
    }

    private Tenant createTenant() {
        System.out.format("  Create Tenant%n");

        Optional<? extends TenantAdminApi> tenantAdminApiExtension = keystoneApi.getTenantAdminApi();

        if (tenantAdminApiExtension.isPresent()) {
            System.out.format("    TenantAdminApi is present%n");

            TenantAdminApi tenantAdminApi = tenantAdminApiExtension.get();
            CreateTenantOptions tenantOptions = CreateTenantOptions.Builder
                    .description("My New Tenant");
            Tenant tenant = tenantAdminApi.create("newTenant", tenantOptions);

            System.out.format("    %s%n", tenant);

            return tenant;
        } else {
            System.out.format("    TenantAdminApi is *not* present%n");
            System.exit(1);

            return null;
        }
    }

    private void createUser(Tenant tenant) {
        System.out.format("  Create User%n");

        Optional<? extends UserAdminApi> userAdminApiExtension = keystoneApi.getUserAdminApi();

        if (userAdminApiExtension.isPresent()) {
            System.out.format("    UserAdminApi is present%n");

            UserAdminApi userAdminApi = userAdminApiExtension.get();
            CreateUserOptions userOptions = CreateUserOptions.Builder
                    .tenant(tenant.getId())
                    .email("new.email@example2.com");
            User user = userAdminApi.create("newUser2", "newPassword2", userOptions);

            System.out.format("    %s%n", user);
        } else {
            System.out.format("    UserAdminApi is *not* present%n");
            System.exit(1);
        }
    }

    public void close() throws IOException {
        Closeables.close(keystoneApi, true);
    }
}