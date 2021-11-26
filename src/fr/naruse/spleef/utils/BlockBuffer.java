package fr.naruse.spleef.utils;

import com.google.common.collect.Sets;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.Set;

public class BlockBuffer implements Iterable<Block> {

    private Set<Block> set = Sets.newHashSet();

    public BlockBuffer add(Block b){
        set.add(b);
        return this;
    }

    public boolean isEmpty(){
        return set.isEmpty();
    }

    @Override
    public Iterator<Block> iterator() {
        return set.iterator();
    }
}
