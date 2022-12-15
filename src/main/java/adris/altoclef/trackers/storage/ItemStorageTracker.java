package adris.altoclef.trackers.storage;

import adris.altoclef.AltoClef;
import adris.altoclef.trackers.Tracker;
import adris.altoclef.trackers.TrackerManager;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.RecipeTarget;
import adris.altoclef.util.helpers.BaritoneHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.slots.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Access ALL forms of storage.
 */
public class ItemStorageTracker extends Tracker {

    private final InventorySubTracker _inventory;
    private final ContainerSubTracker _containers;

    public ItemStorageTracker(AltoClef mod, TrackerManager manager, Consumer<ContainerSubTracker> containerTrackerConsumer) {
        super(manager);
        _inventory = new InventorySubTracker(manager);
        _containers = new ContainerSubTracker(manager);
        containerTrackerConsumer.accept(_containers);
    }

    private static Slot[] getCurrentConversionSlots() {
        // TODO: Anvil input, anything else...
        if (StorageHelper.isPlayerInventoryOpen()) {
            return PlayerSlot.CRAFT_INPUT_SLOTS;
        } else if (StorageHelper.isBigCraftingOpen()) {
            return CraftingTableSlot.INPUT_SLOTS;
        } else if (StorageHelper.isFurnaceOpen()) {
            return new Slot[]{FurnaceSlot.INPUT_SLOT_FUEL, FurnaceSlot.INPUT_SLOT_MATERIALS};
        } else if (StorageHelper.isSmokerOpen()) {
            return new Slot[]{SmokerSlot.INPUT_SLOT_FUEL, SmokerSlot.INPUT_SLOT_MATERIALS};
        } else if (StorageHelper.isBlastFurnaceOpen()) {
            return new Slot[]{BlastFurnaceSlot.INPUT_SLOT_FUEL, BlastFurnaceSlot.INPUT_SLOT_MATERIALS};
        }
        return new Slot[0];
    }

    /**
     * Gets the number of items in the player's inventory OR if the player is USING IT in a conversion process
     * (ex. crafting table slots/furnace input, stuff the player is use )
     */
    public int getItemCount(Item... items) {
        int inConversionSlots = Arrays.stream(getCurrentConversionSlots()).mapToInt(slot -> {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            if (ArrayUtils.contains(items, stack.getItem())) {
                return stack.getCount();
            }
            return 0;
        }).reduce(0, Integer::sum);
        return _inventory.getItemCount(true, false, items) + inConversionSlots;
    }

    public int getItemCount(ItemTarget... targets) {
        return Arrays.stream(targets).mapToInt(target -> getItemCount(target.getMatches())).reduce(0, Integer::sum);
    }

    /**
     * Gets the number of items visible on the screen in any slot
     */
    public int getItemCountScreen(Item... items) {
        return _inventory.getItemCount(true, true, items);
    }

    /**
     * Gets the number of items STRICTLY in the player's inventory.
     * <p>
     * ONLY USE THIS when getting an item is the END GOAL. This will
     * NOT count items in a crafting/furnace slot!
     */
    public int getItemCountInventoryOnly(Item... items) {
        return _inventory.getItemCount(true, false, items);
    }

    /**
     * Gets the number of items only in the currently open container, NOT the player's inventory.
     */
    public int getItemCountContainer(Item... items) {
        return _inventory.getItemCount(false, true, items);
    }

    /**
     * Gets whether an item is in the player's inventory OR if the player is USING IT in a conversion process
     * (ex. crafting table slots/furnace input, stuff the player is use )
     */
    public boolean hasItem(Item... items) {
        return Arrays.stream(getCurrentConversionSlots()).anyMatch(slot -> {
            ItemStack stack = StorageHelper.getItemStackInSlot(slot);
            return ArrayUtils.contains(items, stack.getItem());
        }) || _inventory.hasItem(true, items);
    }

