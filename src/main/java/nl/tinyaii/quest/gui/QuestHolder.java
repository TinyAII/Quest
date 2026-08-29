package nl.tinyaii.quest.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * 任务面板 GUI 标识。
 */
public class QuestHolder implements InventoryHolder {
    private final String section;   // daily / chain / active
    private final int page;
    private final int pages;

    public QuestHolder(String section, int page, int pages) {
        this.section = section;
        this.page = page;
        this.pages = pages;
    }

    @Override
    public Inventory getInventory() { return null; }

    public String getSection() { return section; }
    public int getPage() { return page; }
    public int getPages() { return pages; }
}