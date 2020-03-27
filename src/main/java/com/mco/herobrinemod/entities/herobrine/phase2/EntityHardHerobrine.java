package com.mco.herobrinemod.entities.herobrine.phase2;

import com.google.common.base.Predicate;
import com.mco.herobrinemod.entities.herobrine.phase1.EntityHerobrine;
import com.mco.herobrinemod.entities.herobrine.phase2.ghast.EntityCorruptedGhast;
import com.mco.herobrinemod.config.HerobrineConfig;
import com.mco.herobrinemod.main.MainItems;
import net.ilexiconn.llibrary.server.animation.Animation;
import net.ilexiconn.llibrary.server.animation.AnimationAI;
import net.ilexiconn.llibrary.server.animation.AnimationHandler;
import net.ilexiconn.llibrary.server.animation.IAnimatedEntity;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.List;

public class EntityHardHerobrine extends EntityMob implements IRangedAttackMob, IMob, IAnimatedEntity
{
    private final BossInfoServer bossInfo = (BossInfoServer)(new BossInfoServer(this.getDisplayName(), BossInfo.Color.WHITE,
            BossInfo.Overlay.PROGRESS)).setDarkenSky(true);

    private static float scale = 6;

    private static final DataParameter<Float> SCALE = EntityDataManager.<Float>createKey(EntityHardHerobrine.class, DataSerializers.FLOAT);

    private Animation animation = NO_ANIMATION;
    private int animationTick;

    public static final Animation ANIMATION_DEATH_FULL = Animation.create(200);
    public static final Animation ANIMATION_DEATH = Animation.create(200);

    private static final Animation[] ANIMATIONS = {ANIMATION_DEATH, ANIMATION_DEATH_FULL};

    public AnimationAI currentAnim;

    private int deathTicks;
    private boolean hasSpawned = false;

       /** Selector used to determine the entities a wither boss should attack. */
    private static final Predicate<Entity> NOT_UNDEAD = new Predicate<Entity>()
    {
        public boolean apply(@Nullable Entity p_apply_1_)
        {
            return p_apply_1_ instanceof EntityLivingBase && ((EntityLivingBase)p_apply_1_).getCreatureAttribute()
                    != EnumCreatureAttribute.UNDEAD && !(p_apply_1_ instanceof EntityHerobrine || p_apply_1_ instanceof EntityCorruptedGhast)
                    && ((EntityLivingBase)p_apply_1_).attackable();
        }
    };

    public EntityHardHerobrine(World world){
        super(world);

        setScale(6);
        this.isImmuneToFire = true;
        this.setSize(4F, 12F);
        experienceValue = 250;
    }

