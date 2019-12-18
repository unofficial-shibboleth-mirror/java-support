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

package net.shibboleth.utilities.java.support.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyException;
import java.security.KeyStore;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.security.impl.BasicKeystoreKeyStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.BaseConverter;


/**
 * Implements a tool for managing secret keys in accordance with the {@link BasicKeystoreKeyStrategy}. 
 */
public class BasicKeystoreKeyStrategyTool {
    
    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(BasicKeystoreKeyStrategyTool.class);

    /** Container for options that can be parsed from a command line. */
    @Nonnull private final CommandLineArgs args;
    
    /** Constructor. */
    public BasicKeystoreKeyStrategyTool() {
        args = new CommandLineArgs();
    }
    
    /**
     * Set the type of key that will be generated. Defaults to AES.
     * 
     * @param type type of key that will be generated
     */
    public void setKeyType(@Nonnull @NotEmpty final String type) {
        args.keyType = Constraint.isNotNull(StringSupport.trimOrNull(type), "Key type cannot be null or empty");
    }    

    /**
     * Set the size of the generated key. Defaults to 128
     * 
     * @param size size of the generated key
     */
    public void setKeySize(@Positive final int size) {
        Constraint.isGreaterThan(0, size, "Key size must be greater than 0");
        
        args.keySize = size;
    }
    
    /**
     * Set the encryption key alias base name.
     * 
     * @param alias the encryption key alias base
     */
    public void setKeyAlias(@Nonnull @NotEmpty final String alias) {
        
        args.keyAlias = Constraint.isNotNull(StringSupport.trimOrNull(alias),
                "Key alias base cannot be null or empty");
    }

    /**
     * Set the number of keys to maintain. Defaults to 30.
     * 
     * @param count number of keys to maintain
     */
    public void setKeyCount(@Positive final int count) {
        Constraint.isGreaterThan(0, count, "Key count must be greater than 0");
        
        args.keyCount = count;
    }

    /**
     * Set the type of keystore to create. Defaults to JCEKS.
     * 
     * @param type keystore type
     */
    public void setKeystoreType(@Nonnull @NotEmpty final String type) {
        args.keystoreType = Constraint.isNotNull(StringSupport.trimOrNull(type),
                "Keystore type cannot be null or empty");
    }

    /**
     * Set the keystore file to create or modify.
     * 
     * @param file keystore file
     */
    public void setKeystoreFile(@Nonnull final File file) {
        args.keystoreFile = Constraint.isNotNull(file, "Keystore file cannot be null");
    }

    /**
     * Set the password for the keystore.
     * 
     * @param password password for the keystore
     */
    public void setKeystorePassword(@Nullable final String password) {
        args.keystorePassword = password;
    }
    
    /**
     * Set the key versioning file to create or modify.
     * 
     * @param file key versioning file
     */
    public void setVersionFile(@Nonnull final File file) {
        args.versionFile = Constraint.isNotNull(file, "Key versioning file cannot be null");
    }
    
    /**
     * The main routine.
     * 
     * @throws Exception if an error occurs
     */
    public void changeKey() throws Exception {

        // Load keystore or create empty instance.
        final KeyStore ks = KeyStore.getInstance(args.keystoreType);
        try (final FileInputStream ksIn = args.keystoreFile.exists() ? new FileInputStream(args.keystoreFile) : null) {
            ks.load(ksIn, args.keystorePassword.toCharArray());
        }
        
        // Load key versioning properties.
        final Properties versionInfo = new Properties();
        if (args.versionFile.exists()) {
            try (final FileInputStream versionIn = new FileInputStream(args.versionFile)) {
                versionInfo.load(versionIn);
            }
        }
        
        // Load properties and increment key version.
        int currentVersion = Integer.parseInt(
                versionInfo.getProperty(BasicKeystoreKeyStrategy.CURRENT_VERSION_PROP, "0"));
        if (currentVersion == 0) {
            log.info("No existing versioning property, initializing...");
        } else {
            log.info("Incrementing key version from {} to {}", currentVersion, currentVersion + 1);
        }
        currentVersion++;
        
        // Make sure the new key doesn't already exist.
        final String newKeyAlias = args.keyAlias + Integer.toString(currentVersion);
        if (ks.containsAlias(newKeyAlias)) {
            log.error("Keystore already contains an entry named {}, exiting", newKeyAlias);
            throw new KeyException("Entry for new key already exists");
        }
        
        // Generate key and put it into the store.
        final KeyGenerator keyGenerator = KeyGenerator.getInstance(args.keyType);
        keyGenerator.init(args.keySize);
        final SecretKey newKey = keyGenerator.generateKey();
        ks.setKeyEntry(newKeyAlias, newKey, args.keystorePassword.toCharArray(), null);
        
        // Remove older keys maintaining the key count.
        int oldVersion = currentVersion - args.keyCount;
        while (oldVersion > 0) {
            final String oldAlias = args.keyAlias + Integer.toString(oldVersion);
            if (ks.containsAlias(oldAlias)) {
                log.info("Deleting old key: {}", oldAlias);
                ks.deleteEntry(oldAlias);
            } else {
                break;
            }
            oldVersion--;
        }
        
        // Save keystore back, and then the properties.
        try (final FileOutputStream ksOut = new FileOutputStream(args.keystoreFile)) {
            ks.store(ksOut, args.keystorePassword.toCharArray());
        }
        
        try (final FileOutputStream versionOut = new FileOutputStream(args.versionFile)) {
            versionInfo.setProperty(BasicKeystoreKeyStrategy.CURRENT_VERSION_PROP, Integer.toString(currentVersion));
            versionInfo.store(versionOut, null);
        }
    }
    
