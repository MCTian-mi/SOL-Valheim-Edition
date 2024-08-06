package vice.sol_valheim.mixin;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

@Mixin(ItemFood.class)
public class ItemFoodMixin {

    @Inject(method = "onItemRightClick(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Lnet/minecraft/util/ActionResult;",
            at = @At("HEAD"),
            cancellable = true)
    private void onCanConsume(World worldIn, EntityPlayer playerIn, EnumHand handIn, CallbackInfoReturnable<ActionResult<ItemStack>> info) {
//        var item = (Item) (Object) this;
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        var item = itemStack.getItem();

        if (item == Items.ROTTEN_FLESH) {
            playerIn.setActiveHand(handIn);

            info.setReturnValue(new ActionResult<>(EnumActionResult.SUCCESS, itemStack));
            info.cancel();
            return;
        }

        var canEat = ((PlayerEntityMixinDataAccessor) playerIn).sol_valheim$getFoodData().canEat(item);
        if (canEat) {
            playerIn.setActiveHand(handIn);
            info.setReturnValue(new ActionResult<>(EnumActionResult.SUCCESS, itemStack));
            info.cancel();
            return;
        }

        info.setReturnValue(new ActionResult<>(EnumActionResult.FAIL, itemStack));
        info.cancel();
    }

    @Inject(method = "onFoodEaten(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)V",
            at = @At("TAIL"))
    private void onEaten(ItemStack stack, World worldIn, EntityPlayer player, CallbackInfo ci) {
        ((PlayerEntityMixinDataAccessor) player).sol_valheim$onEatFood(worldIn, stack);
    }
}