    public boolean hasItemInOffhand(Item item) {
        ItemStack offhand = StorageHelper.getItemStackInSlot(PlayerSlot.OFFHAND_SLOT);
        return offhand.getItem() == item;
    }

    public boolean hasItemAll(Item... items) {
        return Arrays.stream(items).allMatch(this::hasItem);
    }

    public boolean hasItem(ItemTarget... targets) {
        return Arrays.stream(targets).anyMatch(target -> hasItem(target.getMatches()));
    }

    /**
     * Returns whether an item is visible on the screen in any slot
     */
    public boolean hasItemScreen(Item... items) {
        return _inventory.hasItem(false, items);
    }

    /**
     * Returns whether the player has an item in its inventory ONLY.
     * <p>
     * ONLY USE THIS when getting an item is the END GOAL. This will
     * NOT count items in a crafting/furnace slot!
     */
    public boolean hasItemInventoryOnly(Item... items) {
        return _inventory.hasItem(true, items);
    }

    /**
     * Returns all slots containing any item given.
     */
    public List<Slot> getSlotsWithItemScreen(Item... items) {
        return _inventory.getSlotsWithItems(true, true, items);
    }

    /**
     * Returns all slots NOT in the player inventory containing any item given.
     */
    public List<Slot> getSlotsWithItemContainer(Item... items) {
        return _inventory.getSlotsWithItems(false, true, items);
    }

    /**
     * Returns all slots in our player inventory containing any item given.
     */
    public List<Slot> getSlotsWithItemPlayerInventory(boolean includeCraftArmorOffhand, Item... items) {
        List<Slot> result = _inventory.getSlotsWithItems(true, false, items);
        // Check other slots
        if (includeCraftArmorOffhand) {
            HashSet<Item> toCheck = new HashSet<>(Arrays.asList(items));
            for (Slot otherSlot : StorageHelper.INACCESSIBLE_PLAYER_SLOTS) {
                if (toCheck.contains(StorageHelper.getItemStackInSlot(otherSlot).getItem())) {
                    result.add(otherSlot);
                }
            }
        }
        return result;
    }

    public List<ItemStack> getItemStacksPlayerInventory(boolean includeCursorSlot) {
        return _inventory.getInventoryStacks(includeCursorSlot);
    }

