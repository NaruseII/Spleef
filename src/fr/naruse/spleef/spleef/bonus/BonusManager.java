package fr.naruse.spleef.spleef.bonus;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import fr.naruse.api.MathUtils;
import fr.naruse.spleef.api.SpleefBonusInitEvent;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.type.*;
import fr.naruse.spleef.spleef.type.Spleef;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class BonusManager extends BukkitRunnable implements Listener {

    private static final List<Class<? extends Bonus>> bonuses = Lists.newArrayList();
    private static final List<Runnable> onRestartRunnableList = Lists.newArrayList();

    public static List<Class<? extends Bonus>> getBonuses() {
        return bonuses;
    }

    static {
        bonuses.add(BonusExplosive.class);
        bonuses.add(BonusRepulsion.class);
        bonuses.add(BonusPlayerTeleporter.class);
        bonuses.add(BonusLittleLightning.class);
        bonuses.add(BonusBigLightning.class);
        bonuses.add(BonusSpeed.class);
        bonuses.add(BonusBlinder.class);
        bonuses.add(BonusGunner.class);
        bonuses.add(BonusSheepRepulsion.class);
        bonuses.add(BonusAttraction.class);
        bonuses.add(BonusBallistic.class);
        bonuses.add(BonusMelt.class);
        bonuses.add(BonusSlowness.class);
        bonuses.add(BonusRandom.class);
        bonuses.add(BonusIntergalactic.class);
        bonuses.add(BonusProjectileCounter.class);
        bonuses.add(BonusIntergalacticShield.class);
        bonuses.add(BonusJinton.class);
        bonuses.add(BonusLittleJinton.class);
        bonuses.add(BonusFloorFixer.class);
        bonuses.add(BonusMegaFloorFixer.class);
        bonuses.add(BonusFloorHider.class);
        bonuses.add(BonusDeadArrows.class);
        bonuses.add(BonusSeekerExplosive.class);
        bonuses.add(BonusVirus.class);
        bonuses.add(BonusCubeVacuum.class);

        Bukkit.getPluginManager().callEvent(new SpleefBonusInitEvent(bonuses));
    }

    private final Spleef spleef;
    private final SpleefPlugin pl;
    private final Random random = new Random();
    private final HashMap<Player, List<Bonus>> playerBonusMap = Maps.newHashMap();
    private final HashMap<Player, Integer> secondBeforeBonus = Maps.newHashMap();
    private final Set<Bonus> aliveBonus = Sets.newHashSet();
    private final List<Class<? extends Bonus>> spleefBonuses;

    public BonusManager(SpleefPlugin plugin, Spleef spleef) {
        this.spleef = spleef;
        this.pl = plugin;

        this.spleefBonuses = bonuses.stream().filter(bonus -> {
            String path = "spleef."+spleef.getId()+".bonus."+bonus.getSimpleName();
            return pl.getConfig().contains(path) ? pl.getConfig().getBoolean(path) : true;
        }).collect(Collectors.toList());

        Bukkit.getPluginManager().registerEvents(this, plugin);
        runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void run() {
        if(spleef.getCurrentStatus() == GameStatus.WAIT){
            return;
        }
        for (int o = 0; o < spleef.getPlayerInGame().size(); o++) {
            Player p = spleef.getPlayerInGame().get(o);
            if (spleef.getSpectators().contains(p) || p.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }
            if(!secondBeforeBonus.containsKey(p)){
                secondBeforeBonus.put(p, random.nextInt(10)+5);
            }else{
                int i = secondBeforeBonus.get(p);
                if(i <= 0){
                    secondBeforeBonus.put(p, random.nextInt(10)+5);
                    giveBonus(p);
                }else{
                    i--;
                    secondBeforeBonus.put(p, i);
                }
            }
        }
    }

    public void restart(){
        for (Bonus bonus : getAliveBonus()) {
            if(bonus.getSheep() != null &&! bonus.getSheep().isDead()){
                bonus.getSheep().remove();
            }
            bonus.cancel(false);
            bonus.onRestart();
        }
        for (Runnable runnable : onRestartRunnableList) {
            runnable.run();
        }
        onRestartRunnableList.clear();
        playerBonusMap.clear();
        secondBeforeBonus.clear();

        for (Entity entity : spleef.getArena().getWorld().getEntities()) {
            if(entity instanceof Pig && entity.isInvulnerable() && ((Pig) entity).hasPotionEffect(PotionEffectType.INVISIBILITY)){
                entity.remove();
            }
        }
    }

    public void giveBonus(List<Player> players){
        for (Player player : players) {
            this.giveBonus(player);
        }
    }

    public Bonus giveBonus(Player player){
        try {
            Class clazz = spleefBonuses.get(random.nextInt(spleefBonuses.size()));
            Constructor constructor = clazz.getDeclaredConstructor(BonusManager.class, Player.class);
            Bonus bonus = (Bonus) constructor.newInstance(this, player);
            bonus.giveWool();
            if(!playerBonusMap.containsKey(player)){
                playerBonusMap.put(player, Lists.newArrayList());
            }
            playerBonusMap.get(player).add(bonus);
            return bonus;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void giveBonus(Player p, String className){
        try{
            Class clazz = Class.forName("fr.naruse.spleef.spleef.bonus.type."+className);
            Constructor constructor = clazz.getDeclaredConstructor(BonusManager.class, Player.class);
            Bonus bonus = (Bonus) constructor.newInstance(this, p);
            bonus.giveWool();
            if(!playerBonusMap.containsKey(p)){
                playerBonusMap.put(p, Lists.newArrayList());
            }
            playerBonusMap.get(p).add(bonus);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @EventHandler
    public void interact(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!spleef.getPlayerInGame().contains(p) || !playerBonusMap.containsKey(p) || e.getItem() == null || e.getItem().getType() != Material.WOOL && p.getGameMode() != GameMode.SPECTATOR){
            return;
        }

        List<Bonus> list = playerBonusMap.get(p);
        for (int i = 0; i < list.size(); i++) {
            Bonus bonus = list.get(i);
            if(bonus.getWoolColorId() == e.getItem().getData().getData() && e.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(bonus.getName())){
                e.setCancelled(true);
                bonus.launchSheep();
                e.getItem().setAmount(e.getItem().getAmount()-1);
                list.remove(bonus);
                break;
            }
        }
    }

    @EventHandler
    public void projectile(ProjectileHitEvent e){
        if(e.getHitEntity() != null && e.getHitEntity() instanceof Player && spleef.getPlayerInGame().contains(e.getHitEntity()) && e.getEntity() instanceof Snowball){
            Snowball snowball = (Snowball) e.getEntity();
            if(snowball.getShooter() != null && snowball.getShooter() instanceof Sheep){
                e.getHitEntity().setVelocity(MathUtils.genVector(e.getEntity().getLocation(), e.getHitEntity().getLocation()));
            }
        }
    }

    public static void addRestartRunnable(Runnable runnable){
        onRestartRunnableList.add(runnable);
    }

    public Spleef getSpleef() {
        return spleef;
    }

    public SpleefPlugin getPlugin() {
        return pl;
    }

    public Set<Bonus> getAliveBonus() {
        return aliveBonus;
    }
}
