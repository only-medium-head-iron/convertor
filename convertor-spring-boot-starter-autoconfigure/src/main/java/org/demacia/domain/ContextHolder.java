package org.demacia.domain;

/**
 * @author hepenglin
 * @since 2024-08-10 20:08
 **/
public class ContextHolder {

    private static final ThreadLocal<Context> CONTEXT_HOLDER = new ThreadLocal<>();

    public static Context get() {
        return CONTEXT_HOLDER.get();
    }

    public static void set(Context context) {
        CONTEXT_HOLDER.set(context);
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
}
