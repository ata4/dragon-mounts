package info.ata4.minecraft.dragon.client.forgeobjmodelported.techne;

import info.ata4.minecraft.dragon.client.forgeobjmodelported.IModelCustom;
import info.ata4.minecraft.dragon.client.forgeobjmodelported.IModelCustomLoader;
import info.ata4.minecraft.dragon.client.forgeobjmodelported.ModelFormatException;
import net.minecraft.util.ResourceLocation;

public class TechneModelLoader implements IModelCustomLoader {

  @Override
  public String getType()
  {
    return "Techne model";
  }

  private static final String[] types = { "tcn" };
  @Override
  public String[] getSuffixes()
  {
    return types;
  }

  @Override
  public IModelCustom loadInstance(ResourceLocation resource) throws ModelFormatException
  {
    return new TechneModel(resource);
  }

}