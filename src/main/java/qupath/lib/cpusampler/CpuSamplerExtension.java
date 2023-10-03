package qupath.ui.cpusampler;

import qupath.lib.gui.actions.ActionTools;
import qupath.lib.gui.extensions.QuPathExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.tools.MenuTools;

import java.util.ResourceBundle;

public class CpuSamplerExtension implements QuPathExtension {

    private static final Logger logger = LoggerFactory.getLogger(CpuSamplerExtension.class);
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ui.cpusampler.strings");
    private static final String EXTENSION_NAME = "My Java extension";
    private static final String EXTENSION_DESCRIPTION = "This is just a demo to show how extensions work";
    private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.5.0");
    private boolean isInstalled = false;

    @Override
    public void installExtension(QuPathGUI qupath) {
        if (isInstalled) {
            logger.debug("{} is already installed", getName());
        } else {
            isInstalled = true;
            addMenuItem(qupath);
        }
    }

    private void addMenuItem(QuPathGUI qupath) {
        MenuTools.addMenuItems(qupath.getMenu("Extensions", false),
                ActionTools.createAction(() -> {}, "TEST")
        );
    }


    @Override
    public String getName() {
        return EXTENSION_NAME;
    }

    @Override
    public String getDescription() {
        return EXTENSION_DESCRIPTION;
    }

    @Override
    public Version getQuPathVersion() {
        return EXTENSION_QUPATH_VERSION;
    }
}
