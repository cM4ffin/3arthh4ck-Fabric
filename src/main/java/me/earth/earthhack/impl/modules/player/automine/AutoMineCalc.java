package me.earth.earthhack.impl.modules.player.automine;

import me.earth.earthhack.api.util.interfaces.Globals;
import me.earth.earthhack.impl.modules.player.automine.util.BigConstellation;
import me.earth.earthhack.impl.modules.player.automine.util.IAutomine;
import me.earth.earthhack.impl.modules.player.automine.util.IConstellation;
import me.earth.earthhack.impl.util.math.BBUtil;
import me.earth.earthhack.impl.util.math.RayTraceUtil;
import me.earth.earthhack.impl.util.math.position.PositionUtil;
import me.earth.earthhack.impl.util.math.rotation.RotationUtil;
import me.earth.earthhack.impl.util.minecraft.DamageUtil;
import me.earth.earthhack.impl.util.minecraft.blocks.mine.MineUtil;
import me.earth.earthhack.impl.util.minecraft.blocks.states.BlockStateHelper;
import me.earth.earthhack.impl.util.minecraft.blocks.states.IBlockStateHelper;
import me.earth.earthhack.impl.util.thread.SafeRunnable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Set;

public class AutoMineCalc implements SafeRunnable, Globals
{
    private final IAutomine automine;
    private final List<PlayerEntity> players;
    private final Set<BlockPos> surrounding;
    private final List<Entity> entities;
    private final PlayerEntity target;
    private final float minDamage;
    private final float maxSelf;
    private final double range;
    private final boolean obby;
    private final boolean newVer;
    private final boolean newVEntities;
    private final boolean mineObby;
    private final double breakTrace;
    private final boolean suicide;
    private int mX;
    private int mY;
    private int mZ;

    public AutoMineCalc(IAutomine automine,
                        List<PlayerEntity> players,
                        Set<BlockPos> surrounding,
                        List<Entity> entities,
                        PlayerEntity target,
                        float minDamage,
                        float maxSelf,
                        double range,
                        boolean obby,
                        boolean newVer,
                        boolean newVEntities,
                        boolean mineObby,
                        double breakTrace,
                        boolean suicide)
    {
        this.automine     = automine;
        this.players      = players;
        this.surrounding = surrounding;
        this.entities     = entities;
        this.target       = target;
        this.minDamage    = minDamage;
        this.maxSelf      = maxSelf;
        this.range        = range;
        this.obby         = obby;
        this.newVer       = newVer;
        this.newVEntities = newVEntities;
        this.mineObby     = mineObby;
        this.breakTrace   = breakTrace;
        this.suicide      = suicide;
    }

