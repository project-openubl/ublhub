import DocumentInputSchema from "schemas/DocumentInputDto-schema.json";

export const buildSchema = (schema: any) => {
  return {
    ...DocumentInputSchema,
    definitions: { ...schema.definitions },
    properties: {
      ...DocumentInputSchema.properties,
      spec: {
        ...DocumentInputSchema.properties.spec,
        properties: {
          ...DocumentInputSchema.properties.spec.properties,
          document: { ...schema, $schema: undefined, definitions: undefined },
        },
      },
    },
  };
};
