package org.openubl.factories;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.openubl.models.MessageModel;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ModelFactory {

    private ModelFactory() {
        // Just static methods
    }

    public static MessageModel getSendFilePropertiesModel(Message message) throws JMSException {
        MessageModel.Builder builder = MessageModel.Builder.aSendFileMessageModel();

        Enumeration<?> enumeration = message.getPropertyNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                switch (key) {
                    case "entityId":
                        builder.withEntityId(Long.valueOf(message.getStringProperty(key)));
                        break;
                    case "username":
                        builder.withUsername(message.getStringProperty(key));
                        break;
                    case "password":
                        builder.withPassword(message.getStringProperty(key));
                        break;
                }
            }
        }

        return builder.build();
    }

    public static Map<String, String> getAsMap(MessageModel model) {
        Map<String, String> map = new HashMap<>();

        map.put("entityId", model.getEntityId().toString());
        map.put("username", model.getUsername());
        map.put("password", model.getPassword());

        return map;
    }

    public static BillServiceModel getBillServiceModel(Message message) throws JMSException {
        BillServiceModel model = new BillServiceModel();

        Enumeration<?> enumeration = message.getPropertyNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                switch (key) {
                    case "code":
                        model.setCode(message.getIntProperty(key));
                        break;
                    case "description":
                        model.setDescription(message.getStringProperty(key));
                        break;
                    case "ticket":
                        model.setTicket(message.getStringProperty(key));
                        break;
                    case "status":
                        model.setStatus(BillServiceModel.Status.valueOf(message.getStringProperty(key)));
                        break;
                }
            }
        }

        return model;
    }

    public static Map<String, String> getAsMap(BillServiceModel model) {
        Map<String, String> map = new HashMap<>();

        map.put("code", model.getCode().toString());
        map.put("description", model.getDescription());
        map.put("ticket", model.getTicket());
        map.put("status", model.getStatus().toString());

        return map;
    }
}
