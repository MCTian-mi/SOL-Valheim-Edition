package vice.sol_valheim;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.DataSerializerEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Mod(modid = SOLValheim.MOD_ID)
public class SOLValheim {

	public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
	public static final String MOD_ID = "sol_valheim";
	private static AttributeModifier speedBuff;

	public static AttributeModifier getSpeedBuffModifier() {
		if (speedBuff == null)
			speedBuff = new AttributeModifier("sol_valheim_speed_buff", ModConfig.common.speedBoost, 1); // TODO?

		return speedBuff;
	}


	@Mod.EventHandler
	public void onInit(@NotNull FMLInitializationEvent event) {

		ForgeRegistries.DATA_SERIALIZERS.register(new DataSerializerEntry(ValheimFoodData.FOOD_DATA_SERIALIZER).setRegistryName("food_data")); // TODO

		// TODO: config

		if (ModConfig.common.foodConfigs.isEmpty()) {
			System.out.println("Generating default food configs, this might take a second.");
			long startTime = System.nanoTime();

			ForgeRegistries.ITEMS.forEach(ModConfig::getFoodConfig);

//			AutoConfig.getConfigHolder(ModConfig.class).save();

			long endTime = System.nanoTime();
			long executionTime = (endTime - startTime) / 1000000;
			System.out.println("Generating default food configs took " + executionTime + "ms.");
		}

//
//		try	{
//			var field = FoodProperties.class.getDeclaredField("canAlwaysEat");
//			field.setBoolean(Items.ROTTEN_FLESH.getFoodProperties(), true);
//		}
//		catch (Exception e) {
//			System.out.println(e);
//		}
	}


	public static void addTooltip(ItemStack item, ITooltipFlag flag, List<String> list) { // TODO

		var food = item.getItem();
		if (food == Items.ROTTEN_FLESH) {
			list.add(TextFormatting.GREEN + "☠ Empties Your Stomach!");
			return;
		}

		var config = ModConfig.getFoodConfig(food);
		if (config == null)
			return;

		var hearts = config.getHearts() % 2 == 0 ? config.getHearts() / 2 : String.format("%.1f", (float) config.getHearts() / 2f);
		list.add(TextFormatting.RED + "❤ " + hearts + " Heart" + (config.getHearts() / 2f > 1 ? "s" : ""));
		list.add(TextFormatting.DARK_RED + "☀ " + String.format("%.1f", config.getHealthRegen()) + " Regen");

		var minutes = (float) config.getTime() / (20 * 60);

		list.add(TextFormatting.GOLD + "⌚ " + String.format("%.0f", minutes) + " Minute" + (minutes > 1 ? "s" : ""));

		for (var effect : config.extraEffects) {
			var eff = effect.getEffect();
			if (eff == null)
				continue;

			list.add(TextFormatting.GREEN + "★ " + eff.getName() + (effect.amplifier > 1 ? " " + effect.amplifier : ""));
		}

		if (item.getItemUseAction() == EnumAction.DRINK) {
			list.add(TextFormatting.AQUA + "❄ Refreshing!");

		}
	}
}
