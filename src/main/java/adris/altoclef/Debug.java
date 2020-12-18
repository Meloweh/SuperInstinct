package adris.altoclef;

import net.fabricmc.loader.launch.common.FabricMixinBootstrap;
import net.fabricmc.loom.util.FabricApiExtension;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class Debug {

    public static void logInternal(String message) {
        System.out.println("ALTO CLEF: " + message);
    }

    public static void logMessage(String message, boolean prefix) {
        if (MinecraftClient.getInstance().player != null) {
            if (prefix) {
                message = "\u00A72\u00A7l\u00A7o[Alto Clef] \u00A7r" + message;
            }
            MinecraftClient.getInstance().player.sendMessage(Text.of(message), false);
            //MinecraftClient.getInstance().player.sendChatMessage(msg);
        } else {
            logInternal(message);
        }
    }
    public static void logMessage(String message) {
        logMessage(message, true);
    }

    public static void logWarning(String message) {
        logInternal("WARNING: " + message);
        if (MinecraftClient.getInstance().player != null) {
            String msg = "\u00A72\u00A7l\u00A7o[Alto Clef] \u00A7c" + message + "\u00A7r";
            MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);
            //MinecraftClient.getInstance().player.sendChatMessage(msg);
        }
    }

    public static void logError(String message) {
        String stacktrace = getStack(2);
        System.err.println(message);
        System.err.println("at:");
        System.err.println(stacktrace);
        if (MinecraftClient.getInstance().player != null) {
            String msg = "\u00A72\u00A7l\u00A7c[Alto Clef ERROR]" + message + "\nat:\n" + stacktrace + "\u00A7r";
            MinecraftClient.getInstance().player.sendMessage(Text.of(msg), false);
            //MinecraftClient.getInstance().player.sendChatMessage(msg);
        }
    }

    public static void logStack() {
        logInternal("STACKTRACE: \n" + getStack(2));
    }

    private static String getStack(int toSkip) {
        StringBuilder stacktrace = new StringBuilder();
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            if (toSkip-- <= 0) {
                stacktrace.append(ste.toString()).append("\n");
            }
        }
        return stacktrace.toString();
    }
}
