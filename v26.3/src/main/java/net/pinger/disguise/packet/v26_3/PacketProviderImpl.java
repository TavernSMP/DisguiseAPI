package net.pinger.disguise.packet.v26_3;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.protocol.Packet;
import net.pinger.disguise.annotation.PacketHandler;
import net.pinger.disguise.packet.PacketProvider;
import net.pinger.disguise.skin.Skin;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;

@PacketHandler(version = "26.3")
public class PacketProviderImpl implements PacketProvider {

    public PacketProviderImpl(Plugin plugin) {
    }

    @Override
    public GameProfile getGameProfile(Player player) {
        return ((CraftPlayer) player).getProfile();
    }

    @Override
    public Skin getProperty(Player player) {
        return this.getGameProfile(player).properties().get("textures").stream()
                .findFirst()
                .map(property -> new Skin(property.value(), property.signature()))
                .orElse(null);
    }

    @Override
    public void updateProperties(Player player, @Nonnull Skin skin) {
        GameProfile profile = this.getGameProfile(player);
        Multimap<String, Property> properties = HashMultimap.create(profile.properties());
        properties.removeAll("textures");
        properties.put("textures", new Property("textures", skin.getValue(), skin.getSignature()));
        ((CraftPlayer) player).getHandle().gameProfile =
                new GameProfile(profile.id(), profile.name(), new PropertyMap(properties));
    }

    @Override
    public void clearProperties(Player player) {
        GameProfile profile = this.getGameProfile(player);
        Multimap<String, Property> properties = HashMultimap.create(profile.properties());
        properties.removeAll("textures");
        ((CraftPlayer) player).getHandle().gameProfile =
                new GameProfile(profile.id(), profile.name(), new PropertyMap(properties));
    }

    @Override
    public void updateName(Player player, String name) {
        GameProfile profile = this.getGameProfile(player);
        ((CraftPlayer) player).getHandle().gameProfile =
                new GameProfile(profile.id(), name, profile.properties());
    }

    @Override
    public void sendPacket(Player player, Object... packets) {
        for (Object packet : packets) {
            ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet);
        }
    }

    @Override
    public void sendServerPackets(Player player) {
        player.setPlayerProfile(player.getPlayerProfile());
    }
}
