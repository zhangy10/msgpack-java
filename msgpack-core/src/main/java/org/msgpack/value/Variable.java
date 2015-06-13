//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.value;

import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageTypeCastException;
import org.msgpack.core.MessageStringCodingException;
import org.msgpack.core.MessageIntegerOverflowException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.CharacterCodingException;


public class Variable implements Value {
    private abstract class AbstractValueAccessor implements Value {
        @Override
        public boolean isNilValue() {
            return getValueType().isNilType();
        }

        @Override
        public boolean isBooleanValue() {
            return getValueType().isBooleanType();
        }

        @Override
        public boolean isNumberValue() {
            return getValueType().isNumberType();
        }

        @Override
        public boolean isIntegerValue() {
            return getValueType().isIntegerType();
        }

        @Override
        public boolean isFloatValue() {
            return getValueType().isFloatType();
        }

        @Override
        public boolean isRawValue() {
            return getValueType().isRawType();
        }

        @Override
        public boolean isBinaryValue() {
            return getValueType().isBinaryType();
        }

        @Override
        public boolean isStringValue() {
            return getValueType().isStringType();
        }

        @Override
        public boolean isArrayValue() {
            return getValueType().isArrayType();
        }

        @Override
        public boolean isMapValue() {
            return getValueType().isMapType();
        }

        @Override
        public boolean isExtensionValue() {
            return getValueType().isExtensionType();
        }

        @Override
        public NilValue asNilValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public BooleanValue asBooleanValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public NumberValue asNumberValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public IntegerValue asIntegerValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public FloatValue asFloatValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public RawValue asRawValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public BinaryValue asBinaryValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public StringValue asStringValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public ArrayValue asArrayValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public MapValue asMapValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public ExtensionValue asExtensionValue() {
            throw new MessageTypeCastException();
        }

        @Override
        public boolean equals(Object obj) {
            return Variable.this.equals(obj);
        }

        @Override
        public int hashCode() {
            return Variable.this.hashCode();
        }

        @Override
        public String toString() {
            return Variable.this.toString();
        }
    }

    public static enum Type {
        NULL(ValueType.NIL),
        BOOLEAN(ValueType.BOOLEAN),
        LONG(ValueType.INTEGER),
        BIG_INTEGER(ValueType.INTEGER),
        DOUBLE(ValueType.FLOAT),
        BYTE_ARRAY(ValueType.BINARY),
        RAW_STRING(ValueType.STRING),
        LIST(ValueType.ARRAY),
        MAP(ValueType.MAP),
        EXTENSION(ValueType.EXTENSION);

        private final ValueType valueType;

        private Type(ValueType valueType) {
            this.valueType = valueType;
        }

        public ValueType getValueType() {
            return valueType;
        }
    }

    private final NilValueAccessor nilAccessor = new NilValueAccessor();
    private final BooleanValueAccessor booleanAccessor = new BooleanValueAccessor();
    private final IntegerValueAccessor integerAccessor = new IntegerValueAccessor();
    private final FloatValueAccessor floatAccessor = new FloatValueAccessor();
    private final BinaryValueAccessor binaryAccessor = new BinaryValueAccessor();
    private final StringValueAccessor stringAccessor = new StringValueAccessor();
    private final ArrayValueAccessor arrayAccessor = new ArrayValueAccessor();
    private final MapValueAccessor mapAccessor = new MapValueAccessor();
    private final ExtensionValueAccessor extensionAccessor = new ExtensionValueAccessor();

    private Type type;

    private long longValue;
    private double doubleValue;
    private Object objectValue;

    private AbstractValueAccessor accessor;

    public Variable() {
        setNilValue();
    }


    ////
    // NilValue
    //

    public Variable setNilValue() {
        this.type = Type.NULL;
        this.accessor = nilAccessor;
        return this;
    }

    private class NilValueAccessor extends AbstractValueAccessor implements NilValue {
        @Override
        public ValueType getValueType() {
            return ValueType.NIL;
        }

        @Override
        public NilValue asNilValue() {
            return this;
        }

        @Override
        public ImmutableNilValue toImmutable() {
            return ValueFactory.nil();
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            pk.packNil();
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitNil();
        }
    }


    ////
    // BooleanValue
    //

    public Variable setBooleanValue(boolean v) {
        this.type = Type.BOOLEAN;
        this.accessor = booleanAccessor;
        this.longValue = (v ? 1L : 0L);
        return this;
    }

