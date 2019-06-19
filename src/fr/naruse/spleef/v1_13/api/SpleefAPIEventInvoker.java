package fr.naruse.spleef.v1_13.api;

import fr.naruse.spleef.manager.SpleefPluginV1_13;
import fr.naruse.spleef.v1_13.api.event.SpleefCancellable;
import fr.naruse.spleef.v1_13.api.event.SpleefEvent;
import org.bukkit.event.Listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class SpleefAPIEventInvoker {
    private boolean isCancelled = false;
    public SpleefAPIEventInvoker(SpleefEvent spleefEvent){
        for(Listener spleefListener : SpleefPluginV1_13.INSTANCE.spleefAPI.getSpleefListeners()){
            Method[] methods = spleefListener.getClass().getMethods();
            for (Method method : methods) {
                Annotation annotation = method.getAnnotation(SpleefEventHandler.class);
                if(method.getGenericParameterTypes().length != 0){
                    java.lang.reflect.Type eventType = method.getGenericParameterTypes()[0];
                    if (annotation instanceof SpleefEventHandler) {
                        try {
                            if(spleefEvent.getClass().getName().equals(eventType.getTypeName())){
                                method.invoke(spleefListener, spleefEvent);
                                Annotation cancellable = spleefEvent.getClass().getAnnotation(SpleefCancellable.class);
                                if(cancellable instanceof SpleefCancellable){
                                    if(!isCancelled){
                                        setCancelled((boolean) spleefEvent.getClass().getMethod("isCancelled").invoke(spleefEvent));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public boolean isCancelled() {
        return this.isCancelled;
    }

    protected void setCancelled(boolean var1) {
        this.isCancelled = var1;
    }
}
