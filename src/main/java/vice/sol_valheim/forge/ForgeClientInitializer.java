package vice.sol_valheim.forge;


import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vice.sol_valheim.SOLValheim;
import vice.sol_valheim.SOLValheimClient;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = SOLValheim.MOD_ID)
public class ForgeClientInitializer {

    @Mod.EventHandler
    public static void clientSetup(FMLInitializationEvent event) {
        SOLValheimClient.init();
    }
}