package info.ata4.minecraft.dragon.client.handler;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Joseph on 10/30/2017.
 */
public interface IModelRegister {
	@SideOnly(Side.CLIENT)
	void registerModel();
}