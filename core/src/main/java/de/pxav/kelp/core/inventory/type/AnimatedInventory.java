package de.pxav.kelp.core.inventory.type;

import com.google.common.collect.Lists;
import de.pxav.kelp.core.animation.TextAnimation;
import de.pxav.kelp.core.animation.TextAnimationFactory;
import de.pxav.kelp.core.inventory.item.KelpItem;
import de.pxav.kelp.core.inventory.version.InventoryVersionTemplate;
import de.pxav.kelp.core.inventory.widget.SimpleWidget;
import de.pxav.kelp.core.inventory.version.WindowPacketTemplate;
import de.pxav.kelp.core.sidebar.type.AnimatedSidebar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class description goes here.
 *
 * @author pxav
 */
public class AnimatedInventory extends KelpInventory {

  private TextAnimation title;
  private int size;
  private List<SimpleWidget> simpleWidgets;

  private ScheduledExecutorService scheduledExecutorService;
  private int animationState = 0;
  private long animationDelayInMillis = 500;

  private WindowPacketTemplate windowPacketTemplate;
  private InventoryVersionTemplate inventoryVersionTemplate;
  private TextAnimationFactory textAnimationFactory;

  public AnimatedInventory(WindowPacketTemplate windowPacketTemplate,
                           InventoryVersionTemplate inventoryVersionTemplate,
                           TextAnimationFactory textAnimationFactory) {
    this.windowPacketTemplate = windowPacketTemplate;
    this.inventoryVersionTemplate = inventoryVersionTemplate;
    this.textAnimationFactory = textAnimationFactory;
    this.simpleWidgets = Lists.newArrayList();
    this.size = 54;
  }

  public AnimatedInventory size(int size) {
    this.size = size;
    return this;
  }

  public AnimatedInventory title(TextAnimation textAnimation) {
    this.title = textAnimation;
    return this;
  }

  public AnimatedInventory addWidget(SimpleWidget widget) {
    this.simpleWidgets.add(widget);
    return this;
  }

  public void updateTitleOnly(Player player, int state) {
    this.windowPacketTemplate.updateWindowTitle(player, title.states().get(state));
  }

  @Override
  public Inventory render() {
    if (this.title == null) {
      this.title = textAnimationFactory.newStaticTextAnimation("§8Inventory");
    }

    Inventory inventory = inventoryVersionTemplate.createInventory(this.size, title.states().get(0));

    for (SimpleWidget current : simpleWidgets) {
      KelpItem item = current.render();
      inventory.setItem(item.getSlot(), item.getItemStack());
    }

    return inventory;
  }

  @Override
  public void update(Inventory toUpdate, int state) {
    toUpdate.clear();

    for (SimpleWidget current : simpleWidgets) {
      KelpItem item = current.render();
      toUpdate.setItem(item.getSlot(), item.getItemStack());
    }
  }

  public void scheduleUpdater(Player playerFor) {
    scheduledExecutorService = Executors.newScheduledThreadPool(0);
    scheduledExecutorService.scheduleAtFixedRate(() -> {
      try {
        updateTitleOnly(playerFor, animationState);
        animationState++;
        if (animationState == title.states().size()) {
          animationState = 0;
        }
      } catch (Exception ignore) {}
    }, 0, animationDelayInMillis, TimeUnit.MILLISECONDS);
  }

  public void stopUpdater() {
    scheduledExecutorService.shutdown();
  }

}
