package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

class CopyHostNetworksHelperTest {

    // Non-vlan
    private static final Guid NET1 = new Guid("00000000-0000-0000-0020-000000000000");
    // Vlan 10
    private static final Guid NET2 = new Guid("00000000-0000-0000-0020-000000000001");
    // Vlan 20
    private static final Guid NET3 = new Guid("00000000-0000-0000-0020-000000000002");
    // Vlan 30
    private static final Guid NET4 = new Guid("00000000-0000-0000-0020-000000000003");

    @Test
    void testScenarioTwoToOne() {
        var sourceConfiguration = createScenarioTwo();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        assertEquals(1, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "eth1");
    }

    @Test
    void testScenarioThreeToOne() {
        var sourceConfiguration = createScenarioThree();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(2, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET2, "eth1");
        assertAttachment(attachmentsToApply.get(1), NET1, "eth2");
    }

    @Test
    void testScenarioFourToOne() {
        var sourceConfiguration = createScenarioFour();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET3, "bond0");
        assertAttachment(attachmentsToApply.get(1), NET4, "bond0");
        assertAttachment(attachmentsToApply.get(2), NET1, "eth1");
        assertAttachment(attachmentsToApply.get(3), NET2, "eth1");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth2", "eth3"), "bond0");
    }

    @Test
    void testScenarioFiveToOne() {
        var sourceConfiguration = createScenarioFive();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "bond0");
        assertAttachment(attachmentsToApply.get(1), NET2, "bond0");
        assertAttachment(attachmentsToApply.get(2), NET3, "bond0");
        assertAttachment(attachmentsToApply.get(3), NET4, "eth2");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth1", "eth3"), "bond0");
    }

    @Test
    void testScenarioSevenToSix() {
        var sourceConfiguration = createScenarioSeven();
        var destinationConfiguration = createScenarioSix();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(1, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET1, "bond1");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth2", "eth3"), "bond1");
    }

    @Test
    void testScenarioThreeToTwo() {
        var sourceConfiguration = createScenarioThree();
        var destinationConfiguration = createScenarioTwo();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());
        assertTrue(helper.getBondsToApply().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(2, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET2, "eth1");
        assertAttachmentReused(attachmentsToApply.get(1), NET1, "eth2");

    }

    @Test
    void testScenarioFourToThree() {
        var sourceConfiguration = createScenarioFour();
        var destinationConfiguration = createScenarioThree();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachment(attachmentsToApply.get(0), NET3, "bond0");
        assertAttachment(attachmentsToApply.get(1), NET4, "bond0");
        assertAttachmentReused(attachmentsToApply.get(2), NET1, "eth1");
        assertAttachmentReused(attachmentsToApply.get(3), NET2, "eth1");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth2", "eth3"), "bond0");
    }

    @Test
    void testScenarioFiveToFour() {
        var sourceConfiguration = createScenarioFive();
        var destinationConfiguration = createScenarioFour();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertTrue(helper.getAttachmentsToRemove().isEmpty());
        assertTrue(helper.getBondsToRemove().isEmpty());

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertAttachmentReused(attachmentsToApply.get(0), NET1, "bond0");
        assertAttachmentReused(attachmentsToApply.get(1), NET2, "bond0");
        assertAttachmentReused(attachmentsToApply.get(2), NET3, "bond0");
        assertAttachmentReused(attachmentsToApply.get(3), NET4, "eth2");

        List<CreateOrUpdateBond> bondsToApply = helper.getBondsToApply();
        assertEquals(1, bondsToApply.size());
        assertBond(bondsToApply.get(0), Arrays.asList("eth1", "eth3"), "bond0");
    }

    @Test
    void testScenarioOneToFive() {
        var sourceConfiguration = createScenarioOne();
        var destinationConfiguration = createScenarioFive();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        assertEquals(4, helper.getAttachmentsToRemove().size());
        assertEquals(1, helper.getBondsToRemove().size());
        assertTrue(helper.getBondsToApply().isEmpty());
        assertTrue(helper.getAttachmentsToApply().isEmpty());
    }

    @Test
    void testIPv4Configuration() {
        var sourceConfiguration = createIpv4Scenario();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(3, attachmentsToApply.size());
        assertIPv4Configuration(attachmentsToApply.get(0), Ipv4BootProtocol.NONE);
        assertIPv4Configuration(attachmentsToApply.get(1), Ipv4BootProtocol.DHCP);
        assertIPv4Configuration(attachmentsToApply.get(2), Ipv4BootProtocol.NONE);
    }

    @Test
    void testIPv6Configuration() {
        var sourceConfiguration = createIpv6Scenario();
        var destinationConfiguration = createScenarioOne();
        var helper = new CopyHostNetworksHelper(sourceConfiguration.getFirst(),
                sourceConfiguration.getSecond(),
                destinationConfiguration.getFirst(),
                destinationConfiguration.getSecond());
        helper.buildDestinationConfig();

        List<NetworkAttachment> attachmentsToApply = helper.getAttachmentsToApply();
        attachmentsToApply.sort(Comparator.comparing(NetworkAttachment::getNicName));
        assertEquals(4, attachmentsToApply.size());
        assertIPv6Configuration(attachmentsToApply.get(0), Ipv6BootProtocol.NONE);
        assertIPv6Configuration(attachmentsToApply.get(1), Ipv6BootProtocol.DHCP);
        assertIPv6Configuration(attachmentsToApply.get(2), Ipv6BootProtocol.AUTOCONF);
        assertIPv6Configuration(attachmentsToApply.get(3), Ipv6BootProtocol.NONE);
    }

    private void assertAttachment(NetworkAttachment attachment, Guid netId, String nicName) {
        assertEquals(netId, attachment.getNetworkId());
        assertEquals(nicName, attachment.getNicName());
    }

    private void assertAttachmentReused(NetworkAttachment attachment, Guid netId, String nicName) {
        assertAttachment(attachment, netId, nicName);
        assertNotNull(attachment.getId());
    }

    private void assertIPv4Configuration(NetworkAttachment attachment, Ipv4BootProtocol bootProtocol) {
        IpConfiguration ipConfig = attachment.getIpConfiguration();
        assertTrue(ipConfig.hasIpv4PrimaryAddressSet());
        assertEquals(bootProtocol, ipConfig.getIpv4PrimaryAddress().getBootProtocol());
    }

    private void assertIPv6Configuration(NetworkAttachment attachment, Ipv6BootProtocol bootProtocol) {
        IpConfiguration ipConfig = attachment.getIpConfiguration();
        assertTrue(ipConfig.hasIpv6PrimaryAddressSet());
        assertEquals(bootProtocol, ipConfig.getIpv6PrimaryAddress().getBootProtocol());
    }

    private void assertBond(CreateOrUpdateBond bond, List<String> slaves, String bondName) {
        assertEquals(new HashSet<>(slaves), bond.getSlaves());
        assertEquals(bondName, bond.getName());
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioOne() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioTwo() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachNetwork("eth1", NET1)
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioThree() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachVlanNetwork("eth1", NET2, 10)
                .attachNetwork("eth2", NET1)
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioFour() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachNetwork("eth1", NET1)
                .attachVlanNetwork("eth1", NET2, 10)
                .createBondIface("bond0", Arrays.asList("eth2", "eth3"))
                .attachVlanNetwork("bond0", NET3, 20)
                .attachVlanNetwork("bond0", NET4, 30)
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioFive() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachVlanNetwork("eth2", NET4, 30)
                .createBondIface("bond0", Arrays.asList("eth1", "eth3"))
                .attachNetwork("bond0", NET1)
                .attachVlanNetwork("bond0", NET2, 10)
                .attachVlanNetwork("bond0", NET3, 20)
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioSix() {
        return new ScenarioBuilder(4)
                .createBondIface("bond0", Arrays.asList("eth0", "eth1"))
                .attachMgmtNetwork("bond0")
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createScenarioSeven() {
        return new ScenarioBuilder(3)
                .attachMgmtNetwork("eth0")
                .createBondIface("bond0", Arrays.asList("eth1", "eth2"))
                .attachNetwork("bond0", NET1)
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createIpv4Scenario() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachNetwork("eth1", NET1, ScenarioBuilder.createIpConfiguration(Ipv4BootProtocol.NONE, null))
                .attachNetwork("eth2", NET2, ScenarioBuilder.createIpConfiguration(Ipv4BootProtocol.DHCP, null))
                .attachNetwork("eth3", NET3, ScenarioBuilder.createIpConfiguration(Ipv4BootProtocol.STATIC_IP, null))
                .build();
    }

    private Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> createIpv6Scenario() {
        return new ScenarioBuilder(4)
                .attachMgmtNetwork("eth0")
                .attachNetwork("eth1", NET1, ScenarioBuilder.createIpConfiguration(null, Ipv6BootProtocol.NONE))
                .attachNetwork("eth2", NET2, ScenarioBuilder.createIpConfiguration(null, Ipv6BootProtocol.DHCP))
                .attachNetwork("eth2", NET3, ScenarioBuilder.createIpConfiguration(null, Ipv6BootProtocol.AUTOCONF))
                .attachNetwork("eth3", NET4, ScenarioBuilder.createIpConfiguration(null, Ipv6BootProtocol.STATIC_IP))
                .build();
    }

    private static class ScenarioBuilder {

        private static final Guid MGMT_ID = new Guid("00000000-0000-0000-0020-010203040506");
        private static final Integer MGMT_TYPE = 2;

        Map<String, VdsNetworkInterface> interfaces;
        List<NetworkAttachment> attachments;

        ScenarioBuilder(int interfaceCount) {
            interfaces = createNics(interfaceCount);
            attachments = new ArrayList<>();
        }

        public Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> build() {
            Pair<List<VdsNetworkInterface>, List<NetworkAttachment>> pair = new Pair<>();
            pair.setFirst(new ArrayList<>(interfaces.values()));
            pair.setSecond(attachments);
            return pair;
        }

        ScenarioBuilder attachMgmtNetwork(String ifaceName) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            iface.setType(MGMT_TYPE);
            this.attachNetwork(ifaceName, MGMT_ID);
            return this;
        }

        ScenarioBuilder attachNetwork(String ifaceName, Guid networkId) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            NetworkAttachment attachment = createAttachment(iface.getId(), networkId);
            attachment.setIpConfiguration(createIpConfiguration(null, null));
            attachments.add(attachment);
            return this;
        }

        ScenarioBuilder attachNetwork(String ifaceName, Guid networkId, IpConfiguration ipConfig) {
            VdsNetworkInterface iface = interfaces.get(ifaceName);
            NetworkAttachment attachment = createAttachment(iface.getId(), networkId);
            attachment.setIpConfiguration(ipConfig);
            attachments.add(attachment);
            return this;
        }

        ScenarioBuilder attachVlanNetwork(String ifaceName, Guid networkId, Integer vlanId) {
            VdsNetworkInterface vlanIface = createVlan(ifaceName, vlanId);
            interfaces.put(vlanIface.getName(), vlanIface);
            this.attachNetwork(ifaceName, networkId);
            return this;
        }

        ScenarioBuilder createBondIface(String bondName, List<String> slaveNames) {
            Bond bond = createBond(bondName, slaveNames);
            bond.setBonded(true);
            slaveNames.stream()
                    .map(interfaces::get)
                    .forEach(iface -> iface.setBondName(bondName));
            interfaces.put(bondName, bond);
            return this;
        }

        static IpConfiguration createIpConfiguration(Ipv4BootProtocol ipv4BootProto, Ipv6BootProtocol ipv6BootProto) {
            var ipconfig = new IpConfiguration();
            if (ipv4BootProto != null) {
                ipconfig.setIPv4Addresses(Collections.singletonList(createIpv4Addr(ipv4BootProto)));
            }
            if (ipv6BootProto != null) {
                ipconfig.setIpV6Addresses(Collections.singletonList(createIpv6Addr(ipv6BootProto)));
            }
            return ipconfig;
        }

        private Map<String, VdsNetworkInterface> createNics(int count) {
            Map<String, VdsNetworkInterface> nicMap = new HashMap<>();
            for (int i = 0; i < count; i++) {
                String nicName = "eth" + i;
                VdsNetworkInterface nic = createNic(nicName);
                nicMap.put(nicName, nic);
            }
            return nicMap;
        }

        private Nic createNic(String name) {
            var iface = new Nic();
            iface.setId(Guid.newGuid());
            iface.setName(name);
            return iface;
        }

        private Vlan createVlan(String baseName, Integer vlanId) {
            var iface = new Vlan();
            iface.setId(Guid.newGuid());
            iface.setName(baseName + "." + vlanId);
            iface.setVlanId(vlanId);
            return iface;
        }

        private Bond createBond(String name, List<String> slaves) {
            var iface = new Bond();
            iface.setId(Guid.newGuid());
            iface.setName(name);
            iface.setSlaves(slaves);
            return iface;
        }

        private NetworkAttachment createAttachment(Guid nicId, Guid netId) {
            NetworkAttachment attachment = new NetworkAttachment();
            attachment.setId(Guid.newGuid());
            attachment.setNicId(nicId);
            attachment.setNetworkId(netId);
            return attachment;
        }

        private static IPv4Address createIpv4Addr(Ipv4BootProtocol bootProtocol) {
            var ipv4 = new IPv4Address();
            ipv4.setBootProtocol(bootProtocol);
            return ipv4;
        }

        private static IpV6Address createIpv6Addr(Ipv6BootProtocol bootProtocol) {
            var ipv6 = new IpV6Address();
            ipv6.setBootProtocol(bootProtocol);
            return ipv6;
        }

    }
}