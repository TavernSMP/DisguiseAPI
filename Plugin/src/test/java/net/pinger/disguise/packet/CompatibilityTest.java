package net.pinger.disguise.packet;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.SharedConstants;
import net.pinger.disguise.context.GameProfileContext;
import net.pinger.disguise.DisguisePlugin;
import net.pinger.disguise.item.XMaterial;
import net.pinger.disguise.server.MinecraftServer;
import net.pinger.disguise.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockMakers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompatibilityTest {

    private static final String VERSION = System.getProperty("disguise.test.minecraftVersion", "26.3");

    private Player player;
    private ServerPlayer handle;
    private PacketProvider provider;
    private GameProfile original;
    private Skin skin;
    private DisguisePlugin plugin;

    @BeforeAll
    static void setupServer() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Server server = mock(Server.class);
        when(server.getBukkitVersion()).thenReturn(VERSION + (VERSION.equals("1.21.11") ? "-R0.2-SNAPSHOT" : "-R0.1-SNAPSHOT"));
        when(server.getVersion()).thenReturn("Spigot " + VERSION + " (MC: " + VERSION + ")");
        when(server.getName()).thenReturn("Spigot");
        when(server.getLogger()).thenReturn(Logger.getLogger("DisguiseAPI-test"));
        Bukkit.setServer(server);
    }

    @BeforeEach
    void setupPlayer() throws ClassNotFoundException {
        handle = mock(ServerPlayer.class);
        when(handle.getGameProfile()).thenCallRealMethod();
        String craftPackage = VERSION.equals("1.21.11") ? "v1_21_R7." : "";
        Class<? extends Player> craftPlayer = Class.forName("org.bukkit.craftbukkit." + craftPackage + "entity.CraftPlayer")
                .asSubclass(Player.class);
        player = mock(craftPlayer, invocation -> switch (invocation.getMethod().getName()) {
            case "getHandle" -> handle;
            case "getProfile" -> handle.getGameProfile();
            default -> RETURNS_DEFAULTS.answer(invocation);
        });
        var properties = HashMultimap.<String, Property>create();
        properties.put("textures", new Property("textures", "old-value", "old-signature"));
        properties.put("custom", new Property("custom", "preserved"));
        original = new GameProfile(UUID.randomUUID(), "Original", new PropertyMap(properties));
        GameProfileContext.updateProfile(handle, original);
        skin = mock(Skin.class);
        when(skin.getValue()).thenReturn("new-value");
        when(skin.getSignature()).thenReturn("new-signature");
        plugin = mock(DisguisePlugin.class, withSettings().mockMaker(MockMakers.INLINE));
        provider = new PacketManagerImpl(plugin).find();
    }

    @Test
    void replacesImmutableTexturesAndRestoresOriginalSkinAndName() {
        provider.updateProperties(player, skin);
        provider.updateName(player, "Disguised");
        GameProfile disguised = handle.getGameProfile();
        assertEquals(original.id(), disguised.id());
        assertEquals("Disguised", disguised.name());
        assertEquals("new-value", disguised.properties().get("textures").iterator().next().value());
        assertEquals("new-signature", disguised.properties().get("textures").iterator().next().signature());
        assertEquals(original.properties().get("custom"), disguised.properties().get("custom"));
        assertEquals("old-value", original.properties().get("textures").iterator().next().value());
        when(skin.getValue()).thenReturn("old-value");
        when(skin.getSignature()).thenReturn("old-signature");
        provider.updateProperties(player, skin);
        provider.updateName(player, original.name());
        assertEquals(original, handle.getGameProfile());
    }

    @Test
    void clearsTexturesWithoutRemovingOtherProperties() {
        provider.clearProperties(player);
        provider.clearProperties(player);
        assertTrue(handle.getGameProfile().properties().get("textures").isEmpty());
        assertEquals(original.properties().get("custom"), handle.getGameProfile().properties().get("custom"));
        assertEquals(original.id(), handle.getGameProfile().id());
        assertEquals(original.name(), handle.getGameProfile().name());
        assertNull(provider.getProperty(player));
    }

    @Test
    void refreshesThroughSpigotPacketsAndRestoresPlayerState() {
        ServerLevel level = mock(ServerLevel.class);
        var server = mock(net.minecraft.server.MinecraftServer.class);
        PlayerList playerList = mock(PlayerList.class);
        handle.connection = mock(ServerGamePacketListenerImpl.class);
        when(handle.level()).thenReturn(level);
        when(level.getServer()).thenReturn(server);
        when(server.getPlayerList()).thenReturn(playerList);
        when(handle.createCommonSpawnInfo(level)).thenReturn(mock(CommonPlayerSpawnInfo.class,
                withSettings().mockMaker(MockMakers.INLINE)));
        Location location = new Location(null, 12, 64, 24);
        when(player.getLocation()).thenReturn(location);
        when(player.getUniqueId()).thenReturn(original.id());
        when(plugin.isEnabled()).thenReturn(true);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(Bukkit.getServer().getScheduler()).thenReturn(scheduler);
        doAnswer(invocation -> {
            invocation.getArgument(1, Runnable.class).run();
            return null;
        }).when(scheduler).runTask(eq(plugin), any(Runnable.class));
        Player viewer = mock(Player.class);
        doReturn(List.of(player, viewer)).when(Bukkit.getServer()).getOnlinePlayers();
        var info = mock(ClientboundPlayerInfoUpdatePacket.class);
        try (var updates = mockStatic(ClientboundPlayerInfoUpdatePacket.class,
                withSettings().mockMaker(MockMakers.INLINE))) {
            updates.when(() -> ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(handle))).thenReturn(info);
            provider.sendServerPackets(player);
        }
        ArgumentCaptor<Packet<?>> packets = ArgumentCaptor.forClass(Packet.class);
        verify(handle.connection, times(3)).send(packets.capture());
        assertInstanceOf(ClientboundPlayerInfoRemovePacket.class, packets.getAllValues().get(0));
        assertSame(info, packets.getAllValues().get(1));
        ClientboundRespawnPacket respawn = assertInstanceOf(ClientboundRespawnPacket.class, packets.getAllValues().get(2));
        assertEquals(ClientboundRespawnPacket.KEEP_ALL_DATA, respawn.dataToKeep());
        verify(handle).onUpdateAbilities();
        verify(handle.connection).teleport(location);
        verify(handle).resetSentInfo();
        verify(playerList).sendPlayerPermissionLevel(handle);
        verify(playerList).sendLevelInfo(handle, level);
        verify(playerList).sendAllPlayerInfo(handle);
        verify(viewer).hidePlayer(plugin, player);
        verify(viewer).showPlayer(plugin, player);
    }

    @Test
    void createsImmutableSkullProfile() {
        GameProfile profile = GameProfileContext.createProfile(skin);
        assertEquals("Player", profile.name());
        assertEquals("new-value", profile.properties().get("textures").iterator().next().value());
        assertEquals("new-signature", profile.properties().get("textures").iterator().next().signature());
    }

    @Test
    void resolvesMatchingAdapterAndCachesIt() {
        PacketManagerImpl manager = new PacketManagerImpl(mock(DisguisePlugin.class));
        assertEquals(2, manager.getProviderCount());
        assertTrue(manager.getRegisteredProviders().contains(net.pinger.disguise.packet.v26_3.PacketProviderImpl.class));
        assertTrue(manager.getRegisteredProviders().contains(net.pinger.disguise.packet.v1_21_11.PacketProviderImpl.class));
        Class<? extends PacketProvider> expected = VERSION.equals("1.21.11")
                ? net.pinger.disguise.packet.v1_21_11.PacketProviderImpl.class
                : net.pinger.disguise.packet.v26_3.PacketProviderImpl.class;
        assertInstanceOf(expected, manager.find());
        assertSame(manager.find(), manager.getProvider());
        assertSame(manager.find(), manager.find());
    }

    @Test
    void rejectsUnsupportedServers() {
        try (var ignored = mockStatic(MinecraftServer.class, withSettings().mockMaker(MockMakers.INLINE))) {
            PacketManagerImpl manager = new PacketManagerImpl(mock(DisguisePlugin.class));
            assertNull(manager.find());
            assertNull(manager.getProvider());
        }
    }

    @Test
    void recognizesNewVersionFormatForProvidersAndMaterials() {
        assertEquals(VERSION, MinecraftServer.fromRaw(SharedConstants.getCurrentVersion().name()).getVersion());
        assertEquals(VERSION, MinecraftServer.CURRENT.getVersion());
        assertTrue(MinecraftServer.atLeast("1.21.8"));
        assertEquals(VERSION.equals("26.3"), MinecraftServer.atLeast("26.2"));
        assertFalse(MinecraftServer.atLeast("26.4"));
        assertEquals("1.21.11", MinecraftServer.fromRaw("1.21.11-R0.1-SNAPSHOT").getVersion());
        assertTrue(XMaterial.supports(21));
        assertNotNull(XMaterial.PLAYER_HEAD.parseMaterial());
    }
}