    /**
     * Get all slots in the player's inventory that can fit an item stack.
     *
     * @param stack         The stack to "fit"/place in the inventory.
     * @param acceptPartial If true, is OK with fitting PART of the stack. If false, requires 100% of the stack to fit.
     */
    public List<Slot> getSlotsThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
        return _inventory.getSlotsThatCanFit(true, false, stack, acceptPartial);
    }

    public Optional<Slot> getSlotThatCanFitInPlayerInventory(ItemStack stack, boolean acceptPartial) {
        List<Slot> slots = getSlotsThatCanFitInPlayerInventory(stack, acceptPartial);
        return Optional.ofNullable(slots.isEmpty() ? null : slots.get(0));
    }

    /**
     * Get all slots in the currently open container that can fit an item stack, EXCLUDING the player inventory.
     *
     * @param stack         The stack to "fit"/place in the inventory.
     * @param acceptPartial If true, is OK with fitting PART of the stack. If false, requires 100% of the stack to fit.
     */
    public List<Slot> getSlotsThatCanFitInOpenContainer(ItemStack stack, boolean acceptPartial) {
        return _inventory.getSlotsThatCanFit(false, true, stack, acceptPartial);
    }

    public Optional<Slot> getSlotThatCanFitInOpenContainer(ItemStack stack, boolean acceptPartial) {
        List<Slot> slots = getSlotsThatCanFitInOpenContainer(stack, acceptPartial);
        return Optional.ofNullable(slots.isEmpty() ? null : slots.get(0));
    }

    /**
     * Get all slots that can fit an item stack.
     *
     * @param stack         The stack to "fit"/place in the inventory.
     * @param acceptPartial If true, is OK with fitting PART of the stack. If false, requires 100% of the stack to fit.
     */
    public List<Slot> getSlotsThatCanFitScreen(ItemStack stack, boolean acceptPartial) {
        return _inventory.getSlotsThatCanFit(true, true, stack, acceptPartial);
    }

    public boolean hasEmptyInventorySlot() {
        return _inventory.hasEmptySlot(true);
    }

    public void registerSlotAction() {
        _inventory.setDirty();
    }

    /**
     * Returns whether an item is present in a container. You can filter out containers
     * you don't like.
     */
    public boolean hasItemContainer(Predicate<ContainerCache> accept, Item... items) {
        return _containers.hasItem(accept, items);
    }

    /**
     * Returns whether an item is present in ANY container, no matter how far.
     */
    public boolean hasItemContainer(Item... items) {
        return _containers.hasItem(items);
    }

    public Optional<ContainerCache> getContainerAtPosition(BlockPos pos) {
        return _containers.getContainerAtPosition(pos);
    }

    public boolean isContainerCached(BlockPos pos) {
        return getContainerAtPosition(pos).isPresent();
    }

    public Optional<ContainerCache> getEnderChestStorage() {
        return _containers.getEnderChestStorage();
    }

    public List<ContainerCache> getCachedContainers(Predicate<ContainerCache> accept) {
        return _containers.getCachedContainers(accept);
    }

    public List<ContainerCache> getCachedContainers(ContainerType... types) {
        return _containers.getCachedContainers(types);
    }

    public List<ContainerCache> getCachedContainers() {
        return getCachedContainers(cache -> true);
    }

    public Optional<ContainerCache> getContainerClosestTo(Vec3d pos, Predicate<ContainerCache> accept) {
        return _containers.getClosestTo(pos, accept);
    }

    public Optional<ContainerCache> getContainerClosestTo(Vec3d pos, ContainerType... types) {
        return _containers.getClosestTo(pos, types);
    }

    public Optional<ContainerCache> getContainerClosestTo(Vec3d pos) {
        return getContainerClosestTo(pos, cache -> true);
    }

    public List<ContainerCache> getContainersWithItem(Item... items) {
        return _containers.getContainersWithItem(items);
    }

    public Optional<ContainerCache> getClosestContainerWithItem(Vec3d pos, Item... items) {
        return _containers.getClosestWithItem(pos, items);
    }

    public Optional<BlockPos> getLastBlockPosInteraction() {
        return Optional.ofNullable(_containers.getLastBlockPosInteraction());
    }

    /////Meloweh's crafting helpers start/////
    public List<Item> getBlockTypes() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _inventory.getInventoryStacks(true)
                    .stream()
                    .filter(e -> e.getItem() instanceof BlockItem)
                    .map(e -> e.getItem())
                    .collect(Collectors.toList());
        }
    }

    public int getBlockCount() {
        ensureUpdated();
        synchronized (BaritoneHelper.MINECRAFT_LOCK) {
            return _inventory.getInventoryStacks(true)
                    .stream()
                    .filter(e -> e.getItem() instanceof BlockItem)
                    .mapToInt(e -> e.getCount())
                    .sum();
        }
    }

    /**
     * RecipesUtils supplies several methods for easing the process of removing existing recipes,
     * as well as a helper method for making a 9x9 grid of the same item (nuggets to ingots, etc)
     * https://github.com/Draco18s/ReasonableRealism/blob/1.12.1/src/main/java/com/draco18s/hardlib/util/RecipesUtils.java#L164
     *
     * @author Draco18s, Meloweh
     */
    @Nullable
    public static net.minecraft.recipe.CraftingRecipe getRecipeWithOutput(ItemStack resultStack) {
        ItemStack recipeResult;
        List<net.minecraft.recipe.CraftingRecipe> recipes = MinecraftClient.getInstance().world.getRecipeManager().listAllOfType(RecipeType.CRAFTING);
        Iterator<net.minecraft.recipe.CraftingRecipe> iterator = recipes.iterator();
        while(iterator.hasNext()) {
            net.minecraft.recipe.CraftingRecipe tmpRecipe = iterator.next();
            recipeResult = tmpRecipe.getOutput();
            if (ItemStack.areItemsEqual(resultStack, recipeResult)) {
                return tmpRecipe;
            }
        }
        return null;
    }

    public final int getItemCountOfSlot(final Slot slot) {
        if (slot == null) return -1;
        final ItemStack stack = StorageHelper.getItemStackInSlot(slot);
        if (stack == null) return -1;
        if (stack.getItem() == null) return 0;
        return getItemCount(stack.getItem());
    }

    //TODO: Needs reset if generic crafting process stopped
    //private Map<Long, DepthAttributes> depthAttributesMap = new HashMap<>();

    public ItemTarget getMissingItemTarget(final AltoClef mod, final CraftingRecipe _recipe) {
        final ItemStorageTracker inventory = mod.getItemStorage();
        boolean bigCrafting = (mod.getPlayer().currentScreenHandler instanceof CraftingScreenHandler);

        // For each slot in table
        for (int craftSlot = 0; craftSlot < _recipe.getSlotCount(); ++craftSlot) {
            ItemTarget toFill = _recipe.getSlot(craftSlot);
            Slot currentCraftSlot;
            if (bigCrafting) {
                // Craft in table
                currentCraftSlot = CraftingTableSlot.getInputSlot(craftSlot, _recipe.isBig());
            } else {
                // Craft in window
                currentCraftSlot = PlayerSlot.getCraftInputSlot(craftSlot);
            }
            ItemStack present = StorageHelper.getItemStackInSlot(currentCraftSlot);
            if (toFill == null || toFill.isEmpty()) {
                continue;
            } else {
                boolean isSatisfied = toFill.matches(present.getItem());

                if (!isSatisfied) {
                    int count = 0;
                    for (int i = 0; i < _recipe.getSlotCount(); i++) {
                        ItemTarget toFill2 = _recipe.getSlot(craftSlot);
                        if (toFill == null || toFill.isEmpty()) {
                            continue;
                        }

                        if (toFill2.getMatches() == null || toFill.getMatches() == null) continue;
                        for (Item item : toFill2.getMatches()) {
                            ItemTarget t1 = (new ItemTarget(item));

                            for (Item item2 : toFill.getMatches()) {
                                if (t1.matches(item2)) {
                                    count++;
                                }
                            }
                        }

                        count = (int)Math.ceil(count / _recipe.getSlotCount());
                        if (count > inventory.getItemCount(toFill) && inventory.getItemCount(toFill) > 0) {
                            return new ItemTarget(toFill, count);
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean hasIngredientInAnyDepth(final Ingredient ingredient, final AltoClef mod, final List<Slot> blacklist, final List<Slot> previouslyBlacklisted) {
        for (final ItemStack itemStack : ingredient.getMatchingStacks()) {
            if (itemStack == null) continue;
            if (itemStack.getItem() == null) continue;
            if (isSlotInAnyDepthSatisfiable(new ItemTarget(itemStack.getItem()/*, itemStack.getCount()*/), mod, blacklist, previouslyBlacklisted)) return true;
        }
        return false;
    }

    private int getItemCountOfSlotsInBlacklist(final List<Slot> blacklist, final AltoClef mod) {
        return blacklist.stream().mapToInt(e -> mod.getItemStorage().getItemCountOfSlot(e)).sum();
    }

    private boolean isSlotInList(final Slot slot, final List<Slot> list) {
        return list.stream().anyMatch(e -> e.getInventorySlot() == slot.getInventorySlot());
    }

    //we can limit blacklisting to one stack = 64 since crafting slots are limited to it anyway.
    private boolean blacklistSatisfyingSlots(final List<Slot> blacklist, final List<Slot> previouslyBlacklisted, final ItemTarget itemTarget, final AltoClef mod) {
        final List<Slot> slots = mod.getItemStorage().getSlotsWithItemPlayerInventory(false, itemTarget.getMatches());
        int size = 1;

        for (final Slot slot : slots) {
            if (size > getItemCountOfSlotsInBlacklist(blacklist, mod)) {
                if (!isSlotInList(slot, blacklist) && !isSlotInList(slot, previouslyBlacklisted)) {
                    blacklist.add(slot);
                }
            } else {
                return true;
            }
        }

        return false;
    }
    final List<ItemTarget> crossed = new ArrayList<>();

    private boolean isSlotInAnyDepthSatisfiable(final ItemTarget itemTarget, final AltoClef mod, final List<Slot> blacklist, final List<Slot> previouslyBlacklisted) {
        //coding style with side effect returns "is it satisfied"?
        if (blacklistSatisfyingSlots(blacklist, previouslyBlacklisted, itemTarget, mod)) {
            return true;
        }
        if (crossed.stream().anyMatch(e -> e.getMatches(itemTarget).length > 0)) {
            return false;
        }
        crossed.add(itemTarget);
        for (final Iterator<Item> iterator = Arrays.stream(itemTarget.getMatches()).iterator(); iterator.hasNext();) {
            final Item item = iterator.next();
            System.out.println("isSlotInAnyDepthSatisfiable item: " + item.toString());
            final Recipe recipe = getRecipeWithOutput(item.getDefaultStack());
            if (recipe == null) return false;

            for (final Iterator it = recipe.getIngredients().iterator(); it.hasNext(); ) {
                Ingredient ingredient = (Ingredient) it.next();
                if (hasIngredientInAnyDepth(ingredient, mod, blacklist, previouslyBlacklisted)) return true; //found = true;
            }

            if (!iterator.hasNext()) {
                crossed.add(itemTarget);
            }
        }

        for (final Item item : itemTarget.getMatches()) {
            if (crossed.stream().anyMatch(e -> e.matches(item))) {
                return false;
            }

            final Recipe recipe = getRecipeWithOutput(item.getDefaultStack());
            if (recipe == null) return false;

            for (Iterator it = recipe.getIngredients().iterator(); it.hasNext(); ) {
                Ingredient ingredient = (Ingredient) it.next();
                if (hasIngredientInAnyDepth(ingredient, mod, blacklist, previouslyBlacklisted)) return true; //found = true;
            }
        }
        return true;
    }

    public final boolean isFullyCapableToCraft(final AltoClef mod, final RecipeTarget recipeTarget) {
        final CraftingRecipe recipe = recipeTarget.getRecipe();
        if (recipe == null || mod == null) return false;
        final List<Slot> blacklist = new ArrayList<>();

        for (int i = 0; i < recipe.getSlotCount(); i++) {
            final ItemTarget itemTarget = recipe.getSlot(i);
            if (itemTarget == null) continue;
            if (itemTarget.getMatches() == null) continue;
            final List<Slot> subBlacklist = new ArrayList<>();
            crossed.clear();
            if (!isSlotInAnyDepthSatisfiable(itemTarget, mod, subBlacklist, blacklist)) return false;
            blacklist.addAll(subBlacklist);
        }
        return true;
    }
    public final boolean isFullyCapableToCraft(final AltoClef mod, final net.minecraft.recipe.CraftingRecipe recipe) {
        if (recipe == null) {
            System.out.println("no recipe");
            return false;
        }

        final List<Slot> blacklist = new ArrayList<>();
        for (final Ingredient ingredient : recipe.getIngredients()) {
            for (final ItemStack ingredientStack : ingredient.getMatchingStacks()) {
                System.out.println("ingredientStackItem: " + ingredientStack.getItem().toString());
                final ItemTarget itemTarget = new ItemTarget(ingredientStack.getItem());
                if (itemTarget == null) continue;
                if (itemTarget.getMatches() == null) continue;
                final List<Slot> subBlacklist = new ArrayList<>();
                crossed.clear();
                if (!isSlotInAnyDepthSatisfiable(itemTarget, mod, subBlacklist, blacklist)) return false;
                blacklist.addAll(subBlacklist);
            }
        }
        return true;
    }
    public final boolean isFullyCapableToCraft(final AltoClef mod, final ItemStack stack) {
        System.out.println("isFullyCapableToCraft:");
        if (stack == null) {
            System.out.println("no stack");
            return false;
        }
        final net.minecraft.recipe.CraftingRecipe recipe = getRecipeWithOutput(stack);
        if (recipe.getIngredients() == null) {
            System.out.println("no ingredients");
            return false;
        }

        return isFullyCapableToCraft(mod, recipe);
        /*for (final Ingredient ingredient : recipe.getIngredients()) {
            for (final ItemStack ingredientStack : ingredient.getMatchingStacks()) {
                System.out.println(ingredientStack.getItem().toString());
            }
        }*/
        /*
        final CraftingRecipe badRecipe = CraftingRecipe.new;
        //final ItemTarget target = new ItemTarget(stack.getItem());
        //final RecipeTarget recipeTarget = new RecipeTarget(stack.getItem(), stack.getCount(), recipe);
        return isFullyCapableToCraft(mod, recipeTarget);*/
        //return false;
    }

    public final boolean isFullyCapableToCraft(final AltoClef mod, final Item item) {
        return isFullyCapableToCraft(mod, new ItemStack(item));
    }
    /////Meloweh's crafting helpers end/////

    public Optional<Item> bestShovelInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_SHOVEL)) {
            return Optional.of(Items.NETHERITE_SHOVEL);
        }
        if (hasItem(Items.GOLDEN_SHOVEL)) {
            return Optional.of(Items.GOLDEN_SHOVEL);
        }
        if (hasItem(Items.DIAMOND_SHOVEL)) {
            return Optional.of(Items.DIAMOND_SHOVEL);
        }
        if (hasItem(Items.IRON_SHOVEL)) {
            return Optional.of(Items.IRON_SHOVEL);
        }
        if (hasItem(Items.STONE_SHOVEL)) {
            return Optional.of(Items.STONE_SHOVEL);
        }
        if (hasItem(Items.WOODEN_SHOVEL)) {
            return Optional.of(Items.WOODEN_SHOVEL);
        }
        return Optional.empty();
    }
    public Optional<Item> bestAxeInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_AXE)) {
            return Optional.of(Items.NETHERITE_AXE);
        }
        if (hasItem(Items.GOLDEN_AXE)) {
            return Optional.of(Items.GOLDEN_AXE);
        }
        if (hasItem(Items.DIAMOND_AXE)) {
            return Optional.of(Items.DIAMOND_AXE);
        }
        if (hasItem(Items.IRON_AXE)) {
            return Optional.of(Items.IRON_AXE);
        }
        if (hasItem(Items.STONE_AXE)) {
            return Optional.of(Items.STONE_AXE);
        }
        if (hasItem(Items.WOODEN_AXE)) {
            return Optional.of(Items.WOODEN_AXE);
        }
        return Optional.empty();
    }
    public Optional<Item> bestPickaxeInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_PICKAXE)) {
            return Optional.of(Items.NETHERITE_PICKAXE);
        }
        if (hasItem(Items.DIAMOND_PICKAXE)) {
            return Optional.of(Items.DIAMOND_PICKAXE);
        }
        if (hasItem(Items.GOLDEN_PICKAXE)) {
            return Optional.of(Items.GOLDEN_PICKAXE);
        }
        if (hasItem(Items.IRON_PICKAXE)) {
            return Optional.of(Items.IRON_PICKAXE);
        }
        if (hasItem(Items.STONE_PICKAXE)) {
            return Optional.of(Items.STONE_PICKAXE);
        }
        if (hasItem(Items.WOODEN_PICKAXE)) {
            return Optional.of(Items.WOODEN_PICKAXE);
        }
        return Optional.empty();
    }
    public Optional<Item> bestSwordInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_SWORD)) {
            return Optional.of(Items.NETHERITE_SWORD);
        }
        if (hasItem(Items.DIAMOND_SWORD)) {
            return Optional.of(Items.DIAMOND_SWORD);
        }
        if (hasItem(Items.IRON_SWORD)) {
            return Optional.of(Items.IRON_SWORD);
        }
        if (hasItem(Items.STONE_SWORD)) {
            return Optional.of(Items.STONE_SWORD);
        }
        if (hasItem(Items.GOLDEN_SWORD)) {
            return Optional.of(Items.GOLDEN_SWORD);
        }
        if (hasItem(Items.WOODEN_SWORD)) {
            return Optional.of(Items.WOODEN_SWORD);
        }
        return Optional.empty();
    }
    public Optional<Item> bestHelmetInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_HELMET)) {
            return Optional.of(Items.NETHERITE_HELMET);
        }
        if (hasItem(Items.DIAMOND_HELMET)) {
            return Optional.of(Items.DIAMOND_HELMET);
        }
        if (hasItem(Items.IRON_HELMET)) {
            return Optional.of(Items.IRON_HELMET);
        }
        if (hasItem(Items.GOLDEN_HELMET)) {
            return Optional.of(Items.GOLDEN_HELMET);
        }
        if (hasItem(Items.LEATHER_HELMET)) {
            return Optional.of(Items.LEATHER_HELMET);
        }
        return Optional.empty();
    }
    public Optional<Item> bestChestplateInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_CHESTPLATE)) {
            return Optional.of(Items.NETHERITE_CHESTPLATE);
        }
        if (hasItem(Items.DIAMOND_CHESTPLATE)) {
            return Optional.of(Items.DIAMOND_CHESTPLATE);
        }
        if (hasItem(Items.IRON_CHESTPLATE)) {
            return Optional.of(Items.IRON_CHESTPLATE);
        }
        if (hasItem(Items.GOLDEN_CHESTPLATE)) {
            return Optional.of(Items.GOLDEN_CHESTPLATE);
        }
        if (hasItem(Items.LEATHER_CHESTPLATE)) {
            return Optional.of(Items.LEATHER_CHESTPLATE);
        }
        return Optional.empty();
    }
    public Optional<Item> bestLeggingsInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_LEGGINGS)) {
            return Optional.of(Items.NETHERITE_LEGGINGS);
        }
        if (hasItem(Items.DIAMOND_LEGGINGS)) {
            return Optional.of(Items.DIAMOND_LEGGINGS);
        }
        if (hasItem(Items.IRON_LEGGINGS)) {
            return Optional.of(Items.IRON_LEGGINGS);
        }
        if (hasItem(Items.GOLDEN_LEGGINGS)) {
            return Optional.of(Items.GOLDEN_LEGGINGS);
        }
        if (hasItem(Items.LEATHER_LEGGINGS)) {
            return Optional.of(Items.LEATHER_LEGGINGS);
        }
        return Optional.empty();
    }
    public Optional<Item> bestBootsInInventory() {
        //TODO: maybe consider enchantments (It's not hard).
        if (hasItem(Items.NETHERITE_BOOTS)) {
            return Optional.of(Items.NETHERITE_BOOTS);
        }
        if (hasItem(Items.DIAMOND_BOOTS)) {
            return Optional.of(Items.DIAMOND_BOOTS);
        }
        if (hasItem(Items.IRON_BOOTS)) {
            return Optional.of(Items.IRON_BOOTS);
        }
        if (hasItem(Items.GOLDEN_BOOTS)) {
            return Optional.of(Items.GOLDEN_BOOTS);
        }
        if (hasItem(Items.LEATHER_BOOTS)) {
            return Optional.of(Items.LEATHER_BOOTS);
        }
        return Optional.empty();
    }

    @Override
    protected void updateState() {
        _inventory.updateState();
        _containers.updateState();
    }

    @Override
    protected void reset() {
        _inventory.reset();
        _containers.reset();
    }
}

