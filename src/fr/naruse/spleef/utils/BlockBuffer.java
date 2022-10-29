package fr.naruse.spleef.utils;

import com.google.common.collect.Sets;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BlockBuffer implements Iterable<Block> {

    private Set<Block> set = Sets.newHashSet();

    public BlockBuffer(List<Block> list) {
        this.set = Sets.newHashSet(list);
    }

    public BlockBuffer(Set<Block> set) {
        this.set = set;
    }

    public BlockBuffer() {
    }

    public BlockBuffer add(Block b){
        set.add(b);
        return this;
    }

    public boolean isEmpty(){
        return set.isEmpty();
    }

    public boolean contains(Block b){
        return set.contains(b);
    }

    public Set<Block> getSet() {
        return this.set;
    }

    @Override
    public Iterator<Block> iterator() {
        return set.iterator();
    }
}
