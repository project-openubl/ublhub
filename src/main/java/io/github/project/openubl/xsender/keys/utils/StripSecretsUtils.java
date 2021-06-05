package io.github.project.openubl.xsender.keys.utils;

import io.github.project.openubl.xsender.keys.component.utils.ComponentUtil;
import io.github.project.openubl.xsender.keys.provider.ProviderConfigProperty;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StripSecretsUtils {

    private static final Pattern VAULT_VALUE = Pattern.compile("^\\$\\{vault\\.(.+?)}$");

    private static String maskNonVaultValue(String value) {
        return value == null
                ? null
                : (VAULT_VALUE.matcher(value).matches()
                ? value
                : ComponentRepresentation.SECRET_VALUE
        );
    }

    public static ComponentRepresentation strip(ComponentUtil componentUtil, ComponentRepresentation rep) {
        Map<String, ProviderConfigProperty> configProperties = componentUtil.getComponentConfigProperties(rep);
        if (rep.getConfig() == null) {
            return rep;
        }

        Iterator<Map.Entry<String, List<String>>> itr = rep.getConfig().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, List<String>> next = itr.next();
            ProviderConfigProperty configProperty = configProperties.get(next.getKey());
            if (configProperty != null) {
                if (configProperty.isSecret()) {
                    if (next.getValue() == null || next.getValue().isEmpty()) {
                        next.setValue(Collections.singletonList(ComponentRepresentation.SECRET_VALUE));
                    } else {
                        next.setValue(next.getValue().stream().map(StripSecretsUtils::maskNonVaultValue).collect(Collectors.toList()));
                    }
                }
            } else {
                itr.remove();
            }
        }
        return rep;
    }

    public static RealmRepresentation strip(RealmRepresentation rep) {
        if (rep.getSmtpServer() != null && rep.getSmtpServer().containsKey("password")) {
            rep.getSmtpServer().put("password", maskNonVaultValue(rep.getSmtpServer().get("password")));
        }
        return rep;
    }

    public static IdentityProviderRepresentation strip(IdentityProviderRepresentation rep) {
        if (rep.getConfig() != null && rep.getConfig().containsKey("clientSecret")) {
            rep.getConfig().put("clientSecret", maskNonVaultValue(rep.getConfig().get("clientSecret")));
        }
        return rep;
    }

    public static RealmRepresentation stripForExport(ComponentUtil componentUtil, RealmRepresentation rep) {
        strip(rep);

        List<ClientRepresentation> clients = rep.getClients();
        if (clients != null) {
            for (ClientRepresentation c : clients) {
                strip(c);
            }
        }
        List<IdentityProviderRepresentation> providers = rep.getIdentityProviders();
        if (providers != null) {
            for (IdentityProviderRepresentation r : providers) {
                strip(r);
            }
        }

        MultivaluedHashMap<String, ComponentExportRepresentation> components = rep.getComponents();
        if (components != null) {
            for (Map.Entry<String, List<ComponentExportRepresentation>> ent : components.entrySet()) {
                for (ComponentExportRepresentation c : ent.getValue()) {
                    strip(componentUtil, ent.getKey(), c);
                }
            }
        }

        List<UserRepresentation> users = rep.getUsers();
        if (users != null) {
            for (UserRepresentation u: users) {
                strip(u);
            }
        }

        users = rep.getFederatedUsers();
        if (users != null) {
            for (UserRepresentation u: users) {
                strip(u);
            }
        }

        return rep;
    }

    public static UserRepresentation strip(UserRepresentation user) {
        user.setCredentials(null);
        return user;
    }

    public static ClientRepresentation strip(ClientRepresentation rep) {
        if (rep.getSecret() != null) {
            rep.setSecret(maskNonVaultValue(rep.getSecret()));
        }
        return rep;
    }

    public static ComponentExportRepresentation strip(ComponentUtil componentUtil, String providerType, ComponentExportRepresentation rep) {
        Map<String, ProviderConfigProperty> configProperties = componentUtil.getComponentConfigProperties(providerType, rep.getProviderId());
        if (rep.getConfig() == null) {
            return rep;
        }

        Iterator<Map.Entry<String, List<String>>> itr = rep.getConfig().entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, List<String>> next = itr.next();
            ProviderConfigProperty configProperty = configProperties.get(next.getKey());
            if (configProperty != null) {
                if (configProperty.isSecret()) {
                    if (next.getValue() == null || next.getValue().isEmpty()) {
                        next.setValue(Collections.singletonList(ComponentRepresentation.SECRET_VALUE));
                    } else {
                        next.setValue(next.getValue().stream().map(StripSecretsUtils::maskNonVaultValue).collect(Collectors.toList()));
                    }
                }
            } else {
                itr.remove();
            }
        }

        MultivaluedHashMap<String, ComponentExportRepresentation> sub = rep.getSubComponents();
        for (Map.Entry<String, List<ComponentExportRepresentation>> ent: sub.entrySet()) {
            for (ComponentExportRepresentation c: ent.getValue()) {
                strip(componentUtil, ent.getKey(), c);
            }
        }
        return rep;
    }

}
