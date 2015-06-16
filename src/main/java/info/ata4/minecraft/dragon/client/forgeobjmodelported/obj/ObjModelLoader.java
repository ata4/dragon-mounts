package info.ata4.minecraft.dragon.client.forgeobjmodelported.obj;

import info.ata4.minecraft.dragon.client.forgeobjmodelported.IModelCustom;
import info.ata4.minecraft.dragon.client.forgeobjmodelported.IModelCustomLoader;
import info.ata4.minecraft.dragon.client.forgeobjmodelported.ModelFormatException;
import net.minecraft.util.ResourceLocation;

public class ObjModelLoader implements IModelCustomLoader
{

  @Override
  public String getType()
  {
    return "OBJ model";
  }

  private static final String[] types = { "obj" };
  @Override
  public String[] getSuffixes()
  {
    return types;
  }

  @Override
  public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException
  {
    return new WavefrontObject(resource);
  }
}