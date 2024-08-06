package vice.sol_valheim.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

@Mixin(EntityPlayerMP.class)
public abstract class EntityPlayerMPMixin extends EntityPlayer {

    public EntityPlayerMPMixin(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Inject(method = "onItemUseFinish()V",
            at = @At("HEAD"))
    public void render(CallbackInfo ci) {
        if (!activeItemStack.isEmpty() && isHandActive() && activeItemStack.getItemUseAction() == EnumAction.DRINK) {
            ((PlayerEntityMixinDataAccessor) this).sol_valheim$getFoodData().eatItem(activeItemStack.getItem());
        }
    }
}