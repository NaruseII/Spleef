package fr.naruse.spleef.ranking;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import fr.naruse.spleef.main.SpleefPlugin;

import java.util.List;

public class ExternalHolographicDisplaysPlugin extends HologramPlugin<Hologram> {

    public ExternalHolographicDisplaysPlugin(SpleefPlugin pl) {
        super(pl);

        this.reload();
    }

    @Override
    protected void insertLines(List<String> lines) {
        this.hologram.clearLines();
        this.hologram.appendTextLine(pl.getMessageManager().get("hologram.title"));
        for (String line : lines) {
            this.hologram.appendTextLine(line);
        }
    }

    @Override
    protected void deleteHologram() {
        this.hologram.delete();
    }

    @Override
    protected void createHologram() {
        this.hologram = HologramsAPI.createHologram(this.pl, this.location);
    }


}
