package me.bristermitten.fluency.implementation;

import me.bristermitten.fluency.Fluency;
import me.bristermitten.fluency.Util;
import me.bristermitten.fluency.button.ButtonBuilder;
import me.bristermitten.fluency.button.MenuButton;
import me.bristermitten.fluency.button.click.HandlerBuilder;
import me.bristermitten.fluency.data.ButtonHolder;
import me.bristermitten.fluency.menu.MenuBuilder;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

class ButtonBuilderImpl implements ButtonBuilder {
	private final MenuBuilder parent;
	private final ButtonHolder button;
	private HandlerBuilder handlerBuilder;
	private List<Consumer<ItemMeta>> modifierQueue;

	public ButtonBuilderImpl(Fluency fluency, MenuBuilder parent) {
		this.parent = parent;
		this.modifierQueue = new ArrayList<>();
		this.handlerBuilder = fluency.buildHandler(this);

		MenuButton button = new MenuButton();
		button.handler(handlerBuilder.build());
		this.button = new ButtonHolder(button);
		amount(1);
	}

	@NotNull
	@Override
	public ButtonBuilder amount(int amount) {
		transformItem(b -> b.setAmount(amount));
		return this;
	}

	@Override
	public int amount() {
		return button.get().getAmount();
	}

	@NotNull
	@Override
	public ButtonBuilder type(Material type) {
		transformItem(b -> b.setType(type));
		return this;
	}

	@Nonnull
	@Override
	public Material type() {
		return button.get().getType();
	}

	@NotNull
	@Override
	public ButtonBuilder data(short data) {
		transformItem(b -> b.setDurability(data));
		return this;
	}

	@Override
	public short data() {
		return button.get().getDurability();
	}

	@NotNull
	@Override
	public ButtonBuilder name(String name) {
		Objects.requireNonNull(name);
		transformMeta(m -> m.setDisplayName(Util.color(name)));
		return this;
	}

	@Nullable
	@Override
	public String name() {
		ItemMeta itemMeta = button.get().getItemMeta();
		if (itemMeta == null) return null;
		return itemMeta.getDisplayName();
	}

	@NotNull
	@Override
	public ButtonBuilder lore(@NotNull String... lore) {
		for (String s : lore) {
			addLore(s);
		}
		return this;
	}

	@NotNull
	@Override
	public ButtonBuilder lore(@NotNull List<String> lore) {
		for (String s : lore) {
			addLore(s);
		}
		return this;
	}

	@NotNull
	@Override
	public ButtonBuilder addLore(@NotNull String lore) {
		transformMeta(m -> {
			List<String> l = m.getLore();
			if (l == null) l = new ArrayList<>();
			l.add(Util.color(lore));
			m.setLore(l);
		});
		return this;
	}

	@Override
	@NotNull
	public List<String> lore() {
		ItemMeta itemMeta = button.get().getItemMeta();
		if (itemMeta == null) return Collections.emptyList();
		return itemMeta.getLore();
	}

	@NotNull
	@Override
	public ButtonBuilder unbreakable() {
		transformMeta(m -> m.spigot().setUnbreakable(true));
		return this;
	}

	@Override
	public @NotNull ButtonBuilder breakable() {
		transformMeta(m -> m.spigot().setUnbreakable(false));
		return this;
	}

	@Override
	public boolean isUnbreakable() {
		ItemMeta itemMeta = button.get().getItemMeta();
		if (itemMeta == null) return false;
		return itemMeta.spigot().isUnbreakable();
	}

	@NotNull
	@Override
	public ButtonBuilder enchant(Enchantment e, int level) {
		transformItem(b -> b.addEnchantment(e, level));
		return this;
	}

	@Override
	public @NotNull Map<Enchantment, Integer> enchantments() {
		return button.get().getEnchantments();
	}

	@NotNull
	@Override
	public HandlerBuilder onClick() {
//        HandlerBuilder handlerBuilder = fluency.buildHandler(this);
//        transform(b -> {
//            ClickHandler build = handlerBuilder.build();
//            if (b.handler() instanceof ActionList && build instanceof ActionList) {
//                ((ActionList) b.handler()).addAll((ActionList) build);
//            } else
//                b.handler(build);
//        });
		return handlerBuilder;
	}

	@NotNull
	@Override
	public HandlerBuilder onClick(ClickType type) {
//        HandlerBuilder handlerBuilder = fluency.buildHandler(this);
		handlerBuilder.whenClickType(type);
//        transform(b -> {
//            ClickHandler build = handlerBuilder.build();
//            if (b.handler() instanceof ActionList && build instanceof ActionList) {
//                ((ActionList) b.handler()).addAll((ActionList) build);
//            } else
//                b.handler(build);
//        });
		return handlerBuilder;
	}


	@NotNull
	@Override
	public MenuButton build() {
		return button.get();
	}

	@NotNull
	@Override
	public ButtonHolder getHolder() {
		return button;
	}

	@Override
	public MenuBuilder done() {
		return parent;
	}

	private void transformItem(Consumer<MenuButton> transformer) {
		MenuButton menuButton = button.get();
		transformer.accept(menuButton);
		button.set(menuButton);
	}

	private void transformMeta(Consumer<ItemMeta> f) {
		transformItem(b -> {
			ItemMeta itemMeta = b.getItemMeta();

			if (itemMeta == null) {
				modifierQueue.add(f);
				return;
			}

			modifierQueue.forEach(q -> q.accept(itemMeta));
			modifierQueue.clear();
			f.accept(itemMeta);
			b.setItemMeta(itemMeta);
		});
	}
}
