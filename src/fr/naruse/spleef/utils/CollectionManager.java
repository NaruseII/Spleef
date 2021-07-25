package fr.naruse.spleef.utils;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;

public class CollectionManager {

    public static final AntiConcurrentBufferSet<Runnable> SECOND_THREAD_RUNNABLE_SET = new AntiConcurrentBufferSet();

    public static class AntiConcurrentBufferSet<T> implements Iterable<T>{

        private final Set<T> set = Sets.newHashSet();

        public void add(T o){
            ThreadGlobal.getExecutorService().submit(() -> {
                set.add(o);
            });
        }

        public boolean isEmpty(){
            return set.isEmpty();
        }

        public void clear(){
            set.clear();
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return set.iterator();
        }
    }
}
