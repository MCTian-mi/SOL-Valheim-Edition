package vice.sol_valheim.mixin;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vice.sol_valheim.accessors.EntityLivingBaseDamageAccessor;

@Mixin(EntityLivingBase.class)
public class EntityLivingBaseMixin implements EntityLivingBaseDamageAccessor {

    @Shadow
    private long lastDamageStamp;

    @Override
    public long solv$getLastDamageStamp() {
        return lastDamageStamp;
    }
}
