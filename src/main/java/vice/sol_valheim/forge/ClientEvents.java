package vice.sol_valheim.forge;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vice.sol_valheim.SOLValheim;

import static vice.sol_valheim.FoodHUD.renderHud;


@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = SOLValheim.MOD_ID, value = Side.CLIENT)
public class ClientEvents {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        SOLValheim.addTooltip(event.getItemStack(), event.getFlags(), event.getToolTip());
    }

    @SubscribeEvent
    public static void onRenderGUI(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD) {
            event.setCanceled(true);
            renderHud();
        }

    }
}