    protected void initEntityAI()
    {
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIHardHerobrineAttack(this, 2, true));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 64.0F));
        this.tasks.addTask(8, new EntityAILookIdle(this));
        this.targetTasks.addTask(2, new EntityAIHurtByTarget(this, false, new Class[0]));

        this.applyEntityAI();
    }

    protected void applyEntityAI()
    {
        this.targetTasks.addTask(2, new EntityAINearestAttackableTarget(this, EntityPlayer.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityVillager.class, false));
        this.targetTasks.addTask(3, new EntityAINearestAttackableTarget(this, EntityIronGolem.class, false));
        this.targetTasks.addTask(4, new EntityAINearestAttackableTarget(this, EntityTameable.class, false));
        this.targetTasks.addTask(5, new EntityAINearestAttackableTarget(this, EntityMob.class, false));
        this.targetTasks.addTask(6, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, false));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.getDataManager().register(SCALE, this.scale);
    }

    public void setScale(float scale)
    {
        this.getDataManager().set(SCALE, Float.valueOf(scale));
    }

    public void setHasSpawned(boolean spawned){
        hasSpawned = spawned;
    }

    @SideOnly(Side.CLIENT)
    public float getScale()
    {
        return ((Float)this.getDataManager().get(SCALE)).floatValue();
    }

    @SideOnly(Side.CLIENT)
    public boolean getHasSpawned(){
        return hasSpawned;
    }

    public int getDeathTicks(){
        return deathTicks;
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(66.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(12.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6.0D);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1500);
    }

    public boolean isAIDisabled()
    {
        return false;
    }

    @Override
    public boolean isEntityUndead()
    {
        return true;
    }

    public boolean canDespawn()
    {
        return false;
    }

    public boolean isNonBoss()
    {
        return false;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if(getAttackTarget() == null && !world.isRemote) {
            if (getAttackTarget() == null) {
                List<EntityPlayer> list = this.world.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, this.getEntityBoundingBox().expand(64.0D, 64.0D, 64.0D));
                for (EntityPlayer entity : list) {
                    if (entity != null && !entity.isCreative())
                        setAttackTarget(entity);
                }
            }
        }

        if (getHealth() >= getMaxHealth() / 2)
            this.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, 0));

        if(getAnimation() != NO_ANIMATION)
        {
            animationTick++;
            if(world.isRemote && animationTick >= animation.getDuration())
            {
                setAnimation(NO_ANIMATION);
            }
        }

        if (getAttackTarget() != null && !world.isRemote && deathTicks == 0) {

            if (rand.nextInt(40) == 1) {
                attackEntityWithRangedAttack(getAttackTarget(), 0);
            }

            if (rand.nextInt(30) == 1 ) {
                attackWithBlazeFireballs(getAttackTarget());
                attackWithBlazeFireballs(getAttackTarget());
                attackWithBlazeFireballs(getAttackTarget());
            }

            if(rand.nextInt(50) == 1){
                EntityLightningBolt lightningBolt = new EntityLightningBolt(world, getAttackTarget().posX, getAttackTarget().posY, getAttackTarget().posZ, false);

                for(int i = 0; i < rand.nextInt(5); i++)
                    world.addWeatherEffect(lightningBolt);
            }

            if(rand.nextInt(100) == 1){
                EntityCorruptedGhast ghast = new EntityCorruptedGhast(world);
                ghast.setLocationAndAngles(getAttackTarget().posX + rand.nextInt(5),
                        getAttackTarget().posY + rand.nextInt(5), getAttackTarget().posZ + rand.nextInt(5), 0, 0);
                world.spawnEntity(ghast);
            }
        }

        if (getHealth() <= getMaxHealth() / 2 && getHealth() > getMaxHealth() / 4){
            setScale(3);
            this.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, 1));
        }

        if(getHealth() <= getMaxHealth() / 2 && !getHasSpawned() && !world.isRemote){
            EntityHerobrine herobrine = new EntityHerobrine(world, true);
            herobrine.setLocationAndAngles(posX, posY, posZ, 0, 0);
//            world.spawnEntity(herobrine);
            setHasSpawned(true);
        }

        if (getHealth() <= getMaxHealth() / 4){
            setScale(1.5F);
            this.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, Integer.MAX_VALUE, 3));
        }

        if(getHealth() <= getMaxHealth() / 4 && ticksExisted % 50 == 0 && getHealth() > 0)
            setHealth(getHealth() + 10);

        updateAITasks();
    }

    @Nullable
    @Override
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));
        IEntityLivingData data = super.onInitialSpawn(difficulty, livingdata);
        return data;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    protected void updateAITasks()
    {
        this.bossInfo.setPercent(this.getHealth() / this.getMaxHealth());
    }

    /**
     * Sets the custom name tag for this entity
     */
    public void setCustomNameTag(String name)
    {
        super.setCustomNameTag(name);
        this.bossInfo.setName(this.getDisplayName());
    }

    /**
     * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in
     * order to view its associated boss bar.
     */
    public void addTrackingPlayer(EntityPlayerMP player)
    {
        super.addTrackingPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    /**
     * Removes the given player from the list of players tracking this entity. See {@link net.minecraft.entity.Entity#addTrackingPlayer} for
     * more information on tracking.
     */
    public void removeTrackingPlayer(EntityPlayerMP player)
    {
        super.removeTrackingPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {}

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float unused) {
        double d1 = 4.0D;
        Vec3d vec3d = this.getLook(1.0F);
        double d2 = target.posX - (this.posX + vec3d.x * 4.0D);
        double d3;
        if(getScale() == 6)
            d3 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (0.5D + this.posY*2 + (double)(this.height / 2.0F));
        else
            d3 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (0.5D + this.posY*1 + (double)(this.height / 2.0F));

        double d4 = target.posZ - (this.posZ + vec3d.z * 4.0D);
        world.playEvent((EntityPlayer)null, 1016, new BlockPos(this), 0);
        EntityLargeFireball entitylargefireball = new EntityLargeFireball(world, this, d2, d3, d4);
        entitylargefireball.explosionPower = 1;
        entitylargefireball.posX = this.posX;
        if(getScale() == 6)
            entitylargefireball.posY = this.posY + (double)(this.height / 2.0F) + 5.0D;
        else if (getScale() == 3)
            entitylargefireball.posY = this.posY + (double)(this.height / 2.0F) + 0.0D;
        else if (getScale() == 1.5)
            entitylargefireball.posY = this.posY + (double)(this.height / 2.0F) - 3.0D;
        entitylargefireball.posZ = this.posZ;

        world.spawnEntity(entitylargefireball);
    }

    public void attackWithBlazeFireballs(EntityLivingBase target){

        double d0 = this.getDistanceSq(target);
        double d1 = target.posX - this.posX;
        double d2;
        if(getScale() == 6)
            d2 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (this.posY*2 + (double)(this.height / 2.0F));
        else
            d2 = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
        double d3 = target.posZ - this.posZ;
        float f = MathHelper.sqrt(MathHelper.sqrt(d0)) * 0.05F;

        for (int i = 0; i < 7; ++i)
        {
            EntitySmallFireball entitysmallfireball = new EntitySmallFireball(this.world, this, d1 + this.getRNG().nextGaussian() * (double)f, d2, d3 + this.getRNG().nextGaussian() * (double)f);
            if(getScale() == 6)
                entitysmallfireball.posY = this.posY + (double)(this.height / 2.0F) + 5.0D;
            else if (getScale() == 3)
                entitysmallfireball.posY = this.posY + (double)(this.height / 2.0F) + 0.0D;
            else if (getScale() == 1.5)
                entitysmallfireball.posY = this.posY + (double)(this.height / 2.0F) - 3.0D;
            this.world.spawnEntity(entitysmallfireball);
        }
    }

    @Override
    public int getAnimationTick()
    {
        return animationTick;
    }

    @Override
    public void setAnimationTick(int tick)
    {
        animationTick = tick;
    }

    @Override
    public Animation getAnimation()
    {
        return this.animation;
    }

    @Override
    public void setAnimation(Animation animation)
    {
        if(animation == NO_ANIMATION)
        {
            onAnimationFinish(this.animation);
            setAnimationTick(0);
        }

        this.animation = animation;
    }

    @Override
    public Animation[] getAnimations()
    {
        return ANIMATIONS;
    }

    protected void onAnimationFinish(Animation animation)
    {}

    public Animation getDeathAnimation()
    {
        if(HerobrineConfig.enableFight)
            return ANIMATION_DEATH;

        return ANIMATION_DEATH_FULL;
    }

    protected void onDeathAIUpdate()
    {
        if(HerobrineConfig.enableFight) {
            if (getAnimation() != ANIMATION_DEATH)
                AnimationHandler.INSTANCE.sendAnimationMessage(this, ANIMATION_DEATH);
        }
        else
            if(getAnimation() != ANIMATION_DEATH_FULL)
                AnimationHandler.INSTANCE.sendAnimationMessage(this, ANIMATION_DEATH_FULL);
    }

    @Override
    protected void onDeathUpdate() {
        onDeathAIUpdate();
        deathTicks++;

        if (getAnimation() == NO_ANIMATION && currentAnim == null) {
            if (HerobrineConfig.enableFight)
                //If the fight is enabled, play the death sequence with the chromatic aberration
                AnimationHandler.INSTANCE.sendAnimationMessage(this, ANIMATION_DEATH);
            else
                //Otherwise, play the full death animation
                AnimationHandler.INSTANCE.sendAnimationMessage(this, ANIMATION_DEATH_FULL);
        }

        boolean flag = this.world.getGameRules().getBoolean("doMobLoot");
        int i = 250;

        if (!this.world.isRemote) {
            if (this.deathTicks > 15 && this.deathTicks % 7 == 0 && flag) {
                this.dropExperience(i);
            }
        }

        if (!HerobrineConfig.enableFight)
        {
            if(deathTicks == 1)
                setAnimation(ANIMATION_DEATH_FULL);

            if (deathTicks >= 170 && deathTicks % 10 == 0)
                deathCircles(10, this, 15, "lightning");

            deathCircles(20, this, 0, "fireball");

            if (deathTicks % 10 == 0)
                deathCircles(13, this, 0, "explode");

            if (deathTicks == 200)
                setDead();
        }
        else
        {
            if(deathTicks == 1)
                setAnimation(ANIMATION_DEATH);

            if(deathTicks < 80)
                deathCircles(20, this, 0, "fireball");

            if (deathTicks % 10 == 0 && deathTicks < 100)
                deathCircles(13, this, 0, "explode");

            if (deathTicks == 300)
                setDead();
        }
    }


    /**
     * If killed by player, puts loot directly in player's inventory so it doesn't get destroyed by death sequence
     * Else, drops loot on ground like it normally would
     * If phase 3 enabled, drops fully enhanced set
     * Otherwise, drops thrice enhanced
     * */
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        ItemStack helmet = new ItemStack(MainItems.halfharder_helmet);
        ItemStack chestplate = new ItemStack(MainItems.halfharder_chestplate);
        ItemStack legs = new ItemStack(MainItems.halfharder_leggings);
        ItemStack boots = new ItemStack(MainItems.halfharder_boots);
        ItemStack sword = new ItemStack(MainItems.halfharder_sword);

        ItemStack[] halfDrops = {helmet, chestplate, legs, boots, sword};

        ItemStack hHelmet = new ItemStack(MainItems.hardest_helmet);
        ItemStack hChestplate = new ItemStack(MainItems.hardest_chestplate);
        ItemStack hLeggings = new ItemStack(MainItems.hardest_leggings);
        ItemStack hBoots = new ItemStack(MainItems.hardest_boots);
        ItemStack hSword = new ItemStack(MainItems.hardest_sword);

        ItemStack[] hardestDrops = {hHelmet, hChestplate, hLeggings, hBoots, hSword};

        if (cause.getTrueSource() instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer)cause.getTrueSource();

            if(HerobrineConfig.enableFight)
            {
                for(ItemStack tempDrops: hardestDrops)
                {
                    ItemHandlerHelper.giveItemToPlayer(entityPlayer, tempDrops);
                }
            }
            else
            {
                for(ItemStack tempDrops: halfDrops)
                {
                    ItemHandlerHelper.giveItemToPlayer(entityPlayer, tempDrops);
                }
            }
        }
        else if(world.getGameRules().getBoolean("doMobLoot"))
        {
            if(HerobrineConfig.enableFight)
            {
                for(ItemStack temp: hardestDrops)
                {
                    if(!world.isRemote)
                    {
                        EntityItem item = new EntityItem(world, posX, posY, posZ, temp);
                        world.spawnEntity(item);
                    }
                }
            }
            else
            {
                for(ItemStack temp: halfDrops)
                {
                    if(!world.isRemote)
                    {
                        EntityItem item = new EntityItem(world, posX, posY, posZ, temp);
                        world.spawnEntity(item);
                    }
                }
            }
        }
    }

    private void dropExperience(int time)
    {
        while (time > 0)
        {
            int i = EntityXPOrb.getXPSplit(time);
            time -= i;
            this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, i));
        }
    }

    /**
     * Handles all circle - related death events
     * An explanation of what this is doing can be found here:
     * @param r the radius of the circle
     * @param entity the entity which should have the lightning spawned around it
     * @param frequency the higher this number, the less lightning bolts are spawned
     * @param event what specific death event we are performing
     * */
    private void deathCircles(int r, EntityLivingBase entity, int frequency, String event)
    {
        if(event.equals("lightning"))
        {
            for(int theta = 0; theta <= 360; theta += frequency)
            {
                double x = Math.cos(theta) * r;
                double z = Math.sin(theta) * r;
                EntityLightningBolt lightningBolt = new EntityLightningBolt(world, entity.posX + x, entity.posY, entity.posZ + z, false);
                world.addWeatherEffect(lightningBolt);
            }
        }
        else if(event.equals("explode"))
        {
            int theta = rand.nextInt(360);

            double x = Math.cos(theta) * r * rand.nextDouble();
            double z = Math.sin(theta) * r * rand.nextDouble();

            world.newExplosion(this, posX + x, posY, posZ + z,3, false, false);
        }
        else if(event.equals("fireball"))
        {
            int theta = rand.nextInt(360);

            double x = Math.cos(theta) * r * rand.nextDouble();
            double z = Math.sin(theta) * r * rand.nextDouble();

            EntityLargeFireball fireball = new EntityLargeFireball(world, this, 0, -1, 0);
            fireball.setPosition(posX + x, posY + 50, posZ + z);
            world.spawnEntity(fireball);
        }
    }
}
