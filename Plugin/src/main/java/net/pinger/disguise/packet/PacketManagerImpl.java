package net.pinger.disguise.packet;

import net.pinger.disguise.DisguisePlugin;
import net.pinger.disguise.server.MinecraftServer;

import java.util.Set;

public class PacketManagerImpl implements PacketManager {

    private final DisguisePlugin plugin;
    private PacketProvider provider;

    public PacketManagerImpl(DisguisePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public PacketProvider find() {
        if (!MinecraftServer.isVersion("26.3")) {
            return null;
        }
        if (this.provider == null) {
            this.provider = new net.pinger.disguise.packet.v26_3.PacketProviderImpl(this.plugin);
        }
        return this.provider;
    }

    @Override
    public PacketProvider getProvider() {
        return this.provider;
    }

    @Override
    public Set<Class<? extends PacketProvider>> getRegisteredProviders() {
        return Set.of(net.pinger.disguise.packet.v26_3.PacketProviderImpl.class);
    }
}
