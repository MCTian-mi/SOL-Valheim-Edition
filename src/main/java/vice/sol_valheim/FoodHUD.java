package vice.sol_valheim;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

public class FoodHUD {

    public static void renderHud() {
        Minecraft client = Minecraft.getMinecraft();

        if (client.player == null) return;

        var solPlayer = (PlayerEntityMixinDataAccessor) client.player;

        var foodData = solPlayer.sol_valheim$getFoodData();
        if (foodData == null) {
            return;
        }

        boolean useLargeIcons = ModConfig.client.useLargeIcons;

        ScaledResolution scaledresolution = new ScaledResolution(client);
        int width = scaledresolution.getScaledWidth() / 2 + 91;
        int height = scaledresolution.getScaledHeight() - 39 - (useLargeIcons ? 6 : 0);

        int offset = 1;
        int size = useLargeIcons ? 14 : 9;

        for (var food : foodData.ItemEntries) {
            renderFoodSlot(food, width, size, offset, height, useLargeIcons);
            offset++;
        }

        if (foodData.DrinkSlot != null) {
            renderFoodSlot(foodData.DrinkSlot, width, size, offset, height, useLargeIcons);
        }
    }

    private static void renderFoodSlot(ValheimFoodData.EatenFoodItem food, int width, int size, int offset, int height, boolean useLargeIcons) {

        var foodConfig = ModConfig.getFoodConfig(food.item);
        if (foodConfig == null)
            return;

        var isDrink = food.item.getDefaultInstance().getItemUseAction() == EnumAction.DRINK;
        int bgColor = isDrink ? color(96, 52, 104, 163) : color(96, 0, 0, 0);
        int yellow = color(255, 255, 191, 0);

        int startWidth = width - (size * offset) - offset + 1;
        float ticksLeftPercent = Float.min(1.0F, (float) food.ticksLeft / foodConfig.getTime());
        int barHeight = Integer.max(1, (int)((size + 2f) * ticksLeftPercent));
        int barColor = ticksLeftPercent < ModConfig.common.eatAgainPercentage ?
                color(180, 255, 10, 10) :
                color(96, 0, 0, 0);

        var time = (float) food.ticksLeft / (20 * 60);
        var scale = useLargeIcons ? 0.75f : 0.5f;
        var isSeconds = false;
        var minutes = String.format("%.0f", time);

        if (time < 1f) {
            isSeconds = true;
            time =  (float) food.ticksLeft / 20;
        }

        GlStateManager.pushMatrix();
        Gui.drawRect(startWidth, height, startWidth + size, height + size, bgColor);
        Gui.drawRect(startWidth, Integer.max(height, height - barHeight + size), startWidth + size, height + size, barColor);
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.translate(startWidth * (useLargeIcons ? 0.3333f : 1f), height * (useLargeIcons ? 0.3333f : 1f), 0f);
        renderGUIItem(new ItemStack(food.item, 1), startWidth + 1, height + 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 200.0f);
        drawFont(minutes, startWidth + (minutes.length() > 1 ? 6 : 12), height + 10, isSeconds ? color(255, 237, 57, 57) : color(255, 255, 255, 255));
        if (!foodConfig.extraEffects.isEmpty()) {
            drawFont("+" + foodConfig.extraEffects.size(), startWidth + 6, height, yellow);
        }
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private static void renderGUIItem(ItemStack stack, int x, int y) {

        var itemRenderer = Minecraft.getMinecraft().getItemRenderer();

        //itemRenderer.textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 100F);
        GlStateManager.translate(8.0, 8.0, 0.0);
        GlStateManager.scale(1.0F, -1.0F, 1.0F);
        GlStateManager.scale(16.0F, 16.0F, 16.0F);

        itemRenderer.renderItem(Minecraft.getMinecraft().player, stack, ItemCameraTransforms.TransformType.GUI);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
    }

    private static void drawFont(String str, int x, int y, int color) {
        Minecraft.getMinecraft().fontRenderer.drawString(str, x, y, color);
    }

    public static int color(int alpha, int red, int green, int blue) {
        return alpha << 24 | red << 16 | green << 8 | blue;
    }
}
