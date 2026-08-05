package me.hypherionmc.morecreativetabs.platform;

import me.hypherionmc.morecreativetabs.ModConstants;
import me.hypherionmc.morecreativetabs.platform.services.IPlatformHelper;

import java.util.ServiceLoader;
import java.util.Iterator;

/**
 * @author HypherionSA
 * Helper Class Loader Service
 */
public class PlatformServices {

    public static final IPlatformHelper helper = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        Iterator<T> services = ServiceLoader.load(clazz).iterator();
        if (!services.hasNext())
            throw new NullPointerException("Failed to load service for " + clazz.getName());
        final T loadedService = services.next();
        ModConstants.logger.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }

}
