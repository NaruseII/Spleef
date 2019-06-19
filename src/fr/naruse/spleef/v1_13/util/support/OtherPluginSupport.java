package fr.naruse.spleef.v1_13.util.support;

public class OtherPluginSupport {
    private VaultPlugin vaultPlugin;
    private HolographicDisplaysPlugin holographicDisplaysPlugin;
    public OtherPluginSupport(){
        this.vaultPlugin = new VaultPlugin();
        this.holographicDisplaysPlugin = new HolographicDisplaysPlugin();
    }

    public VaultPlugin getVaultPlugin() {
        return vaultPlugin;
    }

    public HolographicDisplaysPlugin getHolographicDisplaysPlugin() {
        return holographicDisplaysPlugin;
    }
}