    private class BooleanValueAccessor extends AbstractValueAccessor implements BooleanValue {
        @Override
        public ValueType getValueType() {
            return ValueType.BOOLEAN;
        }

        @Override
        public BooleanValue asBooleanValue() {
            return this;
        }

        @Override
        public ImmutableBooleanValue toImmutable() {
            return ValueFactory.newBoolean(getBoolean());
        }

        @Override
        public boolean getBoolean() {
            return longValue == 1L;
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            pk.packBoolean(longValue == 1L);
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitBoolean(this);
        }
    }


    ////
    // NumberValue
    // IntegerValue
    // FloatValue
    //

    private static final BigInteger LONG_MIN = BigInteger.valueOf((long) Long.MIN_VALUE);
    private static final BigInteger LONG_MAX = BigInteger.valueOf((long) Long.MAX_VALUE);
    private static final long BYTE_MIN = (long) Byte.MIN_VALUE;
    private static final long BYTE_MAX = (long) Byte.MAX_VALUE;
    private static final long SHORT_MIN = (long) Short.MIN_VALUE;
    private static final long SHORT_MAX = (long) Short.MAX_VALUE;
    private static final long INT_MIN = (long) Integer.MIN_VALUE;
    private static final long INT_MAX = (long) Integer.MAX_VALUE;

    private abstract class AbstractNumberValueAccessor extends AbstractValueAccessor implements NumberValue {
        @Override
        public NumberValue asNumberValue() {
            return this;
        }

        @Override
        public byte byteValue() {
            if (type == Type.BIG_INTEGER) {
                return ((BigInteger) objectValue).byteValue();
            }
            return (byte) longValue;
        }

        @Override
        public short shortValue() {
            if (type == Type.BIG_INTEGER) {
                return ((BigInteger) objectValue).shortValue();
            }
            return (short) longValue;
        }

        @Override
        public int intValue() {
            if (type == Type.BIG_INTEGER) {
                return ((BigInteger) objectValue).intValue();
            }
            return (int) longValue;
        }

        @Override
        public long longValue() {
            if (type == Type.BIG_INTEGER) {
                return ((BigInteger) objectValue).longValue();
            }
            return (long) longValue;
        }

        @Override
        public BigInteger bigIntegerValue() {
            if (type == Type.BIG_INTEGER) {
                return (BigInteger) objectValue;
            }
            else if (type == Type.DOUBLE) {
                return new BigDecimal(doubleValue).toBigInteger();
            }
            return BigInteger.valueOf(longValue);
        }

        @Override
        public float floatValue() {
            if (type == Type.BIG_INTEGER) {
                return ((BigInteger) objectValue).floatValue();
            }
            else if (type == Type.DOUBLE) {
                return (float) doubleValue;
            }
            return (float) longValue;
        }

        @Override
        public double doubleValue() {
            if (type == Type.BIG_INTEGER) {
                return ((BigInteger) objectValue).doubleValue();
            }
            else if (type == Type.DOUBLE) {
                return doubleValue;
            }
            return (double) longValue;
        }
    }


    ////
    // IntegerValue
    //

    public Variable setIntegerValue(long v) {
        this.type = Type.LONG;
        this.accessor = integerAccessor;
        this.longValue = v;
        return this;
    }

    public Variable setIntegerValue(BigInteger v) {
        if (0 <= v.compareTo(LONG_MIN) && v.compareTo(LONG_MAX) <= 0) {
            this.type = Type.LONG;
            this.longValue = v.longValue();
        } else {
            this.type = Type.BIG_INTEGER;
            this.objectValue = v;
        }
        return this;
    }

    private class IntegerValueAccessor extends AbstractNumberValueAccessor implements IntegerValue {
        @Override
        public ValueType getValueType() {
            return ValueType.INTEGER;
        }

        @Override
        public IntegerValue asIntegerValue() {
            return this;
        }

        @Override
        public ImmutableIntegerValue toImmutable() {
            if (type == Type.BIG_INTEGER) {
                return ValueFactory.newInteger((BigInteger) objectValue);
            }
            return ValueFactory.newInteger(longValue);
        }

        @Override
        public boolean isInByteRange() {
            if (type == Type.BIG_INTEGER) {
                return false;
            }
            return BYTE_MIN <= longValue && longValue <= BYTE_MAX;
        }

        @Override
        public boolean isInShortRange() {
            if (type == Type.BIG_INTEGER) {
                return false;
            }
            return SHORT_MIN <= longValue && longValue <= SHORT_MAX;
        }

        @Override
        public boolean isInIntRange() {
            if (type == Type.BIG_INTEGER) {
                return false;
            }
            return INT_MIN <= longValue && longValue <= INT_MAX;
        }

        @Override
        public boolean isInLongRange() {
            if (type == Type.BIG_INTEGER) {
                return false;
            }
            return true;
        }

        @Override
        public byte getByte() {
            if (!isInByteRange()) {
                throw new MessageIntegerOverflowException(longValue);
            }
            return (byte) longValue;
        }

        @Override
        public short getShort() {
            if (!isInByteRange()) {
                throw new MessageIntegerOverflowException(longValue);
            }
            return (short) longValue;
        }

        @Override
        public int getInt() {
            if (!isInIntRange()) {
                throw new MessageIntegerOverflowException(longValue);
            }
            return (int) longValue;
        }

        @Override
        public long getLong() {
            if (!isInLongRange()) {
                throw new MessageIntegerOverflowException(longValue);
            }
            return longValue;
        }

        @Override
        public BigInteger getBigInteger() {
            if (type == Type.BIG_INTEGER) {
                return (BigInteger) objectValue;
            } else {
                return BigInteger.valueOf(longValue);
            }
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            if (type == Type.BIG_INTEGER) {
                pk.packBigInteger((BigInteger) objectValue);
            } else {
                pk.packLong(longValue);
            }
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitInteger(this);
        }
    }


    ////
    // FloatValue
    //

    public Variable setFloatValue(double v) {
        this.type = Type.DOUBLE;
        this.accessor = floatAccessor;
        this.doubleValue = v;
        this.longValue = (long) v;  // AbstractNumberValueAccessor uses longValue
        return this;
    }

    public Variable setFloatValue(float v) {
        this.type = Type.DOUBLE;
        this.accessor = floatAccessor;
        this.longValue = (long) v;  // AbstractNumberValueAccessor uses longValue
        return this;
    }

    private class FloatValueAccessor extends AbstractNumberValueAccessor implements FloatValue {
        @Override
        public FloatValue asFloatValue() {
            return this;
        }

        @Override
        public ImmutableFloatValue toImmutable() {
            return ValueFactory.newFloat(doubleValue);
        }

        @Override
        public ValueType getValueType() {
            return ValueType.FLOAT;
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            pk.packDouble(doubleValue);
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitFloat(this);
        }
    }


    ////
    // RawValue
    // BinaryValue
    // StringValue
    //

    private abstract class AbstractRawValueAccessor extends AbstractValueAccessor implements RawValue {
        @Override
        public RawValue asRawValue() {
            return this;
        }

        @Override
        public byte[] getByteArray() {
            return (byte[]) objectValue;
        }

        @Override
        public ByteBuffer getByteBuffer() {
            return ByteBuffer.wrap(getByteArray());
        }

        @Override
        public String getString() {
            byte[] raw = (byte[]) objectValue;
            try {
                CharsetDecoder reportDecoder = MessagePack.UTF8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT);
                return reportDecoder.decode(ByteBuffer.wrap(raw)).toString();
            } catch (CharacterCodingException ex) {
                throw new MessageStringCodingException(ex);
            }
        }

        @Override
        public String stringValue() {
            byte[] raw = (byte[]) objectValue;
            try {
                CharsetDecoder reportDecoder = MessagePack.UTF8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(CodingErrorAction.REPLACE);
                return reportDecoder.decode(ByteBuffer.wrap(raw)).toString();
            } catch (CharacterCodingException ex) {
                throw new MessageStringCodingException(ex);
            }
        }
    }

    ////
    // BinaryValue
    //

    public Variable setBinaryValue(byte[] v) {
        this.type = Type.BYTE_ARRAY;
        this.accessor = binaryAccessor;
        this.objectValue = v;
        return this;
    }

    private class BinaryValueAccessor extends AbstractRawValueAccessor implements BinaryValue {
        @Override
        public ValueType getValueType() {
            return ValueType.BINARY;
        }

        @Override
        public BinaryValue asBinaryValue() {
            return this;
        }

        @Override
        public ImmutableBinaryValue toImmutable() {
            return ValueFactory.newBinary(getByteArray());
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            byte[] data = (byte[]) objectValue;
            pk.packBinaryHeader(data.length);
            pk.writePayload(data);
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitBinary(this);
        }
    }


    ////
    // StringValue
    //

    public Variable setStringValue(String v) {
        return setStringValue(v.getBytes(MessagePack.UTF8));
    }

    public Variable setStringValue(byte[] v) {
        this.type = Type.RAW_STRING;
        this.accessor = stringAccessor;
        this.objectValue = v;
        return this;
    }

    private class StringValueAccessor extends AbstractRawValueAccessor implements StringValue {
        @Override
        public ValueType getValueType() {
            return ValueType.STRING;
        }

        @Override
        public StringValue asStringValue() {
            return this;
        }

        @Override
        public ImmutableStringValue toImmutable() {
            return ValueFactory.newString((byte[]) objectValue);
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            byte[] data = (byte[]) objectValue;
            pk.packRawStringHeader(data.length);
            pk.writePayload(data);
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitString(this);
        }
    }


    ////
    // ArrayValue
    //

    public Variable setArrayValue(List<Value> v) {
        this.type = Type.LIST;
        this.accessor = arrayAccessor;
        this.objectValue = v;
        return this;
    }

    private class ArrayValueAccessor extends AbstractValueAccessor implements ArrayValue {
        @Override
        public ValueType getValueType() {
            return ValueType.ARRAY;
        }

        @Override
        public ArrayValue asArrayValue() {
            return this;
        }

        @Override
        public ImmutableArrayValue toImmutable() {
            return ValueFactory.newArray(list());
        }

        @Override
        public int size() {
            return list().size();
        }

        @Override
        public Value get(int index) {
            return list().get(index);
        }

        @Override
        public Value getOrNilValue(int index) {
            List<Value> l = list();
            if (l.size() < index && index >= 0) {
                return ValueFactory.nil();
            }
            return l.get(index);
        }

        @Override
        public Iterator<Value> iterator() {
            return list().iterator();
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<Value> list() {
            return (List<Value>) objectValue;
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            List<Value> l = list();
            pk.packArrayHeader(l.size());
            for (Value e : l) {
                e.writeTo(pk);
            }
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitArray(this);
        }
    }

    ////
    // MapValue
    //

    public Variable setMapValue(Map<Value,Value> v) {
        this.type = Type.MAP;
        this.accessor = mapAccessor;
        this.objectValue = v;
        return this;
    }

    private class MapValueAccessor extends AbstractValueAccessor implements MapValue {
        @Override
        public ValueType getValueType() {
            return ValueType.MAP;
        }

        @Override
        public MapValue asMapValue() {
            return this;
        }

        @Override
        public ImmutableMapValue toImmutable() {
            return ValueFactory.newMap(map());
        }

        @Override
        public int size() {
            return map().size();
        }

        @Override
        public Set<Value> keySet() {
            return map().keySet();
        }

        @Override
        public Set<Map.Entry<Value, Value>> entrySet() {
            return map().entrySet();
        }

        @Override
        public Collection<Value> values() {
            return map().values();
        }

        @Override
        public Value[] getKeyValueArray() {
            Map<Value,Value> v = map();
            Value[] kvs = new Value[v.size() * 2];
            Iterator<Map.Entry<Value,Value>> ite = v.entrySet().iterator();
            int i = 0;
            while (ite.hasNext()) {
                Map.Entry<Value,Value> pair = ite.next();
                kvs[i] = pair.getKey();
                i++;
                kvs[i] = pair.getValue();
                i++;
            }
            return kvs;
        }

        @SuppressWarnings("unchecked")
        public Map<Value, Value> map() {
            return (Map<Value, Value>) objectValue;
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            Map<Value,Value> m = map();
            pk.packArrayHeader(m.size());
            for (Map.Entry<Value,Value> pair : m.entrySet()) {
                pair.getKey().writeTo(pk);
                pair.getValue().writeTo(pk);
            }
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitMap(this);
        }
    }


    ////
    // ExtensionValue
    //
    public Variable setExtensionValue(byte type, byte[] data) {
        this.type = Type.EXTENSION;
        this.accessor = extensionAccessor;
        this.objectValue = ValueFactory.newExtension(type, data);
        return this;
    }

    private class ExtensionValueAccessor extends AbstractValueAccessor implements ExtensionValue {
        @Override
        public ValueType getValueType() {
            return ValueType.EXTENSION;
        }

        @Override
        public ExtensionValue asExtensionValue() {
            return this;
        }

        @Override
        public ImmutableExtensionValue toImmutable() {
            return (ImmutableExtensionValue) objectValue;
        }

        @Override
        public byte getType() {
            return ((ImmutableExtensionValue) objectValue).getType();
        }

        @Override
        public byte[] getData() {
            return ((ImmutableExtensionValue) objectValue).getData();
        }

        @Override
        public void writeTo(MessagePacker pk) throws IOException {
            ((ImmutableExtensionValue) objectValue).writeTo(pk);
        }

        @Override
        public void accept(ValueVisitor visitor) {
            visitor.visitExtension(((ImmutableExtensionValue) objectValue));
        }
    }


    ////
    // Value
    //

    @Override
    public ImmutableValue toImmutable() {
        return accessor.toImmutable();
    }

    @Override
    public void writeTo(MessagePacker pk) throws IOException {
        accessor.writeTo(pk);
    }

    @Override
    public void accept(ValueVisitor visitor) {
        accessor.accept(visitor);
    }

    @Override
    public int hashCode() {
        return toImmutable().hashCode();  // TODO optimize
    }

    @Override
    public boolean equals(Object o) {
        return toImmutable().equals(o);  // TODO optimize
    }

    @Override
    public String toString() {
        return toImmutable().toString();  // TODO optimize
    }

    @Override
    public ValueType getValueType() {
        return type.getValueType();
    }

    @Override
    public boolean isNilValue() {
        return getValueType().isNilType();
    }

    @Override
    public boolean isBooleanValue() {
        return getValueType().isBooleanType();
    }

    @Override
    public boolean isNumberValue() {
        return getValueType().isNumberType();
    }

    @Override
    public boolean isIntegerValue() {
        return getValueType().isIntegerType();
    }

    @Override
    public boolean isFloatValue() {
        return getValueType().isFloatType();
    }

    @Override
    public boolean isRawValue() {
        return getValueType().isRawType();
    }

    @Override
    public boolean isBinaryValue() {
        return getValueType().isBinaryType();
    }

    @Override
    public boolean isStringValue() {
        return getValueType().isStringType();
    }

    @Override
    public boolean isArrayValue() {
        return getValueType().isArrayType();
    }

    @Override
    public boolean isMapValue() {
        return getValueType().isMapType();
    }

    @Override
    public boolean isExtensionValue() {
        return getValueType().isExtensionType();
    }

    @Override
    public NilValue asNilValue() {
        if (!isNilValue()) {
            throw new MessageTypeCastException();
        }
        return (NilValue) accessor;
    }

    @Override
    public BooleanValue asBooleanValue() {
        if (!isBooleanValue()) {
            throw new MessageTypeCastException();
        }
        return (BooleanValue) accessor;
    }

    @Override
    public NumberValue asNumberValue() {
        if (!isNumberValue()) {
            throw new MessageTypeCastException();
        }
        return (NumberValue) accessor;
    }

    @Override
    public IntegerValue asIntegerValue() {
        if (!isIntegerValue()) {
            throw new MessageTypeCastException();
        }
        return (IntegerValue) accessor;
    }

    @Override
    public FloatValue asFloatValue() {
        if (!isFloatValue()) {
            throw new MessageTypeCastException();
        }
        return (FloatValue) accessor;
    }

    @Override
    public RawValue asRawValue() {
        if (!isRawValue()) {
            throw new MessageTypeCastException();
        }
        return (RawValue) accessor;
    }

    @Override
    public BinaryValue asBinaryValue() {
        if (!isBinaryValue()) {
            throw new MessageTypeCastException();
        }
        return (BinaryValue) accessor;
    }

    @Override
    public StringValue asStringValue() {
        if (!isStringValue()) {
            throw new MessageTypeCastException();
        }
        return (StringValue) accessor;
    }

    @Override
    public ArrayValue asArrayValue() {
        if (!isArrayValue()) {
            throw new MessageTypeCastException();
        }
        return (ArrayValue) accessor;
    }

    @Override
    public MapValue asMapValue() {
        if (!isMapValue()) {
            throw new MessageTypeCastException();
        }
        return (MapValue) accessor;
    }

    @Override
    public ExtensionValue asExtensionValue() {
        if (!isExtensionValue()) {
            throw new MessageTypeCastException();
        }
        return (ExtensionValue) accessor;
    }
}
