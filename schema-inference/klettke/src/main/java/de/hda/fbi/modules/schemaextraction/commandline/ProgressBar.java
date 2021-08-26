package de.hda.fbi.modules.schemaextraction.commandline;

public class ProgressBar {

    private final char[] workchars = {'|', '/', '-', '\\'};
    private final String format = "\r%3d%% %s %c";

    private int percent = 0;
    private int lastPercent;

    private StringBuilder progress;

    /**
     * initialize progress bar properties.
     */
    public ProgressBar() {
        init();
    }

    /**
     * called whenever the progress bar needs to be updated.
     * that is whenever progress was made.
     *
     * @param done  an int representing the work done so far
     * @param total an int representing the total work
     */
    public void update(int done, int total) {

        lastPercent = percent;
        percent = (++done * 100) / total;
        int extrachars = (percent / 2) - this.progress.length();

        while (extrachars-- > 0) {
            progress.append('#');
        }

        System.out.printf(format, percent, progress, workchars[done % workchars.length]);


        if (done == total) {
            System.out.flush();
            System.out.println();
            init();
        }
    }

    public void stop() {
        percent = 0;
        System.out.println("");
    }

    private void init() {
        this.percent = 0;
        this.progress = new StringBuilder(60);
    }
}
