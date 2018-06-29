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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.annotation.constraint.NonnullElements;
import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.annotation.constraint.Positive;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.BaseConverter;

/**
 * A helper class to generate self-signed keypairs.
 */
public class SelfSignedCertificateGenerator {

    /** Class logger. */
    @Nonnull private Logger log = LoggerFactory.getLogger(SelfSignedCertificateGenerator.class);

    /** Container for options that can be parsed from a command line. */
    @Nonnull private final CommandLineArgs args;
    
    /** Constructor. */
    public SelfSignedCertificateGenerator() {
        args = new CommandLineArgs();
    }
    
    /**
     * Set the type of key that will be generated. Defaults to RSA.
     * 
     * @param type type of key that will be generated
     */
    public void setKeyType(@Nonnull @NotEmpty final String type) {
        args.keyType = Constraint.isNotNull(StringSupport.trimOrNull(type), "Key type cannot be null or empty");
    }    

    /**
     * Set the size of the generated key. Defaults to 2048
     * 
     * @param size size of the generated key
     */
    public void setKeySize(@Positive final int size) {
        Constraint.isGreaterThan(0, size, "Key size must be greater than 0");
        
        args.keySize = size;
    }

    /**
     * Set the number of years for which the certificate will be valid.
     * 
     * @param lifetime number of years for which the certificate will be valid
     */
    public void setCertificateLifetime(@Positive final int lifetime) {
        Constraint.isGreaterThan(0, lifetime, "Certificate lifetime must be greater than 0");
        
        args.certificateLifetime = lifetime;
    }

    /**
     * Set the certificate algorithm that will be used. Defaults to SHA256withRSA.
     * 
     * @param alg certificate algorithm
     */
    public void setCertificateAlg(@Nonnull @NotEmpty final String alg) {
        args.certAlg = Constraint.isNotNull(StringSupport.trimOrNull(alg), "Algorithm cannot be null or empty");
    }    
    
    /**
     * Set the hostname that will appear in the certificate's DN.
     * 
     * @param name hostname that will appear in the certificate's DN
     */
    public void setHostName(@Nonnull @NotEmpty final String name) {
        args.hostname = Constraint.isNotNull(StringSupport.trimOrNull(name), "Hostname cannot be null or empty");
    }

    /**
     * Set the file to which the private key will be written.
     * 
     * @param file file to which the private key will be written
     */
    public void setPrivateKeyFile(@Nullable final File file) {
        args.privateKeyFile = file;
    }

    /**
     * Set the file to which the certificate will be written.
     * 
     * @param file file to which the certificate will be written
     */
    public void setCertificateFile(@Nullable final File file) {
        args.certificateFile = file;
    }

    /**
     * Set the type of keystore to create.
     * 
     * @param type keystore type
     */
    public void setKeystoreType(@Nonnull @NotEmpty final String type) {
        args.keystoreType = Constraint.isNotNull(StringSupport.trimOrNull(type),
                "Keystore type cannot be null or empty");
    }

    /**
     * Set the file to which the keystore will be written.
     * 
     * @param file file to which the keystore will be written
     */
    public void setKeystoreFile(@Nullable final File file) {
        args.keystoreFile = file;
    }

    /**
     * Set the password for the generated keystore.
     * 
     * @param password password for the generated keystore
     */
    public void setKeystorePassword(@Nullable final String password) {
        args.keystorePassword = password;
    }

    /**
     * Set the optional DNS subject alt names.
     * 
     * @param altNames collection of subject alt names.
     */
    public void setDNSSubjectAltNames(@Nonnull @NonnullElements final Collection<String> altNames) {
        args.dnsSubjectAltNames = new ArrayList<>(StringSupport.normalizeStringCollection(altNames));
    }

    /**
     * Set the optional URI subject alt names.
     * 
     * @param altNames collection of subject alt names.
     */
    public void setURISubjectAltNames(@Nonnull @NonnullElements final Collection<String> altNames) {
        args.uriSubjectAltNames = new ArrayList<>(StringSupport.normalizeStringCollection(altNames));
    }
    
    /**
     * The main routine.
     * 
     * @throws Exception if an error occurs
     */
    public void generate() throws Exception {
        validate();
        
        // Check all the files to prevent overwrite.
        
        if (args.privateKeyFile != null) {
            if (!args.privateKeyFile.createNewFile()) {
                throw new IOException("Private key file exists: " + args.privateKeyFile.getAbsolutePath());
            }
        }

        if (args.certificateFile != null) {
            if (!args.certificateFile.createNewFile()) {
                throw new IOException("Certificate file exists: " + args.certificateFile.getAbsolutePath());
            }
        }
        
        if (args.keystoreFile != null) {
            if (!args.keystoreFile.createNewFile()) {
                throw new IOException("KeyStore file exists: " + args.keystoreFile.getAbsolutePath());
            }
        }
        
        // Generate the material.
        final KeyPair keypair = generateKeyPair();
        final X509Certificate certificate = generateCertificate(keypair);

        
        // Write the requested files.
        
        if (args.privateKeyFile != null) {
            try (final JcaPEMWriter keyOut = new JcaPEMWriter(new FileWriter(args.privateKeyFile))) {
                keyOut.writeObject(keypair.getPrivate());
                keyOut.flush();
            }
        }

        if (args.certificateFile != null) {
            try (final JcaPEMWriter certOut = new JcaPEMWriter(new FileWriter(args.certificateFile))) {
                certOut.writeObject(certificate);
                certOut.flush();
            }
        }

        if (args.keystoreFile != null) {
            final KeyStore store = KeyStore.getInstance(args.keystoreType);
            store.load(null, null);
            store.setKeyEntry(args.hostname, keypair.getPrivate(), args.keystorePassword.toCharArray(),
                    new X509Certificate[] {certificate});

            try (final FileOutputStream keystoreOut = new FileOutputStream(args.keystoreFile)) {
                store.store(keystoreOut, args.keystorePassword.toCharArray());
                keystoreOut.flush();
            }
        }
    }

    /** Validates the settings. */
    protected void validate() {
        if (args.keySize > 2048) {
            log.warn("Key size is greater than 2048, this may cause problems with some JVMs");
        }

        if (args.hostname == null || args.hostname.length() == 0) {
            throw new IllegalArgumentException("A non-empty hostname is required");
        }

        if (args.keystoreFile != null && (args.keystorePassword == null || args.keystorePassword.length() == 0)) {
            throw new IllegalArgumentException("Keystore password cannot be null if a keystore file is given");
        }
    }

