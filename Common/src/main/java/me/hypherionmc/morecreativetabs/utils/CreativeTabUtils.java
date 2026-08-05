package me.hypherionmc.morecreativetabs.utils;

<<<<<<< HEAD
=======
<<<<<<< HEAD
=======
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.hypherionmc.morecreativetabs.ModConstants;
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
import me.hypherionmc.morecreativetabs.client.data.CustomCreativeTabJsonHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

<<<<<<< HEAD
public class CreativeTabUtils {
=======
<<<<<<< HEAD
public class CreativeTabUtils {
=======
public final class CreativeTabUtils {

    private CreativeTabUtils() {
    }
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)

    public static Supplier<ItemStack> makeTabIcon(CustomCreativeTabJsonHelper json) {
        CustomCreativeTabJsonHelper.TabIcon tabIcon = json.getTabIcon() != null ? json.getTabIcon() : new CustomCreativeTabJsonHelper.TabIcon();
        ItemStack stack = getItemStack(tabIcon.getName());

        if (!stack.isEmpty() && tabIcon.getNbt() != null) {
            try {
                CompoundTag tag = TagParser.parseTag(tabIcon.getNbt());
                stack.setTag(tag);
<<<<<<< HEAD
            } catch (Exception ignored) {
=======
<<<<<<< HEAD
            } catch (Exception ignored) {
=======
            } catch (CommandSyntaxException e) {
                ModConstants.logger.warn("Ignoring invalid NBT for creative tab icon {}", tabIcon.getName(), e);
>>>>>>> d16b230 (initial commit)
>>>>>>> 79b2a8a (initial commit)
            }
        }

        return () -> stack;
    }

    public static ItemStack getItemStack(String jsonItem) {
        if (jsonItem == null || jsonItem.isBlank())
            return ItemStack.EMPTY;

        ResourceLocation itemId = ResourceLocation.tryParse(jsonItem);
        if (itemId == null)
            return ItemStack.EMPTY;

        Optional<Item> itemOptional = BuiltInRegistries.ITEM.getOptional(itemId);
        return itemOptional.map(Item::getDefaultInstance).orElse(ItemStack.EMPTY);
    }

    public static String prefix(String tabName) {
        return String.format("%s.%s", "morecreativetabs", tabName);
    }

    public static String getTabKey(Component component) {
        if (component.getContents() instanceof TranslatableContents contents) {
            return contents.getKey();
        }
        return component.getString();
    }

    public static String fileToTab(String input) {
        String prefix = "morecreativetabs/";
        String suffix = ".json";
        if (input.startsWith(prefix))
            input = input.substring(prefix.length());
        if (input.endsWith(suffix))
            input = input.substring(0, input.length() - suffix.length());

        return input;
    }
}
