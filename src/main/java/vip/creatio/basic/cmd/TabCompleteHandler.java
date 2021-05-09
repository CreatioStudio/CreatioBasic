package vip.creatio.basic.cmd;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.permissions.ServerOperator;
import org.bukkit.util.Vector;
import vip.creatio.basic.CreatioBasic;
import vip.creatio.common.util.ArrayUtil;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TabCompleteHandler {

    public static void test() {

        LiteralArgument arg2 = Argument.of("shitter")
                .then(Argument.of("set")
                        .then(Argument.of("day"))
                        .then(Argument.of("noon"))
                        .then(Argument.of("night"))
                        .then(Argument.of("midnight").executes(c -> false))
                        .then(Argument.arg("time", ArgumentTypes.ofInt())
                                .executes(c -> {
                            System.out.println(c.getInput());
                            return false;
                        })
                                .suggests((c, b) -> {
                            for (int i = 0; i < 50; i++) {
                                b.suggest(i);
                            }
                            return b.buildFuture();
                        }).fallbacksFailure(c -> System.out.println("Fallback 66 !!!!!!")))
                        .fallbacksFailure(c -> System.out.println("Fallback 99 !!!!!!")))
                .then(LiteralArgument.of("add")
                        .then(Argument.arg("time", ArgumentTypes.ofInt()).then(Argument.arg("loc", ArgumentTypes.ofVec3()).executes(c -> {
                            System.out.println("Yes!");
                            Vector vec = c.getArgument("loc", ArgumentTypes.Coords.class).getPosition(c.getSender());
                            c.getWorld().spawnParticle(Particle.FLAME, vec.toLocation(c.getWorld()), 500);
                            return false;
                        }).suggests(SuggestionProviders.of("111222", "fucker", "fuckershit", "a", "aabb"))
                        .fallbacksFailure((c) -> System.out.println("Fallback 2 !!!!!")))))
                .then(Argument.of("query")
                        .then(Argument.of("daytime"))
                        .then(Argument.of("gametime"))
                        .then(Argument.of("day"))
                        .then(Argument.arg("entity", ArgumentTypes.ofEntity()).executes(c -> {
                            Entity e = c.getArgument("entity", EntitySelector.class).findSingleEntity(c.getSender());
                            System.out.println(e);
                            e.setVelocity(new Vector(0, 3, 0));
                            return true;
                        }))
                        .requiresSenderType(SenderType.CONSOLE)
                        .requires(ServerOperator::isOp))
                .then(Argument.of("test")
                        .then(Argument.arg("many", new MultiArgumentType<>(new FileArgumentType(CreatioBasic.getInstance().getDataFolder().getParentFile()))).executes(c -> {
                            List<File[]> arg = c.getArgument("many", List.class);
                            List<File> files = ArrayUtil.flat(arg);
                            files.forEach(System.out::println);
                        })))
                .then(Argument.of("test2")
                        .then(Argument.arg("many", new MultiArgumentType<>(new ClassArgumentType(Arrays.asList(String.class, Object.class, Integer.class, Double.class, Map.class)))).executes(c -> {
                            List<Class<?>[]> arg = c.getArgument("many", List.class);
                            List<Class<?>> files = ArrayUtil.flat(arg);
                            files.forEach(System.out::println);
                        })))
                .then(Argument.of("class")
                        .then(Argument.arg("classes", new MultiArgumentType<>(new ClassPathArgumentType(new File(CreatioBasic.getInstance().getDataFolder().getParentFile(), "ClassLoader/classes/compiled"))))
                        .executes(c -> {
                            List<File[]> arg = c.getArgument("classes", List.class);
                            List<File> files = ArrayUtil.flat(arg);
                            files.forEach(System.out::println);
                        }))
                )
                .fallbacksFailure(c -> System.out.println("Fallback 1 !!!!!!"));

        CreatioBasic.getInstance().getCommandRegister().register(arg2, "Holy fucker!", Arrays.asList("aabb", "j2se"));
    }
}
