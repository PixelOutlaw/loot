package info.faceland.loot.creatures;

import info.faceland.loot.api.creatures.CreatureMod;
import info.faceland.loot.api.items.CustomItem;
import info.faceland.loot.api.sockets.SocketGem;
import info.faceland.loot.api.tier.Tier;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public final class LootCreatureMod implements CreatureMod {

    private final EntityType entityType;
    private Map<CustomItem, Double> customItemDoubleMap;
    private Map<SocketGem, Double> socketGemDoubleMap;
    private Map<Tier, Double> tierDoubleMap;

    public LootCreatureMod(EntityType entityType) {
        this.entityType = entityType;
        this.customItemDoubleMap = new HashMap<>();
        this.socketGemDoubleMap = new HashMap<>();
        this.tierDoubleMap = new HashMap<>();
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public Map<CustomItem, Double> getCustomItemMults() {
        return new HashMap<>(customItemDoubleMap);
    }

    @Override
    public Map<SocketGem, Double> getSocketGemMults() {
        return new HashMap<>(socketGemDoubleMap);
    }

    @Override
    public Map<Tier, Double> getTierMults() {
        return new HashMap<>(tierDoubleMap);
    }

    @Override
    public double getCustomItemMult(CustomItem ci) {
        if (getCustomItemMults().containsKey(ci)) {
            return getCustomItemMults().get(ci);
        }
        return 0;
    }

    @Override
    public double getSocketGemMult(SocketGem sg) {
        if (getSocketGemMults().containsKey(sg)) {
            return getSocketGemMults().get(sg);
        }
        return 0;
    }

    @Override
    public double getTierMult(Tier t) {
        if (getTierMults().containsKey(t)) {
            return getTierMults().get(t);
        }
        return 0;
    }

}
