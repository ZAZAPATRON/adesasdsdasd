package com.example.fpshud;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Entrypoint that registers a HudRenderCallback reflectively (if Fabric API is present).
 * If registration fails, a background thread updates the window title with current FPS.
 */
public class FpsHudMod {
    public FpsHudMod() {}

    // Fabric will try to call this method for client entrypoints.
    public void onInitializeClient() {
        if (tryRegisterHudCallback()) return;
        startTitleUpdater();
    }

    private boolean tryRegisterHudCallback() {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Class<?> hudInterface = Class.forName("net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback", true, cl);
            Field eventField = hudInterface.getField("EVENT");
            Object event = eventField.get(null);

            InvocationHandler handler = (proxy, method, args) -> {
                if ("onHudRender".equals(method.getName())) {
                    try {
                        Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient", true, cl);
                        Method getInstance = mcClass.getMethod("getInstance");
                        Object mc = getInstance.invoke(null);
                        if (mc == null) return null;

                        int fps = -1;
                        try {
                            Method getFps = mcClass.getMethod("getFps");
                            Object fpsObj = getFps.invoke(mc);
                            if (fpsObj instanceof Number) fps = ((Number) fpsObj).intValue();
                        } catch (NoSuchMethodException ignored) {
                            try {
                                Method getWindow = mcClass.getMethod("getWindow");
                                Object window = getWindow.invoke(mc);
                                Method getFramerate = window.getClass().getMethod("getFramerate");
                                Object fpsObj = getFramerate.invoke(window);
                                if (fpsObj instanceof Number) fps = ((Number) fpsObj).intValue();
                            } catch (Exception ignored2) {}
                        }

                        // As a simple, safe visual: update the window title with FPS. HUD drawing via DrawContext
                        // would require more reflection and is less robust across mappings; this is a safe fallback
                        try {
                            Method getWindow = mcClass.getMethod("getWindow");
                            Object window = getWindow.invoke(mc);
                            Method setTitle = window.getClass().getMethod("setTitle", String.class);
                            setTitle.invoke(window, "FPS HUD — " + (fps >= 0 ? (fps + " FPS") : "n/a"));
                        } catch (NoSuchMethodException e) {
                            try {
                                Method getWindow = mcClass.getMethod("getWindow");
                                Object window = getWindow.invoke(mc);
                                Method setTitle2 = window.getClass().getMethod("setWindowTitle", String.class);
                                setTitle2.invoke(window, "FPS HUD — " + (fps >= 0 ? (fps + " FPS") : "n/a"));
                            } catch (Exception ignored3) {}
                        }
                    } catch (Throwable t) {
                        // ignore per-frame errors
                    }
                }
                return null;
            };

            Object callback = Proxy.newProxyInstance(cl, new Class[]{hudInterface}, handler);
            // Event.register(Object) exists; call reflectively
            Method register = event.getClass().getMethod("register", Object.class);
            register.invoke(event, callback);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private void startTitleUpdater() {
        try {
            Thread t = new Thread(() -> {
                try {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Class<?> mcClass = Class.forName("net.minecraft.client.MinecraftClient", true, cl);
                    while (true) {
                        try {
                            Method getInstance = mcClass.getMethod("getInstance");
                            Object mc = getInstance.invoke(null);
                            if (mc == null) { Thread.sleep(1000); continue; }

                            int fps = -1;
                            try {
                                Method getFps = mcClass.getMethod("getFps");
                                Object fpsObj = getFps.invoke(mc);
                                if (fpsObj instanceof Number) fps = ((Number) fpsObj).intValue();
                            } catch (NoSuchMethodException ignored) {
                                try {
                                    Method getWindow = mcClass.getMethod("getWindow");
                                    Object window = getWindow.invoke(mc);
                                    Method getFramerate = window.getClass().getMethod("getFramerate");
                                    Object fpsObj = getFramerate.invoke(window);
                                    if (fpsObj instanceof Number) fps = ((Number) fpsObj).intValue();
                                } catch (Exception ignored2) {}
                            }

                            try {
                                Method getWindow = mcClass.getMethod("getWindow");
                                Object window = getWindow.invoke(mc);
                                Method setTitle = window.getClass().getMethod("setTitle", String.class);
                                setTitle.invoke(window, "FPS HUD — " + (fps >= 0 ? (fps + " FPS") : "n/a"));
                            } catch (NoSuchMethodException e) {
                                try {
                                    Method getWindow = mcClass.getMethod("getWindow");
                                    Object window = getWindow.invoke(mc);
                                    Method setTitle2 = window.getClass().getMethod("setWindowTitle", String.class);
                                    setTitle2.invoke(window, "FPS HUD — " + (fps >= 0 ? (fps + " FPS") : "n/a"));
                                } catch (Exception ignored3) {}
                            }

                            Thread.sleep(1000);
                        } catch (Throwable inner) {
                            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        }
                    }
                } catch (Throwable outer) {
                    // give up silently
                }
            }, "fpshud-title-updater");
            t.setDaemon(true);
            t.start();
        } catch (Throwable t) {
            // ignore
        }
    }
}

