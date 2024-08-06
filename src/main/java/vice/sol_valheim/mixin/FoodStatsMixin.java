package vice.sol_valheim.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vice.sol_valheim.accessors.FoodStatsPlayerAccessor;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

@Mixin(FoodStats.class)
public class FoodStatsMixin implements FoodStatsPlayerAccessor {
    @Unique
    private EntityPlayer sol_valheim$player;

    @Unique
    @Override
    public EntityPlayer sol_valheim$getPlayer() {
        return sol_valheim$player;
    }

    @Override
    public void sol_valheim$setPlayer(EntityPlayer player) {
        sol_valheim$player = player;
    }

    @Inject(method = "addStats(Lnet/minecraft/item/ItemFood;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
    public void onEatFood(ItemFood foodItem, ItemStack stack, CallbackInfo ci) {
        if (sol_valheim$player == null) {
            System.out.println("sol_valheim$player is null in FoodData, this should never happen!");
            return;
        }

        var foodData = ((PlayerEntityMixinDataAccessor) sol_valheim$player).sol_valheim$getFoodData();
        if (foodData.canEat(foodItem))
            foodData.eatItem(foodItem);
    }
}