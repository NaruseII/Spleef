package fr.naruse.spleef.ranking;

import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import fr.naruse.spleef.main.SpleefPlugin;

import java.util.List;
import java.util.Random;

public class ExternalDecentHologramPlugin extends HologramPlugin<Hologram>{

    public ExternalDecentHologramPlugin(SpleefPlugin pl) {
        super(pl);
    }

    @Override
    protected void insertLines(List<String> lines) {
        for (int i = 0; i < this.hologram.getPages().size(); i++) {
            this.hologram.removePage(i);
        }
        HologramPage hologramPage = this.hologram.addPage();

        hologramPage.addLine(new HologramLine(hologramPage, this.location, pl.getMessageManager().get("hologram.title")));
        for (String line : lines) {
            for (int i = 0; i < 5; i++) {
                hologramPage.addLine(new HologramLine(hologramPage, this.location, line+new Random().nextFloat()+"\n"));
            }

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
