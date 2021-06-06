/*
 * Copyright 2019 Project OpenUBL, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Eclipse Public License - v 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.project.openubl.xsender.keys;

import io.github.project.openubl.xsender.keys.component.ComponentModel;
import io.github.project.openubl.xsender.keys.component.ComponentValidationException;
import io.github.project.openubl.xsender.keys.provider.ConfigurationValidationHelper;
import io.github.project.openubl.xsender.keys.provider.ProviderConfigProperty;
import io.github.project.openubl.xsender.keys.qualifiers.ComponentProviderType;
import io.github.project.openubl.xsender.keys.qualifiers.RsaKeyProviderType;
import io.github.project.openubl.xsender.keys.qualifiers.RsaKeyType;
import org.jboss.logging.Logger;
import org.keycloak.common.util.CertificateUtils;
import org.keycloak.common.util.KeyUtils;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.common.util.PemUtils;
import org.keycloak.crypto.Algorithm;
import org.keycloak.crypto.KeyUse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;

@ApplicationScoped
@ComponentProviderType(providerType = KeyProvider.class)
@RsaKeyProviderType(type = RsaKeyType.GENERATED)
public class GeneratedRsaKeyProviderFactory extends AbstractRsaKeyProviderFactory {

    private static final Logger logger = Logger.getLogger(GeneratedRsaKeyProviderFactory.class);

    public static final String ID = "rsa-generated";

    private static final String HELP_TEXT = "Generates RSA keys and creates a self-signed certificate";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = configurationBuilder()
            .property(Attributes.KEY_SIZE_PROPERTY)
            .build();

    @Inject
    ComponentProvider componentProvider;

    @Override
    public KeyProvider create(String entityId, ComponentModel model) {
        return new ImportedRsaKeyProvider(entityId, model);
    }

    @Override
    public boolean createFallbackKeys(String entityId, KeyUse keyUse, String algorithm) {
        if (keyUse.equals(KeyUse.SIG) && isSupportedRsaAlgorithm(algorithm)) {
            ComponentModel generated = new ComponentModel();
            generated.setName("fallback-" + algorithm);
            generated.setParentId(entityId);
            generated.setProviderId(ID);
            generated.setProviderType(KeyProvider.class.getName());

            MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();
            config.putSingle(Attributes.PRIORITY_KEY, "-100");
            config.putSingle(Attributes.ALGORITHM_KEY, algorithm);
            generated.setConfig(config);

            componentProvider.addComponentModel(entityId, generated);

            return true;
        } else {
            return false;
        }
    }

    private boolean isSupportedRsaAlgorithm(String algorithm) {
        return algorithm.equals(Algorithm.RS256)
                || algorithm.equals(Algorithm.PS256)
                || algorithm.equals(Algorithm.RS384)
                || algorithm.equals(Algorithm.PS384)
                || algorithm.equals(Algorithm.RS512)
                || algorithm.equals(Algorithm.PS512);
    }

    @Override
    public void validateConfiguration(String entityId, ComponentModel model) throws ComponentValidationException {
        super.validateConfiguration(entityId, model);

        ConfigurationValidationHelper.check(model).checkList(Attributes.KEY_SIZE_PROPERTY, false);

        int size = model.get(Attributes.KEY_SIZE_KEY, 2048);

        if (!(model.contains(Attributes.PRIVATE_KEY_KEY) && model.contains(Attributes.CERTIFICATE_KEY))) {
            generateKeys(entityId, model, size);

            logger.debugv("Generated keys for entityId={0}", entityId);
        } else {
            PrivateKey privateKey = PemUtils.decodePrivateKey(model.get(Attributes.PRIVATE_KEY_KEY));
            int currentSize = ((RSAPrivateKey) privateKey).getModulus().bitLength();
            if (currentSize != size) {
                generateKeys(entityId, model, size);

                logger.debugv("Key size changed, generating new keys for entityId={0}", entityId);
            }
        }
    }

    private void generateKeys(String entityId, ComponentModel model, int size) {
        KeyPair keyPair;
        try {
            keyPair = KeyUtils.generateRsaKeyPair(size);
            model.put(Attributes.PRIVATE_KEY_KEY, PemUtils.encodeKey(keyPair.getPrivate()));
        } catch (Throwable t) {
            throw new ComponentValidationException("Failed to generate keys", t);
        }

        generateCertificate(entityId, model, keyPair);
    }

    private void generateCertificate(String entityId, ComponentModel model, KeyPair keyPair) {
        try {
            Certificate certificate = CertificateUtils.generateV1SelfSignedCertificate(keyPair, entityId);
            model.put(Attributes.CERTIFICATE_KEY, PemUtils.encodeCertificate(certificate));
        } catch (Throwable t) {
            throw new ComponentValidationException("Failed to generate certificate", t);
        }
    }

    @Override
    public String getHelpText() {
        return HELP_TEXT;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public String getId() {
        return ID;
    }
}
