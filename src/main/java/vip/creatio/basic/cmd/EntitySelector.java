package vip.creatio.basic.cmd;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.ArgumentParserSelector;
import net.minecraft.server.CriterionConditionValue;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import vip.creatio.accessor.Func;
import vip.creatio.accessor.Reflection;
import vip.creatio.basic.tools.Wrapper;
import vip.creatio.basic.util.NMS;
import vip.creatio.common.Pair;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntitySelector implements Wrapper<net.minecraft.server.EntitySelector> {

    private static final Func<CriterionConditionValue.FloatRange> FLOAT_RANGE = Reflection.constructor(CriterionConditionValue.FloatRange.class, Float.class, Float.class);

    private final net.minecraft.server.EntitySelector selector;

    public EntitySelector(net.minecraft.server.EntitySelector nms) {
        this.selector = nms;
    }

    public EntitySelector(String stringSelector) throws CommandSyntaxException {
        this.selector = new ArgumentParserSelector(new StringReader(stringSelector)).parse();
    }

    public EntitySelector(int maxResults,
                          boolean includesEntities,
                          boolean worldLimited,
                          Predicate<Entity> predicate,
                          Pair<Float, Float> range,
                          Function<Vector, Vector> position,
                          @Nullable BoundingBox hitBox,
                          BiConsumer<Vector, List<? extends Entity>> order,
                          boolean currentEntity,
                          @Nullable String playerName,
                          @Nullable UUID entityUUID,
                          @Nullable EntityType type,
                          boolean usesSelector) {
        this.selector = new net.minecraft.server.EntitySelector(
                maxResults,
                includesEntities,
                worldLimited,
                e -> predicate.test(NMS.toBukkit(e)),
                FLOAT_RANGE.invoke(range.getKey(), range.getValue()),
                v -> NMS.toNms(position.apply(NMS.toBukkit(v))),
                NMS.nullable(hitBox, NMS::toNms),
                (v, l) -> order.accept(NMS.toBukkit(v), NMS.batch(l, NMS::toBukkit)),
                currentEntity,
                playerName,
                entityUUID,
                NMS.toNms(type),
                usesSelector);
    }

    public int getMaxResults() {
        return selector.a /* getMaxResults */ ();
    }

    public boolean includesEntities() {
        return selector.b /* includesEntities */ ();
    }

    public boolean isSelfSelector() {
        return selector.c /* isSelcSelector */ ();
    }

    public boolean isWorldLimited() {
        return selector.d /* isWorldLimited */ ();
    }

    public Entity findSingleEntity(final CommandSender sender) throws CommandSyntaxException {
        return NMS.toBukkit(selector.a /* findSingleEntity */ (NMS.toNms(sender)));
    }

    public Entity findSingleEntity() throws CommandSyntaxException {
        return findSingleEntity(Bukkit.getServer().getConsoleSender());
    }

    public List<? extends Entity> findEntities(final CommandSender sender) throws CommandSyntaxException {
        return NMS.batch(selector.getEntities(NMS.toNms(sender)), NMS::toBukkit);
    }

    public List<? extends Entity> findEntities() throws CommandSyntaxException {
        return findEntities(Bukkit.getServer().getConsoleSender());
    }

    public Player findSinglePlayer(final CommandSender sender) throws CommandSyntaxException {
        return NMS.toBukkit(selector.c /* findSinglePlayer */ (NMS.toNms(sender)));
    }

    public Player findSinglePlayer() throws CommandSyntaxException {
        return findSinglePlayer(Bukkit.getServer().getConsoleSender());
    }

    public List<Player> findPlayers(final CommandSender sender) throws CommandSyntaxException {
        return NMS.batch(selector.d /* findPlayers */ (NMS.toNms(sender)), NMS::toBukkit);
    }

    public List<Player> findPlayers() throws CommandSyntaxException {
        return findPlayers(Bukkit.getServer().getConsoleSender());
    }

    @Override
    public net.minecraft.server.EntitySelector unwrap() {
        return selector;
    }

    @Override
    public Class<? extends net.minecraft.server.EntitySelector> wrappedClass() {
        return net.minecraft.server.EntitySelector.class;
    }
}
