package vice.sol_valheim.mixin;

import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vice.sol_valheim.ModConfig;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {

    protected WorldServerMixin(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    @Inject(method = "tick()V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;setWorldTime(J)V", ordinal = 0))
    public void onSleep(CallbackInfo ci) {

        if (!ModConfig.common.passTicksDuringNight) {
            return;
        }

        var dayTime = getWorldInfo().getWorldTime();

        var l = dayTime + 24000L;
        var newTime = l - l % 24000L;

        var passedTicks = Math.max(0, newTime - dayTime);
        if (passedTicks == 0) {
            return;
        }

        for (var player : playerEntities) {
            var foodData = ((PlayerEntityMixinDataAccessor) player).sol_valheim$getFoodData();
            if (foodData.DrinkSlot != null) {
                foodData.DrinkSlot.ticksLeft = (int) Math.max(0, foodData.DrinkSlot.ticksLeft - passedTicks);
            }
            for (var item : foodData.ItemEntries) {
                item.ticksLeft = (int) Math.max(0, item.ticksLeft - passedTicks);
            }
        }
    }
}