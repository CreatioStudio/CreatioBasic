package vip.creatio.basic.tools;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import vip.creatio.basic.CLibBasic;
import vip.creatio.basic.packet.Packet;
import vip.creatio.basic.packet.PacketHandler;
import vip.creatio.basic.packet.PacketListener;
import vip.creatio.accessor.annotation.AnnotationProcessor;
import vip.creatio.common.util.ReflectUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.BiConsumer;

public class ListenerProcessor implements AnnotationProcessor<Listener> {

    private final ListenerRegister register;
    private final PacketListener listener;

    public ListenerProcessor(ListenerRegister register) {
        this.register = register;
        this.listener = CLibBasic.getInstance().getPacketListener();
    }

    public ListenerProcessor(ListenerRegister register, PacketListener packet) {
        this.register = register;
        this.listener = packet;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(Listener listener, Method mth) {
        if (Modifier.isStatic(mth.getModifiers())
                && !Modifier.isPrivate(mth.getModifiers())) {
            if (mth.getParameterCount() == 1) {
                if (Event.class.isAssignableFrom(mth.getParameterTypes()[0])) {
                    register.register(mth);
                } else {
                    CLibBasic.intern("The first param of  " + mth + " should extends " + Event.class.getName() + "!");
                }
            } else if (mth.getParameterCount() == 2) {
                if (Packet.class.isAssignableFrom(mth.getParameterTypes()[0])) {
                    if (mth.getParameterTypes()[1].isAssignableFrom(Player.class)) {
                        PacketHandler<Packet<?>> handler;
                        if (mth.getReturnType() == void.class) {
                            BiConsumer<Packet<?>, Player> consumer = (BiConsumer<Packet<?>, Player>) ReflectUtil.createLambda(BiConsumer.class, mth);
                            handler = (packet, player) -> {
                                consumer.accept(packet, player);
                                return true;
                            };
                        } else if (mth.getReturnType() == boolean.class) {
                            handler = ReflectUtil.createLambda(PacketHandler.class, mth);
                        } else {
                            CLibBasic.intern("Return type of method " + mth + " should be void or boolean!");
                            return;
                        }
                        this.listener.register((Class<Packet<?>>) mth.getParameterTypes()[0], handler);
                    } else {
                        CLibBasic.intern("The second param of  " + mth + " should be the super type of " + Player.class.getName() + "!");
                    }
                } else {
                    CLibBasic.intern("The first param of  " + mth + " should extends " + Packet.class.getName() + "!");
                }
            }
        } else {
            CLibBasic.intern("Method " + mth + " should be non-private and static in oreder to use @Listener!");
        }
    }

    @Override
    public Class<Listener> getTargetClass() {
        return Listener.class;
    }
}
