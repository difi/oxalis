/*
 * Copyright 2010-2017 Norwegian Agency for Public Management and eGovernment (Difi)
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/community/eupl/og_page/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package no.difi.oxalis.commons.filesystem;

import no.difi.oxalis.api.lang.OxalisLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;

/**
 * Represents the Oxalis Home directory, which is located by inspecting various
 * places in the system in this order:
 * <p>
 * <ol>
 * <li>Directory referenced by Local JNDI Context <code>java:comp/env/OXALIS_HOME</code></li>
 * <li>Directory referenced by Java System Property <code>-D OXALIS_HOME</code></li>
 * <li>Directory referenced by Environment Variable <code>OXALIS_HOME</code></li>
 * <li>Directory <code>.oxalis</code> inside the home directory of the user</li>
 * </ol>
 *
 * @author steinar
 * @author thore
 */
public class OxalisHomeDirectory {

    private static final Logger log = LoggerFactory.getLogger(OxalisHomeDirectory.class);

    protected static final String OXALIS_HOME_VAR_NAME = "OXALIS_HOME";

    protected static final String OXALIS_HOME_JNDI_PATH = "java:comp/env/OXALIS_HOME";

    public static File locateDirectory() {

        log.info("Attempting to locate home dir ....");

        File oxalisHomeDir = locateOxalisHomeFromLocalJndiContext();
        if (oxalisHomeDir == null) oxalisHomeDir = locateOxalisHomeFromJavaSystemProperty();
        if (oxalisHomeDir == null) oxalisHomeDir = locateOxalisHomeFromEnvironmentVariable();
        if (oxalisHomeDir == null) oxalisHomeDir = computeOxalisHomeRelativeToUserHome();

        try {
            oxalisHomeDir = validateOxalisHomeDirectory(oxalisHomeDir);
        } catch (OxalisLoadingException ex) {
            log.error(ex.getMessage());
            throw ex;
        }

        return oxalisHomeDir;
    }

    protected static File locateOxalisHomeFromLocalJndiContext() {
        try {
            String oxalis_home = (String) new InitialContext().lookup(OXALIS_HOME_JNDI_PATH);
            if (oxalis_home != null && oxalis_home.length() > 0) {
                log.info("Using OXALIS_HOME specified as JNDI path " + OXALIS_HOME_JNDI_PATH + " as " + oxalis_home);
                return new File(oxalis_home);
            }
        } catch (NamingException ex) {
            log.info("Unable to locate JNDI path " + OXALIS_HOME_JNDI_PATH + " ");
        }
        return null;
    }

    protected static File locateOxalisHomeFromJavaSystemProperty() {
        File result = null;
        String oxalis_home = System.getProperty(OXALIS_HOME_VAR_NAME);
        if (oxalis_home != null && oxalis_home.length() > 0) {
            log.info("Using OXALIS_HOME specified as Java System Property -D " +
                    OXALIS_HOME_VAR_NAME + " as " + oxalis_home);
            result = new File(oxalis_home);
        }
        return result;
    }

    protected static File locateOxalisHomeFromEnvironmentVariable() {
        File result = null;
        String oxalis_home = System.getenv(OXALIS_HOME_VAR_NAME);
        if (oxalis_home != null && oxalis_home.length() > 0) {
            log.info("Using OXALIS_HOME specified as Environment Variable $" +
                    OXALIS_HOME_VAR_NAME + " as " + oxalis_home);
            result = new File(oxalis_home);
        }
        return result;
    }

    protected static File computeOxalisHomeRelativeToUserHome() {
        String userHome = System.getProperty("user.home");
        File userHomeDir = new File(userHome);
        if (!userHomeDir.isDirectory()) {
            throw new IllegalStateException(userHome + " is not a directory");
        }
        File result;
        String relative_home = "/.oxalis";
        result = new File(userHomeDir, relative_home);
        if (result.exists()) {
            log.info("Using OXALIS_HOME relative to user.home " + relative_home + " as " + result);
            return result;
        }

        return null;
    }

    private static File validateOxalisHomeDirectory(File oxalisHomeDirectory) {
        if (oxalisHomeDirectory == null) {
            throw new OxalisLoadingException("No " + OXALIS_HOME_VAR_NAME +
                    " directory was found, Oxalis will probably cause major problems.");
        }
        if (!oxalisHomeDirectory.exists()) {
            throw new OxalisLoadingException(oxalisHomeDirectory + " does not exist!");
        } else if (!oxalisHomeDirectory.isDirectory()) {
            throw new OxalisLoadingException(oxalisHomeDirectory + " is not a directory");
        } else if (!oxalisHomeDirectory.canRead()) {
            throw new OxalisLoadingException(oxalisHomeDirectory + " exists, is a directory but can not be read");
        }
        return oxalisHomeDirectory;
    }
}
