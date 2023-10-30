import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;


public class ContractEntData  {
    // tx uuid
    public Uint256 uuid;
    // token amount
    public Uint256 amount;
    // user address
    public Address userAddress;
    // nonce(on-chain retrieval)
    public Uint256 nonce;
    // singn timestamp
    public Uint256 deadline;

}


