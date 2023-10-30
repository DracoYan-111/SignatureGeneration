import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;

import java.math.BigInteger;


public class ContractRetData {
    // tx uuid
    public String uuid;
    // token amount
    public String amount;
    // user address
    public String userAddress;
    // singn timestamp
    public String deadline;
    
    public BigInteger v;
    public String r;
    public String s;
}
