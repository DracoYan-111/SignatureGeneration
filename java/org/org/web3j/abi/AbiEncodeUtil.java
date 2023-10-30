package org.web3j.abi;

import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.util.Arrays;

public class AbiEncodeUtil {

    /**
     * Encodes multiple values into a packed hexadecimal string.
     *
     * @param types The values to be encoded.
     * @return The packed hexadecimal string.
     */
    public static String encodePacked(Type... types) {
        // Check if the array is empty
        if (types == null || types.length == 0) {
            return null;
        }

        // Create a StringBuilder to store the encoded string
        StringBuilder sb = new StringBuilder();
        sb.append("0x");

        // Loop through each value and encode it
        for (Type type : types) {
            sb.append(TypeEncoder.encodePacked(type));
        }

        // Return the encoded string
        return sb.toString();
    }

    /**
     * Encodes an array of Type objects into a hexadecimal string representation.
     *
     * @param types The array of Type objects to encode.
     * @return The hexadecimal string representation of the encoded types.
     */
    public static String encode(Type... types) {
        // Check if the types array is null or empty
        if (types == null || types.length == 0) {
            return null;
        }

        // Create a StringBuilder to build the encoded string
        StringBuilder sb = new StringBuilder();
        sb.append("0x");

        // Iterate over each Type object in the array and encode it
        for (Type type : types) {
            sb.append(TypeEncoder.encode(type));
        }

        // Return the final encoded string
        return sb.toString();
    }

    /**
     * Encode the given types to a packed byte array.
     * 
     * @param types The types to be encoded.
     * @return The packed byte array.
     */
    public static byte[] encodePackedToBytes(Type... types) {
        // Encode the types to a packed string
        String packedString = encodePacked(types);

        // Check if the packed string is null
        if (packedString == null) {
            // Return null if the packed string is null
            return null;
        }

        // Convert the packed string to a byte array and return it
        return Numeric.hexStringToByteArray(packedString);
    }

    /**
     * Encodes an array of Type objects into a byte array.
     *
     * @param types the Type objects to encode
     * @return the encoded byte array or null if the encoding fails
     */
    public static byte[] encodeToBytes(Type... types) {
        // Encode the types into a string
        String s = encode(types);

        // Check if the encoding was successful
        if (s == null) {
            return null;
        }

        // Convert the encoded string to a byte array
        return Numeric.hexStringToByteArray(s);
    }

    /**
     * Encodes the given types using the encodePacked function and returns the SHA3
     * hash of the encoded data.
     * 
     * @param types The types to be encoded
     * @return The SHA3 hash of the encoded data
     */
    public static String encodePackedToSha3(Type... types) {
        // Encode the types using the encodePacked function
        String encodePackedHex = encodePacked(types);

        // Convert the encoded data to a byte array
        byte[] bytes = Hash.sha3(Numeric.hexStringToByteArray(encodePackedHex));

        // Convert the byte array to a hex string and return it
        return Numeric.toHexString(bytes);
    }

    /**
     * Encodes the given types using the "encodePacked" method and computes the SHA3
     * hash of the result.
     *
     * @param types The types to encode.
     * @return The SHA3 hash of the encoded types as a byte array.
     */
    public static byte[] encodePackedToSha3Byte(Type... types) {
        // Encode the types using the "encodePacked" method
        String encodePackedHex = encodePacked(types);

        // Convert the encoded string to a byte array
        byte[] bytes = Hash.sha3(Numeric.hexStringToByteArray(encodePackedHex));

        // Return the byte array
        return bytes;
    }

    /**
     * Encodes the given types to SHA-3 hash.
     *
     * @param types the types to encode
     * @return the SHA-3 hash as a hexadecimal string
     */
    public static String encodeToSha3(Type... types) {
        // Encode the types using the encode() function
        String encodePackedHex = encode(types);

        // Convert the hexadecimal string to byte array and compute the SHA-3 hash
        byte[] bytes = Hash.sha3(Numeric.hexStringToByteArray(encodePackedHex));

        // Convert the byte array to hexadecimal string and return it
        return Numeric.toHexString(bytes);
    }

    /**
     * Encodes the given types to SHA3 byte array.
     *
     * @param types The types to encode.
     * @return The encoded SHA3 byte array.
     */
    public static byte[] encodeToSha3Byte(Type... types) {
        // Encode the types to a packed hex string
        String encodePackedHex = encode(types);

        // Convert the hex string to byte array
        byte[] bytes = Hash.sha3(Numeric.hexStringToByteArray(encodePackedHex));

        // Return the encoded SHA3 byte array
        return bytes;
    }

    /**
     * Concatenates two byte arrays together.
     *
     * @param array1 The first byte array.
     * @param array2 The second byte array.
     * @return The concatenated byte array.
     */
    public static byte[] concat(byte[] array1, byte[] array2) {
        byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    /**
     * Concatenates multiple byte arrays into a single byte array.
     *
     * @param arrays The byte arrays to be concatenated.
     * @return The concatenated byte array.
     */
    public static byte[] concatAll(byte[]... arrays) {
        if (arrays.length == 0) {
            return new byte[0];
        } else if (arrays.length == 1) {
            return arrays[0];
        } else {
            byte[] cur = concat(arrays[0], arrays[1]);

            for (int i = 2; i < arrays.length; ++i) {
                cur = concat(cur, arrays[i]);
            }

            return cur;
        }
    }

}
