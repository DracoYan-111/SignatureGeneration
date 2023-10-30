package java;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import org.web3j.abi.AbiEncodeUtil;
import org.web3j.abi.EthersHelper;
import java.math.BigInteger;
import java.util.Arrays;

import java.util.List;

public class EthereumSignature {

    private static final String PRIVATEKEY = "";
    private static final String EIP712DOMAIN = Hash
            .sha3String("EIP712Domain(string name,string version,uint256 chainId,address verifyingContract)");
    private static final String CLAIMTOKEN = Hash
            .sha3String("claimToken(uint256 uuid, uint256 amount, address userAddress, uint256 nonce, uint deadline)");

    /**
     * Converts a hex string to a byte array.
     *
     * @param s The hex string to convert.
     * @return The byte array representation of the hex string.
     */
    public static byte[] hexStringToByteArray(String s) {
        // Remove the "0x" prefix if present
        if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }

        int len = s.length();
        byte[] data = new byte[len / 2];

        // Convert each pair of characters to a byte
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    /**
     * Converts the input value to a byte array.
     * 
     * @param inputValue The input value to convert.
     * @return The byte array representation of the input value.
     */
    public static byte[] convertArgToBytes(String inputValue) {
        byte[] argBytes = new byte[1]; // Create a byte array with initial capacity of 1

        try {
            String hexValue = inputValue; // Initialize hexValue with the input value

            // Check if the input value does not contain a hex prefix
            if (!Numeric.containsHexPrefix(inputValue)) {
                BigInteger value;
                try {
                    value = new BigInteger(inputValue); // Try to parse the input value as a decimal number
                } catch (NumberFormatException e) {
                    value = new BigInteger(inputValue, 16); // Parse the input value as a hexadecimal number
                }

                // Convert the value to a hexadecimal string without the prefix
                hexValue = Numeric.toHexStringNoPrefix(value.toByteArray());

                // If the hexadecimal string has a length greater than 64 and starts with "00",
                // remove the leading "00" characters
                if (hexValue.length() > 64 && hexValue.startsWith("00")) {
                    hexValue = hexValue.substring(2);
                }
            }

            // Convert the hexadecimal string to a byte array
            argBytes = Numeric.hexStringToByteArray(hexValue);
        } catch (Exception e) {
            // Handle any exceptions that occur during the conversion process
        }

        return argBytes; // Return the resulting byte array
    }

    /**
     * Generates the domain separator for a contract.
     *
     * @param contractName    The name of the contract.
     * @param contractAddress The address of the contract.
     * @return The domain separator as a byte array.
     */
    public static byte[] getDomainSeparator(String contractName, String contractAddress) {
        // Encode the data into bytes
        byte[] hash = AbiEncodeUtil.encodeToBytes(
                // EIP712 domain
                new Bytes32(hexStringToByteArray(EIP712DOMAIN)),
                // Contract name
                new Bytes32(hexStringToByteArray(Hash.sha3String(contractName))),
                // Contract version
                new Bytes32(hexStringToByteArray(Hash.sha3String("1"))),
                // Chain ID
                new Uint256(97),
                // Contract address
                new Address(contractAddress));

        // Ensure that the hash is not null
        assert hash != null;

        // Return the SHA3 hash of the encoded data
        return Hash.sha3(hash);
    }

    /**
     * Generates the digest of the user claim token for a given contract.
     *
     * @param contractName    The name of the contract.
     * @param contractAddress The address of the contract.
     * @param sigData         The signature data for the user claim.
     * @return The digest of the user claim token.
     */
    public static byte[] getUserClaimTokenDigest(String contractName, String contractAddress, ContractEntData contractEntData) {
        // Generate the domain separator
        byte[] domainSeparator = getDomainSeparator(contractName, contractAddress);

        // Encode the data to be hashed
        byte[] DataHash = AbiEncodeUtil.encodeToBytes(
                new Bytes32(hexStringToByteArray(CLAIMTOKEN)),
                contractEntData.uuid,
                contractEntData.amount,
                contractEntData.userAddress,
                contractEntData.nonce,
                contractEntData.deadline);

        // Convert arguments to bytes and create the list of types and values
        List<String> types = Arrays.asList("bytes1", "bytes1", "bytes32", "bytes32");
        List<Object> values = Arrays.asList(
                convertArgToBytes("0x19"),
                convertArgToBytes("0x01"),
                domainSeparator,
                Hash.sha3(DataHash));

        // Pack the types and values into a byte array
        byte[] result = EthersHelper.pack(types, values);

        // Hash the packed byte array and return the result
        return Hash.sha3(result);
    }

/**
 * This method generates and prints contract data for a given user claim token.
 */
public static void main(String[] args) {
    // Create an instance of ContractEntData
    ContractEntData contractEntData = new ContractEntData();

    // Initialize the fields of sigData
    contractEntData.uuid = new Uint256(BigInteger.valueOf(123456789));
    contractEntData.amount = new Uint256(BigInteger.valueOf(1000000000));
    contractEntData.userAddress = new Address("0x10e3a183db48d854870feda31630bc1eb0ddd52a");
    contractEntData.nonce = new Uint256(0);
    contractEntData.deadline = new Uint256(BigInteger.valueOf(1699629459));

    // Generate the hash of user claim token digest
    byte[] hash = getUserClaimTokenDigest("ClaimToken", "0xFBfb48044fd7b6Cd33a40F4f3D80c0755E8Da20E", sigData);

    // Create an instance of ECKeyPair using the private key
    ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(PRIVATEKEY));

    // Sign the hash message
    Sign.SignatureData signMessage = Sign.signMessage(hash, ecKeyPair, false);
    byte[] r = signMessage.getR();
    byte[] s = signMessage.getS();
    byte[] v = signMessage.getV();

    // Create an instance of ContractData
    ContractRetData contractData = new ContractRetData();

    // Set the fields of contractData
    contractData.uuid = contractEntData.uuid.getValue().toString();
    contractData.amount = contractEntData.amount.getValue().toString();
    contractData.userAddress = contractEntData.userAddress.toString();
    contractData.deadline = contractEntData.deadline.getValue().toString();
    contractData.v = Numeric.toBigInt(v);
    contractData.r = Numeric.toHexString(r);
    contractData.s = Numeric.toHexString(s);

    // Print the contract data
    System.out.println(contractData.uuid);
    System.out.println(contractData.amount);
    System.out.println(contractData.userAddress);
    System.out.println(contractData.deadline);
    System.out.println(contractData.v);
    System.out.println(contractData.r);
    System.out.println(contractData.s);
}
}
