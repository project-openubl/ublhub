/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package io.github.project.openubl.xsender.avro;

import org.apache.avro.generic.GenericArray;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.util.Utf8;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@org.apache.avro.specific.AvroGenerated
public class CompanyEventKafka extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -2664036820612487684L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"CompanyEventKafka\",\"namespace\":\"io.github.project.openubl.xsender.avro\",\"fields\":[{\"name\":\"id\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"event\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}},{\"name\":\"owner\",\"type\":{\"type\":\"string\",\"avro.java.string\":\"String\"}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<CompanyEventKafka> ENCODER =
      new BinaryMessageEncoder<CompanyEventKafka>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<CompanyEventKafka> DECODER =
      new BinaryMessageDecoder<CompanyEventKafka>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageEncoder instance used by this class.
   * @return the message encoder used by this class
   */
  public static BinaryMessageEncoder<CompanyEventKafka> getEncoder() {
    return ENCODER;
  }

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   * @return the message decoder used by this class
   */
  public static BinaryMessageDecoder<CompanyEventKafka> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   * @return a BinaryMessageDecoder instance for this class backed by the given SchemaStore
   */
  public static BinaryMessageDecoder<CompanyEventKafka> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<CompanyEventKafka>(MODEL$, SCHEMA$, resolver);
  }

  /**
   * Serializes this CompanyEventKafka to a ByteBuffer.
   * @return a buffer holding the serialized data for this instance
   * @throws java.io.IOException if this instance could not be serialized
   */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /**
   * Deserializes a CompanyEventKafka from a ByteBuffer.
   * @param b a byte buffer holding serialized data for an instance of this class
   * @return a CompanyEventKafka instance decoded from the given buffer
   * @throws java.io.IOException if the given bytes could not be deserialized into an instance of this class
   */
  public static CompanyEventKafka fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private java.lang.String id;
   private java.lang.String event;
   private java.lang.String owner;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public CompanyEventKafka() {}

  /**
   * All-args constructor.
   * @param id The new value for id
   * @param event The new value for event
   * @param owner The new value for owner
   */
  public CompanyEventKafka(java.lang.String id, java.lang.String event, java.lang.String owner) {
    this.id = id;
    this.event = event;
    this.owner = owner;
  }

  public org.apache.avro.specific.SpecificData getSpecificData() { return MODEL$; }
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return event;
    case 2: return owner;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = value$ != null ? value$.toString() : null; break;
    case 1: event = value$ != null ? value$.toString() : null; break;
    case 2: owner = value$ != null ? value$.toString() : null; break;
    default: throw new IndexOutOfBoundsException("Invalid index: " + field$);
    }
  }

  /**
   * Gets the value of the 'id' field.
   * @return The value of the 'id' field.
   */
  public java.lang.String getId() {
    return id;
  }


  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'event' field.
   * @return The value of the 'event' field.
   */
  public java.lang.String getEvent() {
    return event;
  }


  /**
   * Sets the value of the 'event' field.
   * @param value the value to set.
   */
  public void setEvent(java.lang.String value) {
    this.event = value;
  }

  /**
   * Gets the value of the 'owner' field.
   * @return The value of the 'owner' field.
   */
  public java.lang.String getOwner() {
    return owner;
  }


  /**
   * Sets the value of the 'owner' field.
   * @param value the value to set.
   */
  public void setOwner(java.lang.String value) {
    this.owner = value;
  }

  /**
   * Creates a new CompanyEventKafka RecordBuilder.
   * @return A new CompanyEventKafka RecordBuilder
   */
  public static io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder newBuilder() {
    return new io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder();
  }

  /**
   * Creates a new CompanyEventKafka RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new CompanyEventKafka RecordBuilder
   */
  public static io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder newBuilder(io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder other) {
    if (other == null) {
      return new io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder();
    } else {
      return new io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder(other);
    }
  }

  /**
   * Creates a new CompanyEventKafka RecordBuilder by copying an existing CompanyEventKafka instance.
   * @param other The existing instance to copy.
   * @return A new CompanyEventKafka RecordBuilder
   */
  public static io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder newBuilder(io.github.project.openubl.xsender.avro.CompanyEventKafka other) {
    if (other == null) {
      return new io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder();
    } else {
      return new io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder(other);
    }
  }

  /**
   * RecordBuilder for CompanyEventKafka instances.
   */
  @org.apache.avro.specific.AvroGenerated
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<CompanyEventKafka>
    implements org.apache.avro.data.RecordBuilder<CompanyEventKafka> {

    private java.lang.String id;
    private java.lang.String event;
    private java.lang.String owner;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = other.fieldSetFlags()[0];
      }
      if (isValidValue(fields()[1], other.event)) {
        this.event = data().deepCopy(fields()[1].schema(), other.event);
        fieldSetFlags()[1] = other.fieldSetFlags()[1];
      }
      if (isValidValue(fields()[2], other.owner)) {
        this.owner = data().deepCopy(fields()[2].schema(), other.owner);
        fieldSetFlags()[2] = other.fieldSetFlags()[2];
      }
    }

    /**
     * Creates a Builder by copying an existing CompanyEventKafka instance
     * @param other The existing instance to copy.
     */
    private Builder(io.github.project.openubl.xsender.avro.CompanyEventKafka other) {
      super(SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.event)) {
        this.event = data().deepCopy(fields()[1].schema(), other.event);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.owner)) {
        this.owner = data().deepCopy(fields()[2].schema(), other.owner);
        fieldSetFlags()[2] = true;
      }
    }

    /**
      * Gets the value of the 'id' field.
      * @return The value.
      */
    public java.lang.String getId() {
      return id;
    }


    /**
      * Sets the value of the 'id' field.
      * @param value The value of 'id'.
      * @return This builder.
      */
    public io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder setId(java.lang.String value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'id' field has been set.
      * @return True if the 'id' field has been set, false otherwise.
      */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'id' field.
      * @return This builder.
      */
    public io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /**
      * Gets the value of the 'event' field.
      * @return The value.
      */
    public java.lang.String getEvent() {
      return event;
    }


    /**
      * Sets the value of the 'event' field.
      * @param value The value of 'event'.
      * @return This builder.
      */
    public io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder setEvent(java.lang.String value) {
      validate(fields()[1], value);
      this.event = value;
      fieldSetFlags()[1] = true;
      return this;
    }

    /**
      * Checks whether the 'event' field has been set.
      * @return True if the 'event' field has been set, false otherwise.
      */
    public boolean hasEvent() {
      return fieldSetFlags()[1];
    }


    /**
      * Clears the value of the 'event' field.
      * @return This builder.
      */
    public io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder clearEvent() {
      event = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /**
      * Gets the value of the 'owner' field.
      * @return The value.
      */
    public java.lang.String getOwner() {
      return owner;
    }


    /**
      * Sets the value of the 'owner' field.
      * @param value The value of 'owner'.
      * @return This builder.
      */
    public io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder setOwner(java.lang.String value) {
      validate(fields()[2], value);
      this.owner = value;
      fieldSetFlags()[2] = true;
      return this;
    }

    /**
      * Checks whether the 'owner' field has been set.
      * @return True if the 'owner' field has been set, false otherwise.
      */
    public boolean hasOwner() {
      return fieldSetFlags()[2];
    }


    /**
      * Clears the value of the 'owner' field.
      * @return This builder.
      */
    public io.github.project.openubl.xsender.avro.CompanyEventKafka.Builder clearOwner() {
      owner = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CompanyEventKafka build() {
      try {
        CompanyEventKafka record = new CompanyEventKafka();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.String) defaultValue(fields()[0]);
        record.event = fieldSetFlags()[1] ? this.event : (java.lang.String) defaultValue(fields()[1]);
        record.owner = fieldSetFlags()[2] ? this.owner : (java.lang.String) defaultValue(fields()[2]);
        return record;
      } catch (org.apache.avro.AvroMissingFieldException e) {
        throw e;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<CompanyEventKafka>
    WRITER$ = (org.apache.avro.io.DatumWriter<CompanyEventKafka>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<CompanyEventKafka>
    READER$ = (org.apache.avro.io.DatumReader<CompanyEventKafka>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

  @Override protected boolean hasCustomCoders() { return true; }

  @Override public void customEncode(org.apache.avro.io.Encoder out)
    throws java.io.IOException
  {
    out.writeString(this.id);

    out.writeString(this.event);

    out.writeString(this.owner);

  }

  @Override public void customDecode(org.apache.avro.io.ResolvingDecoder in)
    throws java.io.IOException
  {
    org.apache.avro.Schema.Field[] fieldOrder = in.readFieldOrderIfDiff();
    if (fieldOrder == null) {
      this.id = in.readString();

      this.event = in.readString();

      this.owner = in.readString();

    } else {
      for (int i = 0; i < 3; i++) {
        switch (fieldOrder[i].pos()) {
        case 0:
          this.id = in.readString();
          break;

        case 1:
          this.event = in.readString();
          break;

        case 2:
          this.owner = in.readString();
          break;

        default:
          throw new java.io.IOException("Corrupt ResolvingDecoder.");
        }
      }
    }
  }
}









