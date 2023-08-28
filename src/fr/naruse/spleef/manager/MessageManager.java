package fr.naruse.spleef.manager;

import fr.naruse.spleef.main.SpleefPlugin;

public class MessageManager {
    protected SpleefPlugin pl;
    protected MessageManager(SpleefPlugin pl) {
        this.pl = pl;
    }

    public static class StringManager extends MessageManager{
        private String lang;
        public StringManager(SpleefPlugin pl) {
            super(pl);
            String currentLang = pl.getConfig().getString("currentLang");
            if(currentLang.equalsIgnoreCase("english")){
                lang = "english.";
            }else if(currentLang.equalsIgnoreCase("french")){
                lang = "french.";
            }else if(currentLang.equalsIgnoreCase("russian")){
                lang = "russian.";
            }else if(currentLang.equalsIgnoreCase("german")){
                lang = "german.";
            }else{
                lang = "english.";
                pl.getLogger().warning("Lang '"+currentLang+"' can't be recognized! Using english.");
            }
        }

        public String get(String path){
            String msg = pl.getConfigurations().getMessageConfiguration().getString(lang+path);

            if(msg == null){
                throw new NullPointerException("Unable to get path '"+lang+path+"'!");
            }

            return msg.replace("&", "§");
        }

        public String get(String path, String[] toReplace, String[] replacedBy) {
            String msg = get(path);
            if(path.equals("sign.bow.isOpened.isWaiting.line4") && get("sign.bow.isOpened.isWaiting.line4").equals("§5Splegg")){
                msg = "§5BowSpleef";
            }
            if(toReplace.length != 0){
                for (int i = 0; i < toReplace.length; i++) {
                    String to = "{"+toReplace[i]+"}";
                    String by = replacedBy[i];
                    msg = msg.replace(to, by);
                }
            }
            return msg.replace("&", "§");
        }

        public void setLang(String lang) {
            this.lang = lang+".";
        }
    }
}