    @Override
    public void runSafely() throws Throwable
    {
        BlockPos middle = PositionUtil.getPosition();
        mX = middle.getX();
        mY = middle.getY();
        mZ = middle.getZ();
        BlockPos.Mutable mPos = new BlockPos.Mutable();
        int intRange = (int) range;
        double rSquare = range * range;
        double bSquare  = breakTrace * breakTrace;
        float maxDamage = Float.MIN_VALUE;
        IBlockStateHelper helper = new BlockStateHelper();
        IConstellation constellation = null;
        // no need for Sphere class here, order doesn't matter
        for (int x = mX - intRange; x <= mX + range; x++)
        {
            for (int z = mZ - intRange; z <= mZ + range; z++)
            {
                for (int y = mY - intRange; y < mY + range; y++)
                {
                    // skip position outside range
                    if (dsq(x, y, z) > rSquare
                        || dsq(x + 0.5f, y + 1, z + 0.5f) >= bSquare
                        && !RayTraceUtil.canBeSeen(
                        new Vec3d(x + 0.5f, y + 2.7, z + 0.5f),
                        RotationUtil.getRotationPlayer()))
                    {
                        continue;
                    }

                    mPos.set(x, y, z);
                    if (surrounding.contains(mPos))
                    {
                        return;
                    }

                    BlockState state = mc.world.getBlockState(mPos);
                    boolean isObbyState = state.getBlock() == Blocks.OBSIDIAN
                        || state.getBlock() == Blocks.BEDROCK;
                    if (!obby && !isObbyState
                        || !isObbyState
                        && !state.isReplaceable()
                        && !MineUtil.canBreak(state, mPos))
                    {
                        continue;
                    }

                    mPos.setY(y + 1);
                    BlockState upState = mc.world.getBlockState(mPos);
                    if (upState.getBlock() != Blocks.AIR
                        && !MineUtil.canBreak(upState, mPos)
                        || upState.getBlock() == Blocks.OBSIDIAN
                        && !mineObby
                        || upState.getBlock() != Blocks.AIR
                        && dsq(x, y + 1, z) > rSquare)
                    {
                        continue;
                    }

                    BlockState upUpState = null;
                    if (!newVer) // 1.13+
                    {
                        mPos.setY(y + 2);
                        upUpState = mc.world.getBlockState(mPos);
                        if (upUpState.getBlock() != Blocks.AIR
                            && !MineUtil.canBreak(upUpState, mPos)
                            || upUpState.getBlock() == Blocks.OBSIDIAN
                            && !mineObby
                            || upUpState.getBlock() != Blocks.AIR
                            && dsq(x, y + 2, z) > rSquare)
                        {
                            continue;
                        }
                    }

                    boolean bad = false;
                    for (Entity entity : entities)
                    {
                        if (/*entity.preventEntitySpawning &&*/ !isObbyState)
                        {
                            mPos.setY(y);
                            if (BBUtil.intersects(
                                entity.getBoundingBox(), mPos))
                            {
                                bad = true;
                                break;
                            }
                        }

                        mPos.setY(y + 1);
                        if (BBUtil.intersects(
                            entity.getBoundingBox(), mPos))
                        {
                            bad = true;
                            break;
                        }

                        if (!newVEntities)
                        {
                            mPos.setY(y + 2);
                            if (BBUtil.intersects(
                                entity.getBoundingBox(), mPos))
                            {
                                bad = true;
                                break;
                            }
                        }
                    }

                    if (bad)
                    {
                        continue;
                    }

                    helper.clearAllStates();
                    mPos.setY(y);
                    helper.addBlockState(mPos,
                                         Blocks.OBSIDIAN.getDefaultState());
                    BlockPos up = new BlockPos(x, y + 1, z);
                    helper.addAir(up);
                    BlockPos upUp = null;
                    if (!newVer)
                    {
                        upUp = up.up();
                        helper.addAir(upUp);
                    }

                    float self = DamageUtil.calculate(
                        x + 0.5f,
                        y + 1,
                        z + 0.5f,
                        RotationUtil.getRotationPlayer().getBoundingBox(),
                        RotationUtil.getRotationPlayer(),
                        /*helper*/ mc.world,
                        true);

                    if (!suicide && self > maxSelf)
                    {
                        continue;
                    }

                    float damage = Float.MIN_VALUE;
                    if (target == null)
                    {
                        for (PlayerEntity player : players)
                        {
                            if (player.squaredDistanceTo(x, y, z) > 144)
                            {
                                continue;
                            }

                            float d = DamageUtil.calculate(
                                x + 0.5f,
                                y + 1,
                                z + 0.5f,
                                player.getBoundingBox(),
                                player,
                                /*helper*/ mc.world,
                                true);

                            if (d > damage && ((damage = d) > minDamage))
                            {
                                break;
                            }
                        }
                    }
                    else
                    {
                        damage = DamageUtil.calculate(
                            x + 0.5f,
                            y + 1,
                            z + 0.5f,
                            target.getBoundingBox(),
                            target,
                            /*helper*/ mc.world,
                            true);
                    }

                    if (damage < minDamage
                        || damage < maxDamage
                        || damage < self)
                    {
                        continue;
                    }

                    BlockPos[] positions = new BlockPos[newVer ? 2 : 3];
                    positions[0] = mPos.toImmutable();
                    positions[1] = up;
                    if (!newVer)
                    {
                        positions[2] = upUp;
                    }

                    BlockState[] states = new BlockState[newVer ? 2 : 3];
                    states[0] = state;
                    states[1] = upState;
                    if (!newVer)
                    {
                        states[2] = upUpState;
                    }

                    maxDamage = damage;
                    constellation = new BigConstellation(
                        automine, positions, states, target);
                }
            }
        }

        if (constellation != null)
        {
            IConstellation finalConstellation = constellation;
            mc.execute(() ->
                                {
                                    automine.setFuture(null);
                                    automine.offer(finalConstellation);
                                });
        }
        else
        {
            mc.execute(() -> automine.setFuture(null));
        }
    }

    private double dsq(double x, double y, double z)
    {
        return (mX - x) * (mX - x) + (mZ - z) * (mZ - z) + (mY - y) * (mY - y);
    }

}
