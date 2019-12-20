/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.shibboleth.utilities.java.support.testing;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Command-line utility to show the automatic module names from a
 * <code>.jar</code> file or a directory of them.
 */
public class ShowAutomaticModuleNames {

    private static final String AUTO_MODULE_NAME = "Automatic-Module-Name";

    /**
     * Throw one of these to cause the main program to terminate.
     */
    private static class TerminationException extends Exception {

        /**
         * Mandatory version UID for any subclass of {@link Exception}.
         */
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param message message describing the reason for termination
         */
        public TerminationException(@Nonnull final String message) {
            super(message);
        }

        /**
         * Constructor.
         *
         * @param message message describing the reason for termination
         * @param cause a {@link Throwable} that was behind the problem
         */
        public TerminationException(@Nonnull final String message,
                @Nonnull final Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Fetch the automatic module name (if any) from a <code>.jar</code> file.
     *
     * @param jarFile <code>.jar</code> file to process
     * @return the automatic module name, or <code>null</code>
     * @throws TerminationException if something goes wrong
     */
    @Nullable
    private String getAutomaticModuleName(@Nonnull JarFile jarFile) throws TerminationException {
        final Manifest manifest;
        try {
            manifest = jarFile.getManifest();
            final Attributes mainAttributes = manifest.getMainAttributes();
            return mainAttributes.getValue(AUTO_MODULE_NAME);
        } catch (final IOException e) {
            throw new TerminationException("IOException while processing file", e);
        }
    }

    /**
     * Process all of the <code>.jar</code> files in a given directory, and present
     * the results in a readable way.
     *
     * @param directory directory to process
     * @throws TerminationException if something goes wrong
     */
    private void runDirectory(@Nonnull final File directory) throws TerminationException {
        System.out.println("Processing directory: " + directory.getAbsolutePath());
        final File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        final Map<String, File> seenNames = new HashMap<>();
        final List<File> noName = new ArrayList<>();
        final Map<File, String> withName = new HashMap<>();
        final Map<File, String> problemNames = new HashMap<>();
        for (File file : files) {
            try (final JarFile jarFile = new JarFile(file)) {
                final String automaticName = getAutomaticModuleName(jarFile);
                if (automaticName == null) {
                    noName.add(file);
                } else if (automaticName.isEmpty()) {
                    problemNames.put(file,  "empty module name");
                } else if (seenNames.containsKey(automaticName)) {
                    problemNames.put(file, "duplicate module name " + automaticName +
                            " with " + seenNames.get(automaticName).getName());
                } else {
                    withName.put(file, automaticName);
                    seenNames.put(automaticName, file);
                }
            } catch (final IOException e) {
                throw new TerminationException("could not process " + file.getName(), e);
            }
        }
        if (!problemNames.isEmpty()) {
            System.out.println("   *** with problematic names:");
            final List<File> problemFiles = new ArrayList<>(problemNames.keySet());
            for (File file : problemFiles) {
                System.out.println("      " + file.getName() + ": " + problemNames.get(file));
            }
        }
        if (!withName.isEmpty()) {
            System.out.println("   with module names:");
            final List<File> withNameFiles = new ArrayList<>(withName.keySet());
            withNameFiles.sort(null);
            for (File file : withNameFiles) {
                System.out.println("      " + file.getName() + " --> " + withName.get(file));
            }
        }
        if (!noName.isEmpty()) {
            System.out.println("   with no module name:");
            noName.sort(null);
            for (File file : noName) {
                System.out.println("      " + file.getName());
            }
        }
    }

    /**
     * Runnable main program, wrapped by <code>main</code> in an exception handler.
     *
     * @param args command-line arguments
     * @throws TerminationException if something bad happens
     */
    private void run(@Nonnull final String[] args) throws TerminationException {
        if (args.length != 1) {
            throw new TerminationException("ShowAutomaticModuleNames requires one argument, a file or directory");
        }

        final String fileName = args[0];
        final File file = new File(fileName);
        if (!file.exists()) {
            throw new TerminationException(fileName + " does not exist");
        }

        if (file.isDirectory()) {
            runDirectory(file);
        } else {
            try (final JarFile jarFile = new JarFile(file)) {
                final String automaticName = getAutomaticModuleName(jarFile);
                System.out.println("Automatic module name for " + fileName + " is " + automaticName);
            } catch (final IOException e) {
                throw new TerminationException("could not process " + fileName, e);
            }
        }
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        try {
            new ShowAutomaticModuleNames().run(args);
        } catch (TerminationException e) {
            e.printStackTrace();
            System.err.println("*** ERROR: " + e.getMessage());
            System.exit(1);
        }

    }

}
