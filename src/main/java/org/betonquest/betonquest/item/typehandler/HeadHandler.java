package org.betonquest.betonquest.item.typehandler;

import io.papermc.lib.PaperLib;
import org.betonquest.betonquest.BetonQuest;
import org.betonquest.betonquest.api.profiles.Profile;
import org.betonquest.betonquest.exceptions.InstructionParseException;
import org.betonquest.betonquest.item.QuestItem;
import org.betonquest.betonquest.utils.PlayerConverter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles metadata about player Skulls.
 */
@SuppressWarnings("PMD.TooManyMethods")
public abstract class HeadHandler implements ItemMetaHandler<SkullMeta> {
    /**
     * Owner metadata about the Skull.
     */
    public static final String META_OWNER = "owner";

    /**
     * PlayerId metadata about the Skull.
     */
    public static final String META_PLAYER_ID = "player-id";

    /**
     * Encoded texture metadata about the Skull.
     */
    public static final String META_TEXTURE = "texture";

    /**
     * Variable placeholder literal for player name.
     */
    private static final String VARIABLE_PLAYER_NAME = "%player%";

    /**
     * Existence of the player UUID.
     */
    private QuestItem.Existence playerIdE = QuestItem.Existence.WHATEVER;

    /**
     * Existence of the encoded texture.
     */
    private QuestItem.Existence textureE = QuestItem.Existence.WHATEVER;

    /**
     * An optional player name owner of the skull.
     */
    @Nullable
    private String owner;

    /**
     * An optional player ID owner of the skull, used in conjunction with the encoded texture.
     */
    @Nullable
    private UUID playerId;

    /**
     * An optional encoded texture URL of the skull, used in conjunction with the player UUID.
     */
    @Nullable
    private String texture;

    /**
     * Existence of the owner.
     */
    private QuestItem.Existence ownerE = QuestItem.Existence.WHATEVER;

    /**
     * Construct a new HeadHandler.
     */
    public HeadHandler() {
    }

    /**
     * Get an appropriate implementation of the HeadHandler based upon the type of server running.
     *
     * @return An appropriate HeadHandler instance.
     */
    public static HeadHandler getServerInstance() {
        if (PaperLib.isPaper()) {
            return new PaperHeadHandler();
        } else {
            return new SpigotHeadHandler(BetonQuest.getInstance().getLoggerFactory().create(SpigotHeadHandler.class));
        }
    }

    /**
     * Serialize the specified SkullMeta data into a String for item persistence.
     *
     * @param skullMeta The SkullMeta data to serialize.
     * @return A String representation of the SkullMeta data.
     */
    public static String serializeSkullMeta(final SkullMeta skullMeta) {
        final Map<String, String> props;
        if (PaperLib.isPaper()) {
            props = PaperHeadHandler.parseSkullMeta(skullMeta);
        } else {
            props = SpigotHeadHandler.parseSkullMeta(skullMeta);
        }
        return props.entrySet().stream()
                .map(it -> it.getKey() + ":" + it.getValue())
                .collect(Collectors.joining(" ", " ", ""));
    }

    @Override
    public Class<SkullMeta> metaClass() {
        return SkullMeta.class;
    }

    @Override
    public Set<String> keys() {
        return Set.of(META_OWNER, META_PLAYER_ID, META_TEXTURE);
    }

    @Override
    @Nullable
    public String serializeToString(final SkullMeta meta) {
        final String serialized = serializeSkullMeta(meta);
        if (serialized.isBlank()) {
            return null;
        }
        return serialized.substring(1);
    }

    @Override
    public void set(final String key, final String data) throws InstructionParseException {
        switch (key) {
            case META_OWNER -> setOwner(data);
            case META_PLAYER_ID -> setPlayerId(data);
            case META_TEXTURE -> setTexture(data);
            default -> throw new InstructionParseException("Unknown head key: " + key);
        }
    }

    @Contract("_ -> fail")
    @Override
    public void populate(final SkullMeta meta) {
        throw new UnsupportedOperationException("Use #populate(SkullMeta, Profile) instead");
    }

    /**
     * Set the owner name to the specified value.
     *
     * @param string The new String name for the owner.
     */
    public void setOwner(final String string) {
        if (QuestItem.NONE_KEY.equalsIgnoreCase(string)) {
            ownerE = QuestItem.Existence.FORBIDDEN;
        } else {
            owner = string;
            ownerE = QuestItem.Existence.REQUIRED;
        }
    }

    /**
     * Get the profile of the skull's owner.
     * Also resolves the owner name to a player if it is a variable.
     *
     * @param profile The Profile that the item is made for
     * @return The profile of the skull's owner.
     */
    @Nullable
    public Profile getOwner(@Nullable final Profile profile) {
        if (profile != null && VARIABLE_PLAYER_NAME.equals(owner)) {
            return profile;
        }
        if (owner != null) {
            final OfflinePlayer player = Bukkit.getOfflinePlayer(owner);
            return PlayerConverter.getID(player);
        }
        return null;
    }

    /**
     * Get the player UUID.
     *
     * @return The player ID.
     */
    @Nullable
    public UUID getPlayerId() {
        return playerId;
    }

    /**
     * Set the player UUID to the specified value.
     *
     * @param playerId The new UUID player ID.
     */
    public void setPlayerId(final String playerId) {
        this.playerId = UUID.fromString(playerId);
        this.playerIdE = QuestItem.Existence.REQUIRED;
    }

    /**
     * Get the encoded texture.
     *
     * @return The encoded texture.
     */
    @Nullable
    public String getTexture() {
        return texture;
    }

    /**
     * Set the encoded texture to the specified value.
     *
     * @param texture The new encoded texture.
     */
    public void setTexture(final String texture) {
        this.texture = texture;
        this.textureE = QuestItem.Existence.REQUIRED;
    }

    /**
     * Check to see if the specified owner name matches this HeadHandler metadata.
     *
     * @param string The owner to check.
     * @return True if this metadata is required and matches, false otherwise.
     */
    public boolean checkOwner(@Nullable final String string) {
        return switch (ownerE) {
            case WHATEVER -> true;
            case REQUIRED -> string != null && string.equals(owner);
            case FORBIDDEN -> string == null;
        };
    }

    /**
     * Check to see if the specified player UUID matches this HeadHandler metadata.
     *
     * @param playerId The player UUID to check.
     * @return True if this metadata is required and matches, false otherwise.
     */
    public boolean checkPlayerId(@Nullable final UUID playerId) {
        return switch (playerIdE) {
            case WHATEVER -> true;
            case REQUIRED -> playerId != null && playerId.equals(this.playerId);
            case FORBIDDEN -> playerId == null;
        };
    }

    /**
     * Check to see if the specified encoded texture matches this HeadHandler metadata.
     *
     * @param string The encoded texture to check.
     * @return True if this metadata is required and matches, false otherwise.
     */
    public boolean checkTexture(@Nullable final String string) {
        return switch (textureE) {
            case WHATEVER -> true;
            case REQUIRED -> string != null && string.equals(texture);
            case FORBIDDEN -> string == null;
        };
    }
}
