package qupath.lib.cpusampler;

import qupath.lib.cpusampler.gui.SamplerStarterAction;
import qupath.lib.gui.actions.ActionTools;
import qupath.lib.gui.extensions.QuPathExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.common.Version;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.tools.MenuTools;

import java.util.ResourceBundle;

/**
 * <p>Install the CPU sampler extension.</p>
 * <p>
 *     It adds one action to the Extensions menu, which starts the CPU sampler window
 *     (see {@link SamplerStarterAction}).
 * </p>
 */
public class CpuSamplerExtension implements QuPathExtension {

    private static final Logger logger = LoggerFactory.getLogger(CpuSamplerExtension.class);
    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.lib.cpusampler.strings");
    private static final Version EXTENSION_QUPATH_VERSION = Version.parse("v0.5.0");
    private boolean isInstalled = false;

    @Override
    public void installExtension(QuPathGUI qupath) {
        if (isInstalled) {
            logger.debug("{} is already installed", getName());
        } else {
            isInstalled = true;
            MenuTools.addMenuItems(qupath.getMenu("Extensions", false),
                    ActionTools.createAction(new SamplerStarterAction(), SamplerStarterAction.getActionTitle())
            );
        }
    }

    @Override
    public String getName() {
        return resources.getString("Extension.name");
    }

    @Override
    public String getDescription() {
        return resources.getString("Extension.description");
    }

    @Override
    public Version getQuPathVersion() {
        return EXTENSION_QUPATH_VERSION;
    }

    /**
     * @return the resource file containing the localized strings of the application
     */
    public static ResourceBundle getResources() {
        return resources;
    }
}
