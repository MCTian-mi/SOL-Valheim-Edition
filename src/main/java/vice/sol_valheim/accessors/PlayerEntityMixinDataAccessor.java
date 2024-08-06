package vice.sol_valheim.accessors;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import vice.sol_valheim.ValheimFoodData;

public interface PlayerEntityMixinDataAccessor {

    ValheimFoodData sol_valheim$getFoodData();

    void sol_valheim$onEatFood(World world, ItemStack stack);
}
