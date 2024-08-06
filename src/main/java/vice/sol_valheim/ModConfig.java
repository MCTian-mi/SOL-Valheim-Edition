package vice.sol_valheim;

import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.*;


@Config(modid = SOLValheim.MOD_ID, name = SOLValheim.MOD_ID + '/' + SOLValheim.MOD_ID)
public class ModConfig {

    @Config.Comment("Config options for client-only features")
    @Config.Name("Client Options")
    public static ClientOptions client = new ClientOptions();

    @Config.Comment("TODO") // TODO
    @Config.Name("Common Options")
    @Config.RequiresMcRestart
    public static CommonOptions common = new CommonOptions();

    public static class ClientOptions {

        @Config.Comment("Enlarge the currently eaten food icons")
        public boolean useLargeIcons = true;
    }


    public static class CommonOptions {

        @Config.Comment("Default time in seconds that food should last per saturation level")
        public int defaultTimer = 180;

        @Config.Comment("Speed at which regeneration should occur")
        public float regenSpeedModifier = 1f;

        @Config.Comment("Time in ticks that regeneration should wait after taking damage")
        public int regenDelay = 20 * 10;

        @Config.Comment("Time in seconds after spawning before sprinting is disabled")
        public int respawnGracePeriod = 60 * 5;

        @Config.Comment("Extra speed given when your hearts are full (0 to disable)")
        public float speedBoost = 0.20f;

        @Config.Comment("Number of hearts to start with")
        public int startingHealth = 3;

        @Config.Comment("Number of food slots (range 2-5, default 3)")
        public int maxSlots = 3;

        @Config.Comment("Percentage remaining before you can eat again")
        public float eatAgainPercentage = 0.2F;

        @Config.Comment("Boost given to other foods when drinking")
        public float drinkSlotFoodEffectivenessBonus = 0.10F;

        @Config.Comment("Simulate food ticking down during night")
        public boolean passTicksDuringNight = true;

        @Config.Comment("""
                    Food nutrition and effect overrides (Auto Generated if Empty)
                    - nutrition: Affects Heart Gain & Health Regen
                    - saturationModifier: Affects Food Duration & Player Speed
                    - healthRegenModifier: Multiplies health regen speed
                    - extraEffects: Extra effects provided by eating the food. Format: { String ID, float duration, int amplifier }
                """)
        public Dictionary<ResourceLocation, FoodConfig> foodConfigs = new Hashtable<>();

        public static class FoodConfig {

            public int nutrition;
            public float saturationModifier = 1f;
            public float healthRegenModifier = 1f;
            public List<MobEffectConfig> extraEffects = new ArrayList<>();

            public int getTime() {
                var time = (int) (ModConfig.common.defaultTimer * 20 * saturationModifier * nutrition);
                return Math.max(time, 6000);
            }

            public int getHearts() {
                return Math.max(nutrition, 2);
            }

            public float getHealthRegen() {
                return MathHelper.clamp(nutrition * 0.10f * healthRegenModifier, 0.25f, 2f);
            }
        }

        public static final class MobEffectConfig {
            @Config.Comment("Mob Effect ID")
            public String ID;

            @Config.Comment("Effect duration percentage (1f is the entire food duration)")
            public float duration = 1f;

            @Config.Comment("Effect Level")
            public int amplifier = 1;

            public Potion getEffect() {
                return ForgeRegistries.POTIONS.getValue(new ResourceLocation(ID));
            }
        }
    }

    public static CommonOptions.FoodConfig getFoodConfig(Item item) {
        var isDrink = item.getDefaultInstance().getItemUseAction() == EnumAction.DRINK;
        var isEdible = item.getDefaultInstance().getItemUseAction() == EnumAction.EAT; // will this work?
        if (item != Items.CAKE && !isEdible && !isDrink)
            return null;

        var existing = ModConfig.common.foodConfigs.get(item.getRegistryName());
        if (existing == null) {
            var registry = Objects.requireNonNull(item.getRegistryName()).toString();

            var nutrition = 0;
            var saturationModifier = 0F;

            if (item instanceof ItemFood itemFood) {
                nutrition = itemFood.getHealAmount(new ItemStack(itemFood));
                saturationModifier = itemFood.getSaturationModifier(new ItemStack(itemFood));
            }

            if (item == Items.CAKE) {
                nutrition = 10;
                saturationModifier = 0.7f;
            }

            if (isDrink) {
                if (registry.contains("potion")) {
                    nutrition = 4;
                    saturationModifier = 0.5f;
                } else if (registry.contains("milk")) {
                    nutrition = 6;
                    saturationModifier = 1f;
                } else {
                    nutrition = 2;
                    saturationModifier = 0.5f;
                }
            }

            existing = new CommonOptions.FoodConfig();
            existing.nutrition = nutrition;
            existing.healthRegenModifier = 1f;
            existing.saturationModifier = saturationModifier;

            if (registry.startsWith("farmers")) {
                existing.nutrition = (int) ((existing.nutrition * 1.25));
                existing.saturationModifier = existing.saturationModifier * 1.10f;
                existing.healthRegenModifier = 1.25f;
            }

            if (registry.equals("minecraft:golden_apple") || registry.equals("minecraft:enchanted_golden_apple")) {
                existing.nutrition = 10;
                existing.healthRegenModifier = 1.5f;
            }

//            if (registry.equals("minecraft:beetroot_soup")) {
//                var effectConfig = new Common.MobEffectConfig();
//                effectConfig.ID = BuiltInRegistries.MOB_EFFECT.getKey(MobEffects.MOVEMENT_SPEED).toString();
//                existing.extraEffects.add(effectConfig);
//            }

            ModConfig.common.foodConfigs.put(item.getRegistryName(), existing);
        }
        return existing;
    }
}