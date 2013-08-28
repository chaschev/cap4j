package cap4j.core;

import java.io.File;

/**
 * User: achaschev
 * Date: 8/5/13
 * Time: 7:46 PM
 */
public abstract class Script {
    public File scriptsDir;

    public GlobalContext global;
    public CapConstants cap;

    public abstract void run() throws Exception;

    public void setProperties(GlobalContext global, File scriptsDir) {
        this.global = global;
        this.scriptsDir = scriptsDir;
        cap = global.cap;
    }
}