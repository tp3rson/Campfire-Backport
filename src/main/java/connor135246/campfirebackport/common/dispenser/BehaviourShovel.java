package connor135246.campfirebackport.common.dispenser;

import java.util.Random;

import connor135246.campfirebackport.common.blocks.BlockCampfire;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BehaviourShovel extends BehaviorDefaultDispenseItem
{
    @Override
    protected ItemStack dispenseStack(IBlockSource sourceblock, ItemStack stack)
    {
        EnumFacing enumfacing = BlockDispenser.func_149937_b(sourceblock.getBlockMetadata());
        World world = sourceblock.getWorld();
        int i = sourceblock.getXInt() + enumfacing.getFrontOffsetX();
        int j = sourceblock.getYInt() + enumfacing.getFrontOffsetY();
        int k = sourceblock.getZInt() + enumfacing.getFrontOffsetZ();

        Block block = world.getBlock(i, j, k);

        if (block instanceof BlockCampfire)
        {
            BlockCampfire cblock = (BlockCampfire) block;
            if (cblock.isLit())
            {
                BlockCampfire.updateCampfireBlockState(false, world, i, j, k, cblock.getType());
                if (stack.attemptDamageItem(1, new Random()))
                    stack.stackSize = 0;
            }
            return stack;
        }
        return super.dispenseStack(sourceblock, stack);
    }

}
