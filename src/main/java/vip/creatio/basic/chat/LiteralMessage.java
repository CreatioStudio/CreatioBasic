package vip.creatio.basic.chat;

public class LiteralMessage implements Message {

    private final String string;

    public LiteralMessage(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }

    public String toString() {
        return this.string;
    }

}
