package net.pinger.disguise.packet.v1_21_11;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.Collections;
import javax.annotation.Nonnull;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.pinger.disguise.annotation.PacketHandler;
import net.pinger.disguise.context.GameProfileContext;
import net.pinger.disguise.packet.PacketProvider;
import net.pinger.disguise.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R7.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@PacketHandler(version = "1.21.11")
public class PacketProviderImpl implements PacketProvider {

    private final Plugin plugin;

    public PacketProviderImpl(Plugin plugin) {
        this.plugin = plugin;
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
        GameProfileContext.updateProfile(((CraftPlayer) player).getHandle(),
                new GameProfile(profile.id(), profile.name(), new PropertyMap(properties)));
    }

    @Override
    public void clearProperties(Player player) {
        GameProfile profile = this.getGameProfile(player);
        Multimap<String, Property> properties = HashMultimap.create(profile.properties());
        properties.removeAll("textures");
        GameProfileContext.updateProfile(((CraftPlayer) player).getHandle(),
                new GameProfile(profile.id(), profile.name(), new PropertyMap(properties)));
    }

    @Override
    public void updateName(Player player, String name) {
        GameProfile profile = this.getGameProfile(player);
        GameProfileContext.updateProfile(((CraftPlayer) player).getHandle(),
                new GameProfile(profile.id(), name, profile.properties()));
    }

    @Override
    public void sendPacket(Player player, Object... packets) {
        for (Object packet : packets) {
            ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet);
        }
    }

    @Override
    public void sendServerPackets(Player player) {
        // Get the entity player from the base player
        ServerPlayer sp = ((CraftPlayer) player).getHandle();
        ServerLevel level = sp.level();

        CommonPlayerSpawnInfo spawnInfo = sp.createCommonSpawnInfo(level);
        ClientboundRespawnPacket respawn = new ClientboundRespawnPacket(spawnInfo, ClientboundRespawnPacket.KEEP_ALL_DATA);

        // Remove the player from the list
        this.sendPacket(player, new ClientboundPlayerInfoRemovePacket(Collections.singletonList(player.getUniqueId())));
        this.sendPacket(player, ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(Collections.singletonList(sp)));

        // Send the respawn packet to the player
        // With the respawn packet, the player gets to see their own skin
        this.sendPacket(player, respawn);

        // Update scale
        sp.onUpdateAbilities();

        sp.connection.teleport(player.getLocation());

        // Send health, food, experience (food is sent together with health)
        sp.resetSentInfo();

        if (this.plugin.isEnabled()) {
            Bukkit.getScheduler().runTask(this.plugin, () -> {
                PlayerList playerList = level.getServer().getPlayerList();
                playerList.sendPlayerPermissionLevel(sp);
                playerList.sendLevelInfo(sp, level);
                playerList.sendAllPlayerInfo(sp);
            });
        }

        // Send the refresh packet to other players
        // Where they will be able to see the updated skin
        PacketProvider.refreshPlayer(player, this.plugin);
    }
}
