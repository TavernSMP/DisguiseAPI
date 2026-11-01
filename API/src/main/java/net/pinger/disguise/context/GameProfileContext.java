package net.pinger.disguise.context;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Field;
import java.util.UUID;
import net.pinger.disguise.skin.Skin;

public class GameProfileContext {

    private static final ClassValue<Field> PROFILE_FIELDS = new ClassValue<>() {
        @Override
        protected Field computeValue(Class<?> type) {
            for (Class<?> current = type; current != null; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (field.getType() == GameProfile.class) {
                        field.setAccessible(true);
                        return field;
                    }
                }
            }
            throw new IllegalStateException("No GameProfile field in " + type.getName());
        }
    };

    public static void updateProfile(Object playerHandle, GameProfile profile) {
        try {
            // Match by type because Spigot obfuscates the 1.21.11 field name.
            PROFILE_FIELDS.get(playerHandle.getClass()).set(playerHandle, profile);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot replace player GameProfile", e);
        }
    }

    public static GameProfile createProfile(Skin skin) {
        var properties = HashMultimap.<String, Property>create();
        properties.put("textures", PropertyContext.createProperty(skin));
        return new GameProfile(UUID.randomUUID(), "Player", new PropertyMap(properties));
    }
}