    /**
     * Command line entry point.
     * 
     * @param args  command line arguments
     * 
     * @throws Exception if something goes wrong
     */
    public static void main(@Nonnull final String[] args) throws Exception {
    
        final BasicKeystoreKeyStrategyTool tool = new BasicKeystoreKeyStrategyTool();

        // Parse command line.
        final JCommander jc = new JCommander(tool.args, args);
        if (tool.args.help) {
            jc.setProgramName("BasicKeystoreKeyStrategyTool");
            jc.usage();
            return;
        }

        tool.changeKey();
    }

    /** Command line option conversion from String to File. */
    public static class FileConverter extends BaseConverter<File> {
        
        /**
         * Constructor.
         *
         * @param optionName name of the option
         */
        public FileConverter(final String optionName) {
            super(optionName);
        }

        @Override
        public File convert(final String value) {
            return new File(value);
        }
    }

    /** Manages command line parsing for application and the bean properties used by the tool. */
    private static class CommandLineArgs {

        /** Display command usage. */
        @Nonnull @NotEmpty public static final String HELP = "--help";

        /** Key algorithm. */
        @Nonnull @NotEmpty public static final String KEY_TYPE = "--type";
        
        /** Key size. */
        @Nonnull @NotEmpty public static final String KEY_SIZE = "--size";

        /** Key alias base. */
        @Nonnull @NotEmpty public static final String KEY_ALIAS = "--alias";

        /** Number of keys to maintain. */
        @Nonnull @NotEmpty public static final String KEY_COUNT = "--count";

        /** Type of keystore to create. */
        @Nonnull @NotEmpty public static final String STORE_TYPE = "--storetype";
        
        /** Path to keystore to create.  */
        @Nonnull @NotEmpty public static final String STORE_FILE = "--storefile";

        /** Keystore password.  */
        @Nonnull @NotEmpty public static final String STORE_PASS = "--storepass";

        /** Path to key versioning file.  */
        @Nonnull @NotEmpty public static final String VERSION_FILE = "--versionfile";
        
        /** Display command usage. */
        @Parameter(names = HELP, description = "Display program usage", help = true)
        private boolean help;

        /** Key algorithm. */
        @Parameter(names = KEY_TYPE, description = "Type of key to generate (default: AES)")
        @Nonnull @NotEmpty private String keyType = "AES";
        
        /** Key size. */
        @Parameter(names = KEY_SIZE, description = "Size of key to generate (default: 128)")
        @Positive private int keySize = 128;

        /** Key alias base. */
        @Parameter(names = KEY_ALIAS, required = true, description = "Base name of key alias")
        @Nullable private String keyAlias;

        /** Key count. */
        @Parameter(names = KEY_COUNT, description = "Number of keys to maintain (default: 30)")
        @Positive private int keyCount = 30;
        
        /** Keystore type. */
        @Parameter(names = STORE_TYPE, description = "Type of keystore to generate (default: JCEKS)")
        @Nonnull @NotEmpty private String keystoreType = "JCEKS";
        
        /** Keystore file. */
        @Parameter(names=STORE_FILE, required=true, converter=FileConverter.class, description="Path to keystore")
        @Nullable private File keystoreFile;
        
        /** Keystore password. */
        @Parameter(names = STORE_PASS, required = true, description = "Password for keystore")
        @Nullable private String keystorePassword;

        /** Key versioning file. */
        @Parameter(names = VERSION_FILE, required = true, converter = FileConverter.class,
                description = "Path to key versioning file")
        @Nullable private File versionFile;
    }
    
}