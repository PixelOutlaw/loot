package info.faceland.loot.data.pdc;

import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public final class GenericStatsType implements PersistentDataType<String, GenericItemStats> {

  private static final Gson gson = new Gson();

  @Override
  public Class<String> getPrimitiveType() {
    return String.class;
  }

  @Override
  public Class<GenericItemStats> getComplexType() {
    return GenericItemStats.class;
  }

  @Override
  public String toPrimitive(GenericItemStats complex, PersistentDataAdapterContext context) {
    return gson.toJson(complex);
  }

  @Override
  public GenericItemStats fromPrimitive(String primitive, PersistentDataAdapterContext context) {
    return gson.fromJson(primitive, GenericItemStats.class);
  }

}