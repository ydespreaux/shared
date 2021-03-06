/**
 * Autogenerated by Avro
 * <p>
 * DO NOT EDIT DIRECTLY
 */
package com.ydespreaux.shared.testcontainers.kafka.domain;

import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.SchemaStore;
import org.apache.avro.specific.SpecificData;

@SuppressWarnings("all")
/** Structure d'une machine */
@org.apache.avro.specific.AvroGenerated
public class WorkstationAvro extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
    public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"WorkstationAvro\",\"namespace\":\"com.ydespreaux.shared.testcontainers.kafka.domain\",\"doc\":\"Structure d'une machine\",\"fields\":[{\"name\":\"id\",\"type\":[\"null\",\"long\"],\"doc\":\"Identifiant technique de la machine\",\"default\":null},{\"name\":\"name\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Nom de la machine\",\"default\":null},{\"name\":\"serialNumber\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"doc\":\"Numero de serie de la machine\",\"default\":null}]}");
    private static final long serialVersionUID = 1963073151771998145L;
    private static SpecificData MODEL$ = new SpecificData();
    private static final BinaryMessageEncoder<WorkstationAvro> ENCODER =
            new BinaryMessageEncoder<WorkstationAvro>(MODEL$, SCHEMA$);
    private static final BinaryMessageDecoder<WorkstationAvro> DECODER =
            new BinaryMessageDecoder<WorkstationAvro>(MODEL$, SCHEMA$);
    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumWriter<WorkstationAvro>
            WRITER$ = (org.apache.avro.io.DatumWriter<WorkstationAvro>) MODEL$.createDatumWriter(SCHEMA$);
    @SuppressWarnings("unchecked")
    private static final org.apache.avro.io.DatumReader<WorkstationAvro>
            READER$ = (org.apache.avro.io.DatumReader<WorkstationAvro>) MODEL$.createDatumReader(SCHEMA$);
    /** Identifiant technique de la machine */
    @Deprecated
    public Long id;
    /** Nom de la machine */
    @Deprecated
    public String name;
    /** Numero de serie de la machine */
    @Deprecated
    public String serialNumber;

    /**
     * Default constructor.  Note that this does not initialize fields
     * to their default values from the schema.  If that is desired then
     * one should use <code>newBuilder()</code>.
     */
    public WorkstationAvro() {
    }
    /**
     * All-args constructor.
     * @param id Identifiant technique de la machine
     * @param name Nom de la machine
     * @param serialNumber Numero de serie de la machine
     */
    public WorkstationAvro(Long id, String name, String serialNumber) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
    }

    public static org.apache.avro.Schema getClassSchema() {
        return SCHEMA$;
    }

    /**
     * Return the BinaryMessageDecoder instance used by this class.
     */
    public static BinaryMessageDecoder<WorkstationAvro> getDecoder() {
        return DECODER;
    }

    /**
     * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
     * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
     */
    public static BinaryMessageDecoder<WorkstationAvro> createDecoder(SchemaStore resolver) {
        return new BinaryMessageDecoder<WorkstationAvro>(MODEL$, SCHEMA$, resolver);
    }

    /** Deserializes a WorkstationAvro from a ByteBuffer. */
    public static WorkstationAvro fromByteBuffer(
            java.nio.ByteBuffer b) throws java.io.IOException {
        return DECODER.decode(b);
    }

    /**
     * Creates a new WorkstationAvro RecordBuilder.
     * @return A new WorkstationAvro RecordBuilder
     */
    public static WorkstationAvro.Builder newBuilder() {
        return new WorkstationAvro.Builder();
    }

    /**
     * Creates a new WorkstationAvro RecordBuilder by copying an existing Builder.
     * @param other The existing builder to copy.
     * @return A new WorkstationAvro RecordBuilder
     */
    public static WorkstationAvro.Builder newBuilder(WorkstationAvro.Builder other) {
        return new WorkstationAvro.Builder(other);
    }

    /**
     * Creates a new WorkstationAvro RecordBuilder by copying an existing WorkstationAvro instance.
     * @param other The existing instance to copy.
     * @return A new WorkstationAvro RecordBuilder
     */
    public static WorkstationAvro.Builder newBuilder(WorkstationAvro other) {
        return new WorkstationAvro.Builder(other);
    }

    /** Serializes this WorkstationAvro to a ByteBuffer. */
    public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
        return ENCODER.encode(this);
    }

    public org.apache.avro.Schema getSchema() {
        return SCHEMA$;
    }

    // Used by DatumWriter.  Applications should not call.
    public Object get(int field$) {
        switch (field$) {
            case 0:
                return id;
            case 1:
                return name;
            case 2:
                return serialNumber;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    // Used by DatumReader.  Applications should not call.
    @SuppressWarnings(value = "unchecked")
    public void put(int field$, Object value$) {
        switch (field$) {
            case 0:
                id = (Long) value$;
                break;
            case 1:
                name = (String) value$;
                break;
            case 2:
                serialNumber = (String) value$;
                break;
            default:
                throw new org.apache.avro.AvroRuntimeException("Bad index");
        }
    }

    /**
     * Gets the value of the 'id' field.
     * @return Identifiant technique de la machine
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the 'id' field.
     * Identifiant technique de la machine
     * @param value the value to set.
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the 'name' field.
     * @return Nom de la machine
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the 'name' field.
     * Nom de la machine
     * @param value the value to set.
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the 'serialNumber' field.
     * @return Numero de serie de la machine
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * Sets the value of the 'serialNumber' field.
     * Numero de serie de la machine
     * @param value the value to set.
     */
    public void setSerialNumber(String value) {
        this.serialNumber = value;
    }

    @Override
    public void writeExternal(java.io.ObjectOutput out)
            throws java.io.IOException {
        WRITER$.write(this, SpecificData.getEncoder(out));
    }

    @Override
    public void readExternal(java.io.ObjectInput in)
            throws java.io.IOException {
        READER$.read(this, SpecificData.getDecoder(in));
    }

    /**
     * RecordBuilder for WorkstationAvro instances.
     */
    public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<WorkstationAvro>
            implements org.apache.avro.data.RecordBuilder<WorkstationAvro> {

        /** Identifiant technique de la machine */
        private Long id;
        /** Nom de la machine */
        private String name;
        /** Numero de serie de la machine */
        private String serialNumber;

        /** Creates a new Builder */
        private Builder() {
            super(SCHEMA$);
        }

        /**
         * Creates a Builder by copying an existing Builder.
         * @param other The existing Builder to copy.
         */
        private Builder(WorkstationAvro.Builder other) {
            super(other);
            if (isValidValue(fields()[0], other.id)) {
                this.id = data().deepCopy(fields()[0].schema(), other.id);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.name)) {
                this.name = data().deepCopy(fields()[1].schema(), other.name);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.serialNumber)) {
                this.serialNumber = data().deepCopy(fields()[2].schema(), other.serialNumber);
                fieldSetFlags()[2] = true;
            }
        }

        /**
         * Creates a Builder by copying an existing WorkstationAvro instance
         * @param other The existing instance to copy.
         */
        private Builder(WorkstationAvro other) {
            super(SCHEMA$);
            if (isValidValue(fields()[0], other.id)) {
                this.id = data().deepCopy(fields()[0].schema(), other.id);
                fieldSetFlags()[0] = true;
            }
            if (isValidValue(fields()[1], other.name)) {
                this.name = data().deepCopy(fields()[1].schema(), other.name);
                fieldSetFlags()[1] = true;
            }
            if (isValidValue(fields()[2], other.serialNumber)) {
                this.serialNumber = data().deepCopy(fields()[2].schema(), other.serialNumber);
                fieldSetFlags()[2] = true;
            }
        }

        /**
         * Gets the value of the 'id' field.
         * Identifiant technique de la machine
         * @return The value.
         */
        public Long getId() {
            return id;
        }

        /**
         * Sets the value of the 'id' field.
         * Identifiant technique de la machine
         * @param value The value of 'id'.
         * @return This builder.
         */
        public WorkstationAvro.Builder setId(Long value) {
            validate(fields()[0], value);
            this.id = value;
            fieldSetFlags()[0] = true;
            return this;
        }

        /**
         * Checks whether the 'id' field has been set.
         * Identifiant technique de la machine
         * @return True if the 'id' field has been set, false otherwise.
         */
        public boolean hasId() {
            return fieldSetFlags()[0];
        }


        /**
         * Clears the value of the 'id' field.
         * Identifiant technique de la machine
         * @return This builder.
         */
        public WorkstationAvro.Builder clearId() {
            id = null;
            fieldSetFlags()[0] = false;
            return this;
        }

        /**
         * Gets the value of the 'name' field.
         * Nom de la machine
         * @return The value.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the 'name' field.
         * Nom de la machine
         * @param value The value of 'name'.
         * @return This builder.
         */
        public WorkstationAvro.Builder setName(String value) {
            validate(fields()[1], value);
            this.name = value;
            fieldSetFlags()[1] = true;
            return this;
        }

        /**
         * Checks whether the 'name' field has been set.
         * Nom de la machine
         * @return True if the 'name' field has been set, false otherwise.
         */
        public boolean hasName() {
            return fieldSetFlags()[1];
        }


        /**
         * Clears the value of the 'name' field.
         * Nom de la machine
         * @return This builder.
         */
        public WorkstationAvro.Builder clearName() {
            name = null;
            fieldSetFlags()[1] = false;
            return this;
        }

        /**
         * Gets the value of the 'serialNumber' field.
         * Numero de serie de la machine
         * @return The value.
         */
        public String getSerialNumber() {
            return serialNumber;
        }

        /**
         * Sets the value of the 'serialNumber' field.
         * Numero de serie de la machine
         * @param value The value of 'serialNumber'.
         * @return This builder.
         */
        public WorkstationAvro.Builder setSerialNumber(String value) {
            validate(fields()[2], value);
            this.serialNumber = value;
            fieldSetFlags()[2] = true;
            return this;
        }

        /**
         * Checks whether the 'serialNumber' field has been set.
         * Numero de serie de la machine
         * @return True if the 'serialNumber' field has been set, false otherwise.
         */
        public boolean hasSerialNumber() {
            return fieldSetFlags()[2];
        }


        /**
         * Clears the value of the 'serialNumber' field.
         * Numero de serie de la machine
         * @return This builder.
         */
        public WorkstationAvro.Builder clearSerialNumber() {
            serialNumber = null;
            fieldSetFlags()[2] = false;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public WorkstationAvro build() {
            try {
                WorkstationAvro record = new WorkstationAvro();
                record.id = fieldSetFlags()[0] ? this.id : (Long) defaultValue(fields()[0]);
                record.name = fieldSetFlags()[1] ? this.name : (String) defaultValue(fields()[1]);
                record.serialNumber = fieldSetFlags()[2] ? this.serialNumber : (String) defaultValue(fields()[2]);
                return record;
            } catch (Exception e) {
                throw new org.apache.avro.AvroRuntimeException(e);
            }
        }
    }

}
