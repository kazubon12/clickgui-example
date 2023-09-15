package me.enokitoraisu.features.gui.clickgui.item.items;

import me.enokitoraisu.features.gui.clickgui.utils.RenderUtil;
import me.enokitoraisu.features.gui.clickgui.item.Item;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.input.Keyboard;

public class ItemString extends Item<StrSetting> {
    private boolean moved;
    private int input;
    private String value;
    private boolean typing;
    private int selectStart;
    private int selectEnd;

    public ItemString(StrSetting strSetting, int x, int y, int width, int height) {
        super(strSetting, x, y, width, height);
        this.moved = false;
        this.value = getObject().getValue();
        this.input = this.value.length();
        this.typing = false;
        this.selectStart = 0;
        this.selectEnd = 0;
    }

    @Override
    public int drawScreen(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, int offset) {
        if (!getObject().getVisible()) return 0;

        this.offset = offset;
        float y = offset + this.y;

        RenderUtil.rect(matrices, x, y, width, height, 0x80000000);
        mc.textRenderer.drawWithShadow(matrices, value, x + 5, y + height / 2F - mc.textRenderer.fontHeight / 2F, -1);
        if (typing) {
            int len = moved ? this.input : value.length();
            float len_width = mc.textRenderer.getWidth(value.substring(0, len));
            RenderUtil.rect(matrices, x + 5 + len_width, y + height / 2F - mc.textRenderer.fontHeight / 2F, 1, mc.textRenderer.fontHeight, 0xFFFFFF | alpha() << 24);
            if (hasSelect()) {
                float start = mc.textRenderer.getWidth(value.substring(0, selectStart));
                float selectWidth = mc.textRenderer.getWidth(value.substring(selectStart, selectEnd));
                RenderUtil.minusrect(matrices, x + 5 + start, y + height / 2F - mc.textRenderer.fontHeight / 2F, selectWidth, mc.textRenderer.fontHeight, 0xFFFFFFFF);
            }
        }

        return height;
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (bounding(mouseX, mouseY) && mouseButton == 0) {
            typing = !typing;
            if (typing) {
                value = getObject().getValue();
                input = value.length();
                moved = false;
            } else {
                input = value.length();
                moved = false;
                getObject().setValue(value);
            }
        } else {
            selectReset();
            typing = false;
            input = value.length();
            moved = false;
            value = getObject().getValue();
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!typing) return;
        if (controlPressed()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_A:
                    if (controlPressed()) {
                        selectStart = 0;
                        selectEnd = value.length();
                        return;
                    }
                case GLFW.GLFW_KEY_C:
                    if (controlPressed()) {
                        return;
                    }
            }
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_LEFT -> {
                if (value.isEmpty()) break;
                this.moved = true;
                this.input = Math.max(this.input - 1, 0);
                if (this.input == value.length())
                    moved = false;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                if (value.isEmpty()) break;
                this.moved = true;
                this.input = Math.min(this.input + 1, value.length());
                if (this.input == value.length())
                    moved = false;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (value.isEmpty()) break;
                if (hasSelect()) {
                    value = value.substring(0, selectStart) + value.substring(selectEnd);
                    selectReset();
                    moved = false;
                    input = 0;
                    break;
                }
                value = getDELETEString(value, moved, input);
                if (value.length() <= input) {
                    moved = false;
                    input = value.length();
                }
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (value.isEmpty()) break;
                if (hasSelect()) {
                    value = value.substring(0, selectStart) + value.substring(selectEnd);
                    selectReset();
                    moved = false;
                    input = 0;
                    break;
                }
                value = getBACKSPACEString(value, moved, input);
                if (input > 0)
                    input -= 1;
                if (value.length() <= input) {
                    moved = false;
                    input = value.length();
                }
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_ESCAPE -> {
                typing = false;
                getObject().setValue(value);
            }
        }
    }

    @Override
    public void charTyped(char chr, int modifiers) {
        if (hasSelect()) {
            value = value.substring(0, selectStart) + chr + value.substring(selectEnd);
            selectReset();
            moved = false;
            input = 0;
            return;
        }
        value = append(chr, value, moved, input);
        if (!moved) input = value.length();
        else input += 1;
    }

    private void selectReset() {
        selectStart = -1;
        selectEnd = -1;
    }

    private boolean hasSelect() {
        return selectStart != -1 && selectEnd != -1;
    }

    private boolean controlPressed() {
        /*
        TODO: 時間あるときにやります
        if (MinecraftClient.IS_SYSTEM_MAC) {
            return Keyboard.isKeyDown(Keyboard.KEY_LMETA) || Keyboard.isKeyDown(Keyboard.KEY_RMETA);
        } else {
            return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
        }

         */
        return false;
    }

    public String append(char typedChar, String value, boolean position, int inputPosition) {
        if (position) {
            String before = value.substring(0, inputPosition);
            String after = value.substring(inputPosition);
            return before + typedChar + after;
        } else {
            return value + typedChar;
        }
    }

    public String getBACKSPACEString(String value, boolean position, int inputPosition) {
        if (position) {
            String before = value.substring(0, inputPosition);
            String after = value.substring(inputPosition);
            int splitPos = before.isEmpty() ? 0 : before.length() - 1;
            return before.substring(0, splitPos) + after;
        } else {
            int splitPos = value.isEmpty() ? 0 : value.length() - 1;
            return value.substring(0, splitPos);
        }
    }

    public String getDELETEString(String value, boolean position, int inputPosition) {
        if (position) {
            String before = value.substring(0, inputPosition);
            String after = value.substring(inputPosition);
            int splitPos = after.isEmpty() ? 0 : 1;
            return before + after.substring(splitPos);
        } else {
            return value;
        }
    }

    public int alpha() {
        float value = System.currentTimeMillis() % 1000L / 1000F;
        if (value >= 0.5) {
            return (int) ((1 - (value - 0.5F) * 2) * 255);
        } else {
            return (int) ((value * 2) * 255);
        }
    }
}
