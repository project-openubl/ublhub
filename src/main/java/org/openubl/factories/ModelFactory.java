package org.openubl.factories;

import io.github.carlosthe19916.webservices.providers.BillServiceModel;
import org.openubl.models.SendFileMessageModel;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class ModelFactory {

    private ModelFactory() {
        // Just static methods
    }

    public static SendFileMessageModel getSendFilePropertiesModel(Message message) throws JMSException {
        SendFileMessageModel.Builder builder = SendFileMessageModel.Builder.aSunatJMSMessageModel();

        Enumeration<?> enumeration = message.getPropertyNames();
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                switch (key) {
                    case "documentType":
                        builder.withDocumentType(message.getStringProperty(key));
                        break;
                    case "serverUrl":
                        builder.withServerUrl(message.getStringProperty(key));
                        break;
                    case "fileName":
                        builder.withFileName(message.getStringProperty(key));
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

    public static Map<String, String> getAsMap(SendFileMessageModel model) {
        Map<String, String> map = new HashMap<>();

        map.put("fileName", model.getFileName());
        map.put("serverUrl", model.getServerUrl());
        map.put("documentType", model.getDocumentType());
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
