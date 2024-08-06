package vice.sol_valheim.mixin;

import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vice.sol_valheim.ModConfig;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {

    @Inject(method = "setSprinting(Z)V", at = @At("HEAD"), cancellable = true)
    public void canStartSprinting(boolean bool, CallbackInfo ci) {
        if (!bool) return;

        var solPlayer = (PlayerEntityMixinDataAccessor) this;
        var mayFly = ((EntityPlayerSP) (Object) this).capabilities.isFlying; // TODO

        if (mayFly || ((EntityPlayerSP) (Object) this).ticksExisted < ModConfig.common.respawnGracePeriod * 20) {
            return;
        }

        var foodData = solPlayer.sol_valheim$getFoodData();
        if (foodData == null || foodData.ItemEntries.isEmpty()) {
            ci.cancel();
        }
    }
}