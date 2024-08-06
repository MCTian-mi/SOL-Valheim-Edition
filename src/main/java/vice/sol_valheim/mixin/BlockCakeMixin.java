package vice.sol_valheim.mixin;

import net.minecraft.block.BlockCake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

@Mixin(BlockCake.class)
public class BlockCakeMixin {

    @Redirect(method = "eatCake(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;canEat(Z)Z"))
    private boolean canEatCake(EntityPlayer player, boolean ignoreHunger) {
        return ignoreHunger || ((PlayerEntityMixinDataAccessor) player).sol_valheim$getFoodData().canEat(Items.CAKE);
    }
}