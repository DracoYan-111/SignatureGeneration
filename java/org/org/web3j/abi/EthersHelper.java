package org.web3j.abi;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EthersHelper {

    private static final Pattern regexBytes = Pattern.compile("^bytes([0-9]+)$");
    private static final Pattern regexNumber = Pattern.compile("^(u?int)([0-9]*)$");
    private static final Pattern regexArray = Pattern.compile("^(.*)\\[([0-9]*)\\]$");

    /**
     * Packs a list of values into a byte array based on a list of types.
     *
     * @param types  The list of types of the values.
     * @param values The list of values to be packed.
     * @return The packed byte array.
     * @throws IllegalArgumentException If the number of types and values do not
     *                                  match.
     */
    public static byte[] pack(List<String> types, List<Object> values) {
        // Check if the number of types and values match
        if (types.size() != values.size()) {
            throw new IllegalArgumentException("wrong number of values; expected " + types.size());
        }

        // Pack each value based on its type
        List<byte[]> tight = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            tight.add(_pack(types.get(i), values.get(i)));
        }

        // Allocate a buffer to store the packed values
        ByteBuffer buffer = ByteBuffer.allocate(tight.stream().mapToInt(b -> b.length).sum());

        // Copy the packed values into the buffer
        for (byte[] b : tight) {
            buffer.put(b);
        }

        // Return the packed byte array
        return buffer.array();
    }

    /**
     * Packs a value of a given type into a byte array.
     *
     * @param type  The type of the value.
     * @param value The value to pack.
     * @return The packed value as a byte array.
     * @throws IllegalArgumentException If the type is invalid.
     */
    public static byte[] _pack(String type, Object value) {
        // Check the type and perform the corresponding packing logic
        switch (type) {
            case "address":
            case "bytes":
                // For address and bytes types, simply cast the value to byte[]
                return (byte[]) value;
            case "string":
                // For string type, convert the value to bytes using UTF-8 encoding
                return ((String) value).getBytes(StandardCharsets.UTF_8);
            case "bool":
                // For bool type, create a byte array with value 1 if true, 0 if false
                return new byte[] { (Boolean) value ? (byte) 1 : (byte) 0 };
            default:
                // For other types, check if the type matches a number type
                Matcher m = regexNumber.matcher(type);
                if (m.matches()) {
                    BigInteger bi = new BigInteger(value.toString());
                    int size = m.group(2).isEmpty() ? 256 : Integer.parseInt(m.group(2));
                    byte[] bytes = bi.toByteArray();
                    // If the number of bytes is less than required size, pad with zeros
                    if (bytes.length < size / 8) {
                        byte[] tmp = new byte[size / 8];
                        System.arraycopy(bytes, 0, tmp, tmp.length - bytes.length, bytes.length);
                        bytes = tmp;
                    }
                    return bytes;
                }
                // Check if the type matches a bytes type
                m = regexBytes.matcher(type);
                if (m.matches()) {
                    // For bytes type, simply cast the value to byte[]
                    return (byte[]) value;
                }
                // Check if the type matches an array type
                m = regexArray.matcher(type);
                if (m.matches() && value instanceof List) {
                    List<Object> list = (List<Object>) value;
                    List<byte[]> arrayBytes = new ArrayList<>();
                    // Iterate through the list and recursively pack each element
                    for (Object obj : list) {
                        arrayBytes.add(_pack(m.group(1), obj));
                    }
                    // Create a byte buffer to hold the packed array elements
                    ByteBuffer buffer = ByteBuffer.allocate(arrayBytes.stream().mapToInt(b -> b.length).sum());
                    for (byte[] b : arrayBytes) {
                        buffer.put(b);
                    }
                    return buffer.array();
                }
                // If the type is invalid, throw an exception
                throw new IllegalArgumentException("Invalid type: " + type);
        }
    }
}
