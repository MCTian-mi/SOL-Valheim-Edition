package vice.sol_valheim.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.FoodStats;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vice.sol_valheim.ModConfig;
import vice.sol_valheim.SOLValheim;
import vice.sol_valheim.ValheimFoodData;
import vice.sol_valheim.accessors.EntityLivingBaseDamageAccessor;
import vice.sol_valheim.accessors.FoodStatsPlayerAccessor;
import vice.sol_valheim.accessors.PlayerEntityMixinDataAccessor;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase implements PlayerEntityMixinDataAccessor {

    protected EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Unique
    @SuppressWarnings("all")
    private static final DataParameter<ValheimFoodData> sol_valheim$DATA_ACCESSOR = EntityDataManager.createKey(EntityPlayer.class, ValheimFoodData.FOOD_DATA_SERIALIZER);

    @Shadow
    protected FoodStats foodStats;

    @Override
    public ValheimFoodData sol_valheim$getFoodData() {
        return dataManager.get(sol_valheim$DATA_ACCESSOR);
    }

    @Unique
    private ValheimFoodData sol_valheim$food_data = new ValheimFoodData();

    @Unique
    @Override
    public void sol_valheim$onEatFood(World world, ItemStack stack) {
        if (stack.getItem() == Items.ROTTEN_FLESH) {
            sol_valheim$food_data.clear();
            sol_valheim$trackData();
            return;
        }

        sol_valheim$food_data.eatItem(stack.getItem());
        sol_valheim$trackData();
    }

    @Inject(method = "addExhaustion(F)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddExhaustion(float exhaustion, CallbackInfo info) {
        info.cancel();
    }

    @Inject(method = "getFoodStats()Lnet/minecraft/util/FoodStats;",
            at = @At("HEAD"))
    private void onGetFoodStats(CallbackInfoReturnable<FoodStats> cir) {
        // hack workaround for player data not being accessible in FoodData
        ((FoodStatsPlayerAccessor) foodStats).sol_valheim$setPlayer((EntityPlayer) (Object) this);
    }

    @Unique
    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        sol_valheim$tick();
    }

    @Unique
    private void sol_valheim$tick() {
        if (!isEntityAlive()) {
            sol_valheim$food_data.clear();
            sol_valheim$trackData();
            return;
        }

        if (!sol_valheim$food_data.ItemEntries.isEmpty()) {
            sol_valheim$food_data.tick();
            sol_valheim$trackData();
        }

        float maxhp = Math.min(40, (ModConfig.common.startingHealth * 2) + sol_valheim$food_data.getTotalFoodNutrition());

        EntityPlayer player = (EntityPlayer) (Object) this;
        player.getFoodStats().setFoodSaturationLevel(0);

        player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxhp);
        //if (getHealth() > maxhp)
        //    setHealth(maxhp);

        if (ModConfig.common.speedBoost > 0.01f) {
            var attr = player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
            var speedBuff = attr.getModifier(SOLValheim.getSpeedBuffModifier().getID());
            if (maxhp >= 20 && speedBuff == null)
                attr.applyModifier(SOLValheim.getSpeedBuffModifier()); //TODO
            else if (maxhp < 20 && speedBuff != null)
                attr.removeModifier(SOLValheim.getSpeedBuffModifier());
        }

        var timeSinceHurt = getEntityWorld().getWorldTime() - ((EntityLivingBaseDamageAccessor) this).solv$getLastDamageStamp();
        if (timeSinceHurt > ModConfig.common.regenDelay && player.ticksExisted % (5 * ModConfig.common.regenSpeedModifier) == 0) {
            player.heal(sol_valheim$food_data.getRegenSpeed() / 20f);
        }
    }

    @Inject(method = "canEat(Z)Z", at = @At("HEAD"), cancellable = true)
    private void onCanConsume(boolean ignoreHunger, CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(true);
        info.cancel();
    }

    @Inject(method = "damageEntity(Lnet/minecraft/util/DamageSource;F)V", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfo info) {
        if (source == DamageSource.STARVE) {
            info.cancel();
        }
    }

    @Inject(method = "writeEntityToNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("TAIL"))
    private void onWriteCustomData(NBTTagCompound nbt, CallbackInfo info) {
        nbt.setTag("sol_food_data", sol_valheim$food_data.save(new NBTTagCompound()));
    }

    @Inject(method = "readEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("TAIL"))
    private void onReadCustomData(NBTTagCompound nbt, CallbackInfo info) {
        if (sol_valheim$food_data == null)
            sol_valheim$food_data = new ValheimFoodData();

        var foodData = ValheimFoodData.read(nbt.getCompoundTag("sol_food_data"));
        sol_valheim$food_data.MaxItemSlots = foodData.MaxItemSlots;
        sol_valheim$food_data.DrinkSlot = foodData.DrinkSlot;
        sol_valheim$food_data.ItemEntries = foodData.ItemEntries.stream()
                .map(ValheimFoodData.EatenFoodItem::new)
                .collect(Collectors.toCollection(ArrayList::new));

        sol_valheim$trackData();
    }

    @Unique
    private void sol_valheim$trackData() {
        this.dataManager.set(sol_valheim$DATA_ACCESSOR, sol_valheim$food_data);
    }

    @Inject(method = "entityInit()V", at = {@At("TAIL")})
    private void onInitDataTracker(CallbackInfo info) {
        if (sol_valheim$food_data == null)
            sol_valheim$food_data = new ValheimFoodData();

        this.dataManager.register(sol_valheim$DATA_ACCESSOR, sol_valheim$food_data);
    }
}