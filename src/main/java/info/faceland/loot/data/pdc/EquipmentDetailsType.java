package info.faceland.loot.data.pdc;

import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

public final class EquipmentDetailsType implements PersistentDataType<String, EquipmentData> {

  private static final Gson gson = new Gson();

  @Override
  public Class<String> getPrimitiveType() {
    return String.class;
  }

  @Override
  public Class<EquipmentData> getComplexType() {
    return EquipmentData.class;
  }

  @Override
  public String toPrimitive(EquipmentData complex, PersistentDataAdapterContext context) {
    return gson.toJson(complex);
  }

  @Override
  public EquipmentData fromPrimitive(String primitive, PersistentDataAdapterContext context) {
    return gson.fromJson(primitive, EquipmentData.class);
  }

}