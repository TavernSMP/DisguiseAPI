package net.pinger.disguise.packet;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerPlayer;
import net.pinger.disguise.DisguisePlugin;
import net.pinger.disguise.context.GameProfileContext;
import net.pinger.disguise.item.XMaterial;
import net.pinger.disguise.server.MinecraftServer;
import net.pinger.disguise.skin.Skin;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;

import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompatibilityTest {

    private static final String VERSION = System.getProperty("disguise.test.minecraftVersion", "26.3");

    private CraftPlayer player;
    private ServerPlayer handle;
    private PacketProvider provider;
    private GameProfile original;
    private Skin skin;

    @BeforeAll
    static void setupServer() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Server server = mock(Server.class);
        when(server.getBukkitVersion()).thenReturn(VERSION + "-R0.1-SNAPSHOT");
        when(server.getVersion()).thenReturn("Paper " + VERSION + " (MC: " + VERSION + ")");
        when(server.getName()).thenReturn("Paper");
        when(server.getLogger()).thenReturn(Logger.getLogger("DisguiseAPI-test"));
        Bukkit.setServer(server);
    }

    @BeforeEach
    void setupPlayer() {
        player = mock(CraftPlayer.class);
        handle = mock(ServerPlayer.class);
        when(player.getHandle()).thenReturn(handle);
        when(player.getProfile()).thenAnswer(invocation -> handle.gameProfile);
        var properties = HashMultimap.<String, Property>create();
        properties.put("textures", new Property("textures", "old-value", "old-signature"));
        properties.put("custom", new Property("custom", "preserved"));
        original = new GameProfile(UUID.randomUUID(), "Original", new PropertyMap(properties));
        handle.gameProfile = original;
        skin = mock(Skin.class);
        when(skin.getValue()).thenReturn("new-value");
        when(skin.getSignature()).thenReturn("new-signature");
        provider = new PacketManagerImpl(mock(DisguisePlugin.class)).find();
    }

    @Test
    void replacesImmutableTexturesAndRestoresOriginalSkinAndName() {
        provider.updateProperties(player, skin);
        provider.updateName(player, "Disguised");
        GameProfile disguised = handle.gameProfile;
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
        assertEquals(original, handle.gameProfile);
    }

    @Test
    void clearsTexturesWithoutRemovingOtherProperties() {
        provider.clearProperties(player);
        provider.clearProperties(player);
        assertTrue(handle.gameProfile.properties().get("textures").isEmpty());
        assertEquals(original.properties().get("custom"), handle.gameProfile.properties().get("custom"));
        assertEquals(original.id(), handle.gameProfile.id());
        assertEquals(original.name(), handle.gameProfile.name());
        assertNull(provider.getProperty(player));
    }

    @Test
    void refreshesThroughPaperProfileApi() {
        var profile = mock(com.destroystokyo.paper.profile.PlayerProfile.class);
        when(player.getPlayerProfile()).thenReturn(profile);
        provider.sendServerPackets(player);
        verify(player).setPlayerProfile(profile);
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
        assertEquals(VERSION, SharedConstants.getCurrentVersion().name());
        assertEquals(VERSION, MinecraftServer.CURRENT.getVersion());
        assertTrue(MinecraftServer.atLeast("1.21.8"));
        assertEquals(VERSION.equals("26.3"), MinecraftServer.atLeast("26.2"));
        assertFalse(MinecraftServer.atLeast("26.4"));
        assertEquals("1.21.11", MinecraftServer.fromRaw("1.21.11-R0.1-SNAPSHOT").getVersion());
        assertTrue(XMaterial.supports(21));
        assertNotNull(XMaterial.PLAYER_HEAD.parseMaterial());
    }
}
