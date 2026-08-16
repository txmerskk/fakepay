package com.txmersk.fakepay;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FakePayClient implements ClientModInitializer {
    public static boolean enabled = true;
    public static boolean soundEnabled = true;
    public static boolean indicatorEnabled = true;

    private static KeyBinding menuKey;

    @Override
    public void onInitializeClient() {
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fakepay.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.fakepay"
        ));

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("pay")
                    .then(ClientCommandManager.argument("player", StringArgumentType.word())
                            .then(ClientCommandManager.argument("amount", StringArgumentType.word())
                                    .executes(context -> {
                                        if (!enabled) {
                                            MinecraftClient client = MinecraftClient.getInstance();
                                            if (client.player != null) {
                                                client.player.sendMessage(Text.literal("Fake Pay is disabled."), false);
                                            }
                                            return 0;
                                        }

                                        String player = StringArgumentType.getString(context, "player");
                                        String amount = StringArgumentType.getString(context, "amount");
                                        showFakePayment(player, amount);
                                        return 1;
                                    }))));
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (menuKey.wasPressed()) {
                client.setScreen(new FakePayScreen(client.currentScreen));
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            if (!indicatorEnabled) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.getWindow() == null) return;

            String text = enabled ? "FAKE PAY: ON" : "FAKE PAY: OFF";
            int width = client.textRenderer.getWidth(text);
            int x = client.getWindow().getScaledWidth() - width - 8;
            int y = 8;
            int color = enabled ? 0x55FF55 : 0xFF5555;
            drawContext.drawTextWithShadow(client.textRenderer, text, x, y, color);
        });
    }

    private static void showFakePayment(String player, String amount) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // This is deliberately a local client chat message. No command or packet is sent.
        Text message = Text.literal("You paid ")
                .append(Text.literal(player).styled(style -> style.withColor(0x55FFFF)))
                .append(Text.literal(" $"))
                .append(Text.literal(amount).styled(style -> style.withColor(0x55FF55)));

        client.player.sendMessage(message, false);

        if (soundEnabled) {
            client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
        }
    }

    public static class FakePayScreen extends Screen {
        private final Screen parent;

        protected FakePayScreen(Screen parent) {
            super(Text.literal("Fake Pay Settings"));
            this.parent = parent;
        }

        @Override
        protected void init() {
            int centerX = this.width / 2;
            int y = this.height / 2 - 50;

            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
                    Text.literal("Fake Pay: " + (enabled ? "ON" : "OFF")),
                    button -> {
                        enabled = !enabled;
                        button.setMessage(Text.literal("Fake Pay: " + (enabled ? "ON" : "OFF")));
                    }).dimensions(centerX - 100, y, 200, 20).build());

            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
                    Text.literal("Sound: " + (soundEnabled ? "ON" : "OFF")),
                    button -> {
                        soundEnabled = !soundEnabled;
                        button.setMessage(Text.literal("Sound: " + (soundEnabled ? "ON" : "OFF")));
                    }).dimensions(centerX - 100, y + 25, 200, 20).build());

            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
                    Text.literal("Corner Indicator: " + (indicatorEnabled ? "ON" : "OFF")),
                    button -> {
                        indicatorEnabled = !indicatorEnabled;
                        button.setMessage(Text.literal("Corner Indicator: " + (indicatorEnabled ? "ON" : "OFF")));
                    }).dimensions(centerX - 100, y + 50, 200, 20).build());

            this.addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
                    Text.literal("Done"),
                    button -> this.close()).dimensions(centerX - 100, y + 85, 200, 20).build());
        }

        @Override
        public void close() {
            MinecraftClient.getInstance().setScreen(parent);
        }

        @Override
        public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            this.renderBackground(context, mouseX, mouseY, delta);
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 30, 0xFFFFFF);
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("Local cosmetic only — nothing is sent to the server."),
                    this.width / 2, 48, 0xAAAAAA);
            super.render(context, mouseX, mouseY, delta);
        }
    }
}
