package info.faceland.loot.data;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;

public class TradeMerchant {

  private final List<Trade> trades = new ArrayList<>();

  public void addTrade(Trade trade) {
    trades.add(trade);
  }

  public void open(Player player, String displayName) {
    Merchant merchant = Bukkit.createMerchant(displayName);
    List<MerchantRecipe> recipes = new ArrayList<>();
    for (Trade t : trades) {
      if (t.getReward() == null || (t.getCost1() == null && t.getCost2() == null)) {
        continue;
      }
      if (StringUtils.isNotBlank(t.getPermission()) && !player.hasPermission(t.getPermission())) {
        continue;
      }
      MerchantRecipe recipe1 = new MerchantRecipe(t.getReward(), 9999);
      if (t.getCost1() != null) {
        recipe1.addIngredient(t.getCost1());
      }
      if (t.getCost2() != null) {
        recipe1.addIngredient(t.getCost2());
      }
      recipe1.setExperienceReward(false);
      recipe1.setIgnoreDiscounts(true);
      recipe1.setPriceMultiplier(1);
      recipes.add(recipe1);
    }
    merchant.setRecipes(recipes);
    player.openMerchant(merchant, true);
  }

}
