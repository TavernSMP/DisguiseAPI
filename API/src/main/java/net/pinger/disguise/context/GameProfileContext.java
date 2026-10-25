package net.pinger.disguise.context;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.pinger.disguise.skin.Skin;

import java.util.UUID;

public class GameProfileContext {

    public static GameProfile createProfile(Skin skin) {
        var properties = HashMultimap.<String, Property>create();
        properties.put("textures", PropertyContext.createProperty(skin));
        return new GameProfile(UUID.randomUUID(), "Player", new PropertyMap(properties));
    }
}
