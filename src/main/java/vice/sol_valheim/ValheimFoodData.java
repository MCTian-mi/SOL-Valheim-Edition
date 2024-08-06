package vice.sol_valheim;

import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ValheimFoodData {

    public static final DataSerializer<ValheimFoodData> FOOD_DATA_SERIALIZER = new DataSerializer<>() {

        @Override
        public void write(PacketBuffer buffer, ValheimFoodData value) {
            buffer.writeCompoundTag(value.save(new NBTTagCompound()));
        }

        @Override
        public @NotNull ValheimFoodData read(PacketBuffer buffer) throws IOException {
            return ValheimFoodData.read(Objects.requireNonNull(buffer.readCompoundTag()));
        }

        @Override
        public @NotNull DataParameter<ValheimFoodData> createKey(int id) {
            return new DataParameter<>(id, this);
        }

        @Override
        public @NotNull ValheimFoodData copyValue(ValheimFoodData value) {
            var ret = new ValheimFoodData();
            ret.MaxItemSlots = value.MaxItemSlots;
            ret.ItemEntries = value.ItemEntries.stream().map(EatenFoodItem::new).collect(Collectors.toCollection(ArrayList::new));
            if (value.DrinkSlot != null)
                ret.DrinkSlot = new EatenFoodItem(value.DrinkSlot);
            return ret;
        }

    };

    public List<EatenFoodItem> ItemEntries = new ArrayList<>();
    public EatenFoodItem DrinkSlot;
    public int MaxItemSlots = ModConfig.common.maxSlots;

    public void eatItem(Item food) {
        if (food == Items.ROTTEN_FLESH)
            return;

        var config = ModConfig.getFoodConfig(food);
        if (config == null)
            return;

        var isDrink = food.getDefaultInstance().getItemUseAction() == EnumAction.DRINK;
        if (isDrink) {
            if (DrinkSlot != null && !DrinkSlot.canEatEarly())
                return;

            if (DrinkSlot == null)
                DrinkSlot = new EatenFoodItem(food, config.getTime());
            else {
                DrinkSlot.ticksLeft = config.getTime();
                DrinkSlot.item = food;
            }

            return;
        }

        var existing = getEatenFood(food);
        if (existing != null) {
            if (!existing.canEatEarly())
                return;

            existing.ticksLeft = config.getTime();
            return;
        }

        if (ItemEntries.size() < MaxItemSlots) {
            ItemEntries.add(new EatenFoodItem(food, config.getTime()));
            return;
        }

        for (var item : ItemEntries) {
            if (item.canEatEarly()) {
                item.ticksLeft = config.getTime();
                item.item = food;
                return;
            }
        }
    }

    public boolean canEat(Item food) {
        if (food == Items.ROTTEN_FLESH) {
            return true;
        }

        if (food.getDefaultInstance().getItemUseAction() == EnumAction.DRINK) {
            return DrinkSlot == null || DrinkSlot.canEatEarly();
        }

        var existing = getEatenFood(food);
        if (existing != null) {
            return existing.canEatEarly();
        }

        if (ItemEntries.size() < MaxItemSlots) {
            return true;
        }

        return ItemEntries.stream().anyMatch(EatenFoodItem::canEatEarly);
    }

    public EatenFoodItem getEatenFood(Item food) {
        return ItemEntries.stream()
                .filter((item) -> item.item == food)
                .findFirst()
                .orElse(null);
    }


    public void clear() {
        ItemEntries.clear();
        DrinkSlot = null;
    }


    public void tick() {
        for (var item : ItemEntries) {
            item.ticksLeft--;
        }

        if (DrinkSlot != null) {
            DrinkSlot.ticksLeft--;
            if (DrinkSlot.ticksLeft <= 0)
                DrinkSlot = null;
        }

        ItemEntries.removeIf(item -> item.ticksLeft <= 0);
        ItemEntries.sort(Comparator.comparingInt(a -> a.ticksLeft));
    }


    public float getTotalFoodNutrition() {
        float nutrition = 0f;
        for (var item : ItemEntries) {
            ModConfig.CommonOptions.FoodConfig food = ModConfig.getFoodConfig(item.item);
            if (food == null)
                continue;

            nutrition += food.getHearts();
        }

        if (DrinkSlot != null) {
            ModConfig.CommonOptions.FoodConfig food = ModConfig.getFoodConfig(DrinkSlot.item);
            if (food != null) {
                nutrition += food.getHearts();
            }

            nutrition = nutrition * (1.0f + ModConfig.common.drinkSlotFoodEffectivenessBonus);
        }

        return nutrition;
    }


    public float getRegenSpeed() {
        float regen = 0.25f;
        for (var item : ItemEntries) {
            ModConfig.CommonOptions.FoodConfig food = ModConfig.getFoodConfig(item.item);
            if (food == null)
                continue;

            regen += food.getHealthRegen();
        }

        if (DrinkSlot != null) {
            ModConfig.CommonOptions.FoodConfig food = ModConfig.getFoodConfig(DrinkSlot.item);
            if (food != null) {
                regen += food.getHealthRegen();
            }

            regen = regen * (1.0f + ModConfig.common.drinkSlotFoodEffectivenessBonus);
        }

        return regen;
    }


    public NBTTagCompound save(NBTTagCompound tag) {
        int count = 0;
        tag.setInteger("max_slots", MaxItemSlots);
        tag.setInteger("count", ItemEntries.size());
        for (var item : ItemEntries) {
            tag.setString("id" + count, Objects.requireNonNull(item.item.getRegistryName()).toString());
            tag.setInteger("ticks" + count, item.ticksLeft);
            count++;
        }

        if (DrinkSlot != null) {
            tag.setString("drink", Objects.requireNonNull(DrinkSlot.item.getRegistryName()).toString());
            tag.setInteger("drinkticks", DrinkSlot.ticksLeft);
        }

        return tag;
    }

    public static ValheimFoodData read(NBTTagCompound tag) {
        var instance = new ValheimFoodData();
        instance.MaxItemSlots = tag.getInteger("max_slots");

        var size = tag.getInteger("count");
        for (int count = 0; count < size; count++) {
            var str = tag.getString("id" + count);
            var ticks = tag.getInteger("ticks" + count);
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(str));

            instance.ItemEntries.add(new EatenFoodItem(item, ticks));
        }

        var drink = tag.getString("drink");
        var drinkTicks = tag.getInteger("drinkticks");

        if (!drink.isEmpty()) {
            var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(drink));
            instance.DrinkSlot = new EatenFoodItem(item, drinkTicks);
        }

        return instance;
    }


    public static class EatenFoodItem {
        public Item item;
        public int ticksLeft;

        public boolean canEatEarly() {
            if (ticksLeft < 1200) {
                return true;
            }

            var config = ModConfig.getFoodConfig(item);
            if (config == null) {
                return false;
            }

            return ((float) this.ticksLeft / config.getTime()) < ModConfig.common.eatAgainPercentage;
        }

        public EatenFoodItem(Item item, int ticksLeft) {
            this.item = item;
            this.ticksLeft = ticksLeft;
        }

        public EatenFoodItem(EatenFoodItem eaten) {
            this.item = eaten.item;
            this.ticksLeft = eaten.ticksLeft;
        }
    }
}