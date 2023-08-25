package fr.naruse.spleef.spleef.bonus.type;

import com.google.common.collect.Lists;
import fr.naruse.api.MathUtils;
import fr.naruse.api.async.CollectionManager;
import fr.naruse.api.particle.Particle;
import fr.naruse.spleef.spleef.GameStatus;
import fr.naruse.spleef.spleef.bonus.BonusColored;
import fr.naruse.spleef.spleef.bonus.BonusManager;
import fr.naruse.spleef.utils.Utils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class BonusVirus extends BonusColored {

    private final List<Block> infectedBlocks = Lists.newArrayList();
    private boolean tenthWave = false;

    public BonusVirus(BonusManager bonusManager, Player p) {
        super(bonusManager, p, "§a§lVirus Sheep", 5, 3);
    }

    @Override
    protected void onAction() {
        this.sheep.getWorld().playSound(this.sheep.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 100, 100);
        this.sendParticle(this.sheep.getLocation(), Particle.getEnumParticle().EXPLOSION_HUGE(), 4, 3, 4, 5);

        for (int i = 0; i < 10; i++) {
            this.attackNewBlock(this.sheep.getLocation().getBlock(), 0, 0);
        }
    }

    private void attackNewBlock(Block currentBlock, int waveCount, long addTime) {
        if(System.currentTimeMillis()-addTime < 50+Utils.RANDOM.nextInt(250)){
            CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> {
                this.attackNewBlock(currentBlock, waveCount, addTime);
            });
            return;
        }
        if (this.needToStop()) {
            return;
        }

        if(waveCount > 10){
            if(!this.tenthWave){
                this.tenthWave = true;
                for (int i = 0; i < 5; i++) {
                    this.meltSmoothly();
                }

            }
            return;
        }

        List<Block> nearBlocks = Lists.newArrayList(MathUtils.nearBlocks(currentBlock));
        Collections.shuffle(nearBlocks);

        Block newBlock = null;
        for (Block block : nearBlocks) {
            if(block.getType() == Material.SNOW_BLOCK){
                newBlock = block;
                break;
            }
        }
        Block finalNewBlock = newBlock;
        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> {
            if(finalNewBlock != null){
                this.infectedBlocks.add(finalNewBlock);
                this.runSync(() -> {
                    finalNewBlock.setType(Material.WOOL);
                    finalNewBlock.setData((byte) 5);
                });
                this.attackNewBlock(finalNewBlock, waveCount+1, System.currentTimeMillis());
            }else{
                this.attackNewBlock(currentBlock, waveCount+1, System.currentTimeMillis());
            }
        });
    }

    private boolean needToStop() {
        if(this.spleef.getCurrentStatus() == GameStatus.WAIT){
            this.runSync(() -> {
                for (Block infectedBlock : this.infectedBlocks) {
                    infectedBlock.setType(Material.SNOW_BLOCK);
                }
                this.infectedBlocks.clear();
            });
            return true;
        }
        return false;
    }

    private void meltSmoothly() {
        if (this.needToStop()) {
            return;
        }

        CollectionManager.SECOND_THREAD_RUNNABLE_SET.add(() -> {
            if(this.infectedBlocks.isEmpty()){
                return;
            }

            Block block = this.infectedBlocks.get(0);
            this.infectedBlocks.remove(block);

            this.spleef.destroyBlock(this.p, block, true);
            this.meltSmoothly();
        });
    }
}
