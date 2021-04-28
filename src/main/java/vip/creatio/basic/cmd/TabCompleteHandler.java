package vip.creatio.basic.cmd;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import vip.creatio.basic.CLibBasic;

import java.util.Arrays;

public class TabCompleteHandler {

    public static void test() {

        LiteralArgument arg2 = Argument.of("shitter")
                .then(Argument.of("set")
                        .then(Argument.of("day"))
                        .then(Argument.of("noon"))
                        .then(Argument.of("night"))
                        .then(Argument.of("midnight"))
                        .then(Argument.arg("time", ArgumentTypes.ofInt()).executes(c -> {
                            System.out.println(c.getInput());
                            c.setErrMessage("Fuck");
                            return false;
                        }).suggests((c, b) -> {
                            for (int i = 0; i < 50; i++) {
                                b.suggest(i);
                            }
                            return b.buildFuture();
                        })))
                .then(LiteralArgument.of("add")
                        .then(Argument.arg("time", ArgumentTypes.ofInt()).then(Argument.arg("loc", ArgumentTypes.ofVec3()).executes(c -> {
                            System.out.println("Yes!");
                            Vector vec = c.getArgument("loc", ArgumentTypes.Coords.class).getPosition(c.getSource());
                            c.getWorld().spawnParticle(Particle.FLAME, vec.toLocation(c.getWorld()), 500);
                            return true;
                        }))))
                .then(Argument.of("query")
                        .then(Argument.of("daytime"))
                        .then(Argument.of("gametime"))
                        .then(Argument.of("day")).then(Argument.arg("entity", ArgumentTypes.ofEntity()).executes(c -> {
                            Entity e = c.getArgument("entity", EntitySelector.class).findSingleEntity(c.getSource());
                            System.out.println(e);
                            e.setVelocity(new Vector(0, 3, 0));
                            return true;
                        })));

        CLibBasic.getInstance().getCommandRegister().register(arg2, "Holy fucker!", Arrays.asList("aabb", "j2se"));
    }
}