    /**
     * Generates the key pair for the certificate.
     * 
     * @return key pair for the certificate
     * @throws NoSuchAlgorithmException if there is a problem generating the keys
     */
    @Nonnull protected KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance(args.keyType);
            generator.initialize(args.keySize);
            return generator.generateKeyPair();
        } catch (final NoSuchAlgorithmException e) {
            log.error("The {} key type is not supported by this JVM", args.keyType);
            throw e;
        }
    }

    /**
     * Generates the self-signed certificate.
     * 
     * @param keypair keypair associated with the certificate
     * 
     * @return self-signed certificate
     * @throws Exception if an error occurs
     */
    @Nonnull protected X509Certificate generateCertificate(@Nonnull final KeyPair keypair) throws Exception {
        
        final X500Name dn = new X500Name("CN=" + args.hostname);
        final GregorianCalendar notBefore = new GregorianCalendar();
        final GregorianCalendar notOnOrAfter = new GregorianCalendar();
        notOnOrAfter.set(GregorianCalendar.YEAR, notOnOrAfter.get(GregorianCalendar.YEAR) + args.certificateLifetime);
        
        final X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                dn,
                new BigInteger(160, new SecureRandom()),
                notBefore.getTime(),
                notOnOrAfter.getTime(),
                dn,
                keypair.getPublic()
                );
        
        final JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();
        
        builder.addExtension(Extension.subjectKeyIdentifier, false,
                extUtils.createSubjectKeyIdentifier(keypair.getPublic()));

        builder.addExtension(Extension.subjectAlternativeName, false,
                GeneralNames.getInstance(new DERSequence(buildSubjectAltNames())));

        final X509CertificateHolder certHldr = builder.build(
                new JcaContentSignerBuilder(args.certAlg).build(keypair.getPrivate()));
        final X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHldr);
        
        cert.checkValidity(new Date());
        cert.verify(keypair.getPublic());
        
        return cert;
    }

    /**
     * Builds the subject alt names for the certificate.
     * 
     * @return subject alt names for the certificate
     */
    @Nonnull @NonnullElements protected ASN1Encodable[] buildSubjectAltNames() {
        
        final ArrayList<ASN1Encodable> subjectAltNames = new ArrayList<>();

        subjectAltNames.add(new GeneralName(GeneralName.dNSName, args.hostname));

        if (args.dnsSubjectAltNames != null) {
            for (final String subjectAltName : args.dnsSubjectAltNames) {
                subjectAltNames.add(new GeneralName(GeneralName.dNSName, subjectAltName));
            }
        }

        if (args.uriSubjectAltNames != null) {
            for (final String subjectAltName : args.uriSubjectAltNames) {
                subjectAltNames.add(new GeneralName(GeneralName.uniformResourceIdentifier, subjectAltName));
            }
        }
        
        return subjectAltNames.toArray(new ASN1Encodable[0]);
    }
    
    /**
     * Command line entry point.
     * 
     * @param args  command line arguments
     * 
     * @throws Exception 
     */
    public static void main(@Nonnull final String[] args) throws Exception {
    
        final SelfSignedCertificateGenerator generator = new SelfSignedCertificateGenerator();

        // Parse command line.
        final JCommander jc = new JCommander(generator.args, args);
        if (generator.args.help) {
            jc.setProgramName("SelfSignedCertificateGenerator");
            jc.usage();
            return;
        }

        generator.generate();
    }
    
    /** Command line option conversion from String to File. */
    public static class FileConverter extends BaseConverter<File> {
        
        /**
         * Constructor.
         *
         * @param optionName 
         */
        public FileConverter(final String optionName) {
            super(optionName);
        }

        @Override
        public File convert(final String value) {
            return new File(value);
        }
    }

    /** Manages command line parsing for application and the bean properties used by the generator. */
    private static class CommandLineArgs {

        /** Display command usage. */
        @Nonnull @NotEmpty public static final String HELP = "--help";

        /** Key algorithm. */
        @Nonnull @NotEmpty public static final String KEY_TYPE = "--type";
        
        /** Key size. */
        @Nonnull @NotEmpty public static final String KEY_SIZE = "--size";

        /** Certificate lifetime. */
        @Nonnull @NotEmpty public static final String CERT_LIFETIME = "--lifetime";

        /** Certificate algorithm. */
        @Nonnull @NotEmpty public static final String CERT_ALG = "--certAlg";

        /** Hostname for cert subject. */
        @Nonnull @NotEmpty public static final String HOSTNAME = "--hostname";
        
        /** DNS subjectAltNames. */
        @Nonnull @NotEmpty public static final String DNS_ALTNAMES = "--dnsAltName";

        /** URI subjectAltNames. */
        @Nonnull @NotEmpty public static final String URI_ALTNAMES = "--uriAltName";
        
        /** Path to private key file to create. */
        @Nonnull @NotEmpty public static final String KEY_FILE = "--keyfile";

        /** Path to certificate file to create. */
        @Nonnull @NotEmpty public static final String CERT_FILE = "--certfile";

        /** Type of keystore to create. */
        @Nonnull @NotEmpty public static final String STORE_TYPE = "--storetype";
        
        /** Path to keystore to create.  */
        @Nonnull @NotEmpty public static final String STORE_FILE = "--storefile";

        /** Keystore password.  */
        @Nonnull @NotEmpty public static final String STORE_PASS = "--storepass";
        
        /** Display command usage. */
        @Parameter(names = HELP, description = "Display program usage", help = true)
        private boolean help;

        /** Key algorithm. */
        @Parameter(names = KEY_TYPE, description = "Type of key to generate (default: RSA)")
        @Nonnull @NotEmpty private String keyType = "RSA";
        
        /** Key size. */
        @Parameter(names = KEY_SIZE, description = "Size of key to generate (default: 3072)")
        @Positive private int keySize = 3072;

        /** Certificate lifetime. */
        @Parameter(names = CERT_LIFETIME, description = "Certificate lifetime in years (default: 20)")
        @Positive private int certificateLifetime = 20;

        /** Certificate algorithm. */
        @Parameter(names = CERT_ALG, description = "Certificate algorithm (default: SHA256withRSA)")
        @Nonnull @NotEmpty private String certAlg = "SHA256withRSA";

        /** Hostname. */
        @Parameter(names = HOSTNAME, required = true, description = "Hostname for certificate subject")
        @Nonnull @NotEmpty private String hostname;
        
        /** DNS subjectAltNames. */
        @Parameter(names = DNS_ALTNAMES, description = "DNS subjectAltNames for certificate")
        @Nullable private List<String> dnsSubjectAltNames;

        /** URI subjectAltNames. */
        @Parameter(names = URI_ALTNAMES, description = "URI subjectAltNames for certificate")
        @Nullable private List<String> uriSubjectAltNames;
        
        /** Private key file. */
        @Parameter(names = KEY_FILE, converter = FileConverter.class, description = "Path to private key file")
        @Nullable private File privateKeyFile;

        /** Certificate file. */
        @Parameter(names = CERT_FILE, converter = FileConverter.class, description = "Path to certificate file")
        @Nullable private File certificateFile;
        
        /** Keystore type. */
        @Parameter(names = STORE_TYPE, description = "Type of keystore to generate (default: PKCS12)")
        @Nonnull @NotEmpty private String keystoreType = "PKCS12";
        
        /** Keystore file. */
        @Parameter(names = STORE_FILE, converter = FileConverter.class, description = "Path to keystore")
        @Nullable private File keystoreFile;
        
        /** Keystore password. */
        @Parameter(names = STORE_PASS, description = "Password for keystore")
        @Nullable private String keystorePassword;
    }
    
}