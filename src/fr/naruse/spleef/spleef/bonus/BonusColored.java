package fr.naruse.spleef.spleef.bonus;

import fr.naruse.api.async.CollectionManager;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

public abstract class BonusColored extends Bonus {
    private int timeBeforeAction;
    public BonusColored(BonusManager bonusManager, Player p, String name, int woolColorId, int timeBeforeAction) {
        super(bonusManager, p, 1, name, woolColorId);
        this.timeBeforeAction = timeBeforeAction;
    }

    protected abstract void onAction();

    protected void onTick() { }

    protected int timer = -1;
    private boolean wasRed = true;

    @Override
    public void bonusRun() {
        if(sheep == null){
            return;
        }
        if(sheep.isDead()){
            cancel();
            return;
        }
        if(timer == -1){
            timer = timeBeforeAction*20;
        }

        onTick();

        if(timer <= 0){
            onAction();
            cancel();
            runSync(() -> {
                sheep.remove();
                CollectionManager.ASYNC_ENTITY_LIST.remove(sheep);
            });
            return;
        }else{
            timer--;
        }

        if(isMulticolor){
            return;
        }

        boolean flag = true;
        for (int i = timeBeforeAction; i > 0; i--) {
            if(timer > i*20){
                flag = timer % i == 0;
                break;
            }
        }

        if(flag){
            if(wasRed){
                runSync(() -> sheep.setColor(DyeColor.WHITE));
                wasRed = false;
            }else{
                runSync(() -> sheep.setColor(DyeColor.getByWoolData((byte) getWoolColorId())));
                wasRed = true;
            }
        }
    }

}
