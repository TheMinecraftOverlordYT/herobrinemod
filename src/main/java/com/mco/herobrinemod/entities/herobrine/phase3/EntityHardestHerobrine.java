package com.mco.herobrinemod.entities.herobrine.phase3;

import com.mco.herobrinemod.entities.herobrine.phase3.ai.AILaser;
import com.mco.herobrinemod.entities.herobrine.phase3.laser.EntityLaser;
import com.mco.herobrinemod.main.HerobrineDamageSources;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationAI;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class EntityHardestHerobrine extends EntityMob implements IAnimatedEntity
{
    private Vec3d startPos;
    private Vec3d endPos;

    private Animation animation = NO_ANIMATION;
    private int animationTick;

    public static Animation ANIMATION_LASER = Animation.create(200);
    public static Animation ANIMATION_DEATH = Animation.create(200);

    private static final Animation[] ANIMATIONS = {ANIMATION_LASER, ANIMATION_DEATH};

    public AnimationAI currentAnim;

    private static final DataParameter<Integer> BEAM_LENGTH = EntityDataManager.createKey(EntityHardestHerobrine.class, DataSerializers.VARINT);

    public EntityHardestHerobrine(World world){
        super(world);
        setSize(15, 60);
        ignoreFrustumCheck = true;
        tasks.addTask(1, new AILaser(this, ANIMATION_LASER));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(66.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.33000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(666);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        getDataManager().register(BEAM_LENGTH, 0);
    }

    public void setBeamLength(int length){
        dataManager.set(BEAM_LENGTH, length);
    }

    public int getBeamLength(){
        return dataManager.get(BEAM_LENGTH);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setInteger("Beam Length", getBeamLength());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setBeamLength(compound.getInteger("Beam Length"));
    }

    @Override
    public boolean isAIDisabled() {
        return false;
    }

    @Override
    public void setInWeb() {
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
    }

    /**
     * Since it's so big, we have to override these
     * to make it not disappear when you look up
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRender3d(double x, double y, double z)
    {
        double d0 = this.posX - x;
        double d1 = this.posY + y;
        double d2 = this.posZ - z;
        double d3 = d0 * d0 + d1 * d1 + d2 * d2;
        return this.isInRangeToRenderDist(d3);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength();

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 128.0D;
        return distance < d0 * d0;
    }
    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public Animation getAnimation() {
        return this.animation;
    }

    @Override
    public int getAnimationTick() {
        return this.animationTick;
    }

    @Override
    public void setAnimationTick(int animationTick) {
        this.animationTick = animationTick;
    }

    @Override
    public void setAnimation(Animation animation) {
        if(animation == NO_ANIMATION){
            setAnimationTick(0);
        }
        this.animation = animation;
    }

    @Override
    public Animation[] getAnimations() {
        return ANIMATIONS;
    }

    public static Animation getDeathAnimation() {
        return ANIMATION_DEATH;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        this.rotationPitch = 38;
        laser(1);
        EntityLaser laser = new EntityLaser(world, this, posX, posY, posZ,
                (float)((rotationYawHead + 90) * Math.PI / 180), (float)(-rotationPitch * Math.PI / 180), 100);
    //    if(ticksExisted % 100 == 0)
         //   world.spawnEntity(laser);
/*        if(getAnimation() != NO_ANIMATION){
            animationTick++;
            if(world.isRemote && animationTick >= animation.getDuration())
                setAnimation(NO_ANIMATION);
        }

        if(currentAnim == null && getAnimation() == NO_ANIMATION && getAnimation() != ANIMATION_DEATH){
            AnimationHandler.INSTANCE.sendAnimationMessage(this, ANIMATION_LASER);
        }*/
    }

    @Nullable
    private void laser(int offset)
    {
        Vec3d initialVec = startPos = this.getPositionEyes(1);
        //Get the block or entity within 200 blocks of the start vec
        RayTraceResult rayTrace = this.rayTrace(200,1);
        Vec3d lookFar = rayTrace.hitVec;

        if(lookFar != null)
        {
            BlockPos secondPos = new BlockPos(lookFar);
            //Light a fire at the targeted block
            setFire(secondPos);
            setFire(secondPos.east());
            setFire(secondPos.west());
            setFire(secondPos.north());
            setFire(secondPos.south());

            double diffX = secondPos.getX() - initialVec.x;
            double diffY = secondPos.getY() - initialVec.y;
            double diffZ = secondPos.getZ() - initialVec.z;

            //Get how far away the hit block is from the start
            double length = Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2) + Math.pow(diffZ, 2));
            for (int i = 0; i < length; i++)
            {
                //Increase the divisor to increase the frequency of blocks in the line
                double factorX = diffX * (i / 32.0);
                double factorY = diffY * (i / 32.0);
                double factorZ = diffZ * (i / 32.0);

                Vec3d factorVec = new Vec3d(factorX, factorY, factorZ);
                Vec3d slopeVec = initialVec.add(factorVec);

                //Get the current block in the line
                BlockPos slopePos = new BlockPos(slopeVec);

                //Cosmetic stuff
                if (ticksExisted % 10 == 0)
                {
                    world.setBlockToAir(slopePos);

                    AxisAlignedBB axisPos = new AxisAlignedBB(slopePos.getX(), slopePos.getY(), slopePos.getZ(), slopePos.getX(), slopePos.getY(), slopePos.getZ());
                    axisPos.grow(2);
                    List entities = world.getEntitiesWithinAABB(Entity.class, axisPos);

                    if(entities.size() > 0 && entities.get(0) != null && !world.isRemote)
                    {
                        Entity entity = (Entity) entities.get(0);
                        System.out.println(entity);
                        entity.attackEntityFrom(HerobrineDamageSources.HARD_HEROBRINE, 10);
                    }

                    world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, slopePos.getX(), slopePos.getY(), slopePos.getZ(),
                            0, 0, 0);
                }
            }
        }
        this.endPos = lookFar;
    }

    public Vec3d getStartPos(){
        return startPos;
    }

    public Vec3d getEndPos()
    {
        return endPos;
    }

    private void setFire(BlockPos pos)
    {
        if(world.getBlockState(pos).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(world, pos)
                && !world.isRemote)
            world.setBlockState(pos, Blocks.FIRE.getDefaultState());
    }

}