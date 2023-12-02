package fr.naruse.spleef.ranking;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import fr.naruse.spleef.main.SpleefPlugin;

import java.util.List;

public class ExternalDecentHologramPlugin extends HologramPlugin<Hologram>{

    public ExternalDecentHologramPlugin(SpleefPlugin pl) {
        super(pl);
    }

    @Override
    protected void insertLines(List<String> lines) {
        int size;
        try {
            Object listObj = this.hologram.getClass().getMethod("getPages").invoke(this.hologram);
            size = (int) listObj.getClass().getMethod("size").invoke(listObj);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        for (int i = 0; i < size; i++) {
            this.hologram.removePage(i);
        }
        HologramPage hologramPage = this.hologram.addPage();

        hologramPage.addLine(new HologramLine(hologramPage, hologramPage.getNextLineLocation(), pl.getMessageManager().get("hologram.title")));
        for (String line : lines) {
            hologramPage.addLine(new HologramLine(hologramPage, hologramPage.getNextLineLocation(), line));
        }
        this.hologram.showAll();
    }

    @Override
    protected void deleteHologram() {
        this.hologram.destroy();
        this.hologram.delete();
    }

    @Override
    protected void createHologram() {
        this.hologram = new Hologram("SpleefHologram", this.location, false);
        this.hologram.showAll();
    }
}
