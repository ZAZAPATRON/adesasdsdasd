package com.example.fpshud;

import java.lang.reflect.Method;

/**
 * Minimal mod initializer that avoids compile-time Fabric/Minecraft deps by using reflection.
 * It launches a background daemon thread which updates the Minecraft window title with FPS.
 */
public class FpsHudMod {
    public FpsHudMod() {}

    // Fabric will reflectively call this method if configured as the entrypoint.
    public void onInitializeClient() {
        try {
            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient");
                        Method getInstance = mcClass.getMethod("getInstance");
                        Object client = getInstance.invoke(null);
                        if (client == null) { Thread.sleep(1000); continue; }

                        // attempt to get window and fps via reflection
                        Object window = null;
                        try {
                            Method getWindow = mcClass.getMethod("getWindow");
                            window = getWindow.invoke(client);
                        } catch (NoSuchMethodException ignored) {}

                        int fps = -1;
                        if (window != null) {
                            try {
                                Method getFps = window.getClass().getMethod("getFps");
                                Object fpsObj = getFps.invoke(window);
                                if (fpsObj instanceof Number) fps = ((Number) fpsObj).intValue();
                            } catch (NoSuchMethodException e) {
                                // ignore
                            }
                        }

                        // fallback: try a common method on client
                        if (fps < 0) {
                            try {
                                Method getFps2 = mcClass.getMethod("getFramerate");
                                Object fpsObj = getFps2.invoke(client);
                                if (fpsObj instanceof Number) fps = ((Number) fpsObj).intValue();
                            } catch (Exception ignored) {}
                        }

                        String title = "FPS HUD" + (fps >= 0 ? (" — " + fps + " FPS") : "");

                        // try setTitle or setWindowTitle on window
                        if (window != null) {
                            try {
                                Method setTitle = window.getClass().getMethod("setTitle", String.class);
                                setTitle.invoke(window, title);
                            } catch (NoSuchMethodException e1) {
                                try {
                                    Method setTitle2 = window.getClass().getMethod("setWindowTitle", String.class);
                                    setTitle2.invoke(window, title);
                                } catch (Exception ignored) {}
                            } catch (Exception ignored) {}
                        }

                        Thread.sleep(1000);
                    } catch (Throwable ex) {
                        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    }
                }
            });
            t.setDaemon(true);
            t.setName("fpshud-updater");
            t.start();
        } catch (Throwable t) {
            // swallow to avoid breaking loader
        }
    }
}
