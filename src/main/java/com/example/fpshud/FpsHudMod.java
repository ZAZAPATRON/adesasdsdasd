package com.example.fpshud;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class FpsHudMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client == null || client.textRenderer == null) return;

            int fps = (int) client.getWindow().getFps();
            String s = fps + " FPS";

            TextRenderer tr = client.textRenderer;
            int width = client.getWindow().getScaledWidth();
            int textWidth = tr.getWidth(s);
            int x = width - textWidth - 12; // right padding
            int y = 8; // top padding

            // Draw larger by drawing scale transform if DrawContext supports matrices
            try {
                drawContext.getMatrices().push();
                drawContext.getMatrices().scale(1.8f, 1.8f, 1.0f);
                int sx = (int) (x / 1.8f);
                int sy = (int) (y / 1.8f);
                drawContext.drawText(tr, Text.of(s), sx, sy, 0x66CCFF, true);
            } catch (Exception e) {
                // Fallback: normal size
                drawContext.drawText(tr, Text.of(s), x, y, 0x66CCFF, true);
            } finally {
                try { drawContext.getMatrices().pop(); } catch (Exception ignored) {}
            }
        });
    }
}
