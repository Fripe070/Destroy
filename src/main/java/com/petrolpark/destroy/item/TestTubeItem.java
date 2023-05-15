package com.petrolpark.destroy.item;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.petrolpark.destroy.chemistry.ReadOnlyMixture;
import com.petrolpark.destroy.config.DestroyAllConfigs;
import com.petrolpark.destroy.fluid.MixtureFluid;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.ItemFluidContainer;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class TestTubeItem extends ItemFluidContainer {

    public static final int CAPACITY = 200;
    private static final DecimalFormat df = new DecimalFormat();

    static {
        df.setMinimumFractionDigits(1);
        df.setMinimumFractionDigits(1);
    };

    public TestTubeItem(Properties properties) {
        super(properties, CAPACITY);
    };

    public static ItemStack of(FluidStack fluidStack) {
        ItemStack stack = DestroyItems.TEST_TUBE.asStack(1);
        setContents(stack, fluidStack);
        return stack;
    };

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltip, isAdvanced);
        if (!DestroyItems.TEST_TUBE.isIn(stack)) return;
        getContents(stack).ifPresent(fluidStack -> {

            if (fluidStack.isEmpty()) return;

            MutableComponent name;
            String temperature = "";
        
            CompoundTag mixtureTag = fluidStack.getOrCreateTag().getCompound("Mixture");
            if (mixtureTag.isEmpty()) { // If this is not a Mixture
                name = fluidStack.getDisplayName().copy();
            } else { // If this is a Mixture
                ReadOnlyMixture mixture = ReadOnlyMixture.readNBT(mixtureTag);

                boolean iupac = DestroyAllConfigs.CLIENT.chemistry.iupacNames.get();
                name = mixture.getName().copy();
                temperature = df.format(mixture.getTemperature());
                tooltip.addAll(mixture.getContentsTooltip(iupac).stream().map(c -> c.copy()).toList());
            };

            tooltip.add(1, name.withStyle(ChatFormatting.GRAY).append(" "+fluidStack.getAmount()).append(Lang.translateDirect("generic.unit.millibuckets")).append(" "+temperature+"K"));
        });
    };

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            return getContents(stack).map(MixtureFluid::getTintColor).orElse(0);
        } else {
            return -1;
        }
    };

    public static Optional<FluidStack> getContents(ItemStack itemStack) {
        return itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(tanks -> tanks.drain(CAPACITY, FluidAction.SIMULATE));
    };

    public static void setContents(ItemStack itemStack, FluidStack fluidStack) {
        itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(tanks -> tanks.fill(fluidStack, FluidAction.EXECUTE));
    };
};