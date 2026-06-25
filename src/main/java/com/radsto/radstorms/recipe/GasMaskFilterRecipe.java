package com.radsto.radstorms.recipe;

import com.radsto.radstorms.RadStormsMod;
import com.radsto.radstorms.items.ModArmorMaterials;
import com.radsto.radstorms.items.ModItems;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class GasMaskFilterRecipe extends CustomRecipe {
    public static final RecipeSerializer<GasMaskFilterRecipe> SERIALIZER =
            new SimpleCraftingRecipeSerializer<>(GasMaskFilterRecipe::new);

    public GasMaskFilterRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }

    @Override
    public boolean matches(CraftingContainer pContainer, Level pLevel) {
        ItemStack mask = ItemStack.EMPTY;
        ItemStack filter = ItemStack.EMPTY;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.getItem() instanceof ArmorItem armor && armor.getMaterial() == ModArmorMaterials.GAS_MASK) {
                    if (!mask.isEmpty()) return false;
                    mask = stack;
                } else if (stack.getItem() == ModItems.GAS_MASK_FILTER.get()) {
                    if (!filter.isEmpty()) return false;
                    filter = stack;
                } else {
                    return false;
                }
            }
        }
        return !mask.isEmpty() && !filter.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer pContainer, RegistryAccess pRegistryAccess) {
        ItemStack resultMask =  ItemStack.EMPTY;

        for (int i = 0; i < pContainer.getContainerSize(); i++) {
            ItemStack stack = pContainer.getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                resultMask = stack.copy();
            }
        }

        if (!resultMask.isEmpty()) {
            resultMask.getOrCreateTag().putInt("FilterLeft", 1000);
        }

        return resultMask;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return pWidth * pHeight >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.GAS_FILTER_SERIALIZER.get();
    }
}
