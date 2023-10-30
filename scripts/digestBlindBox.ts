import * as ethersUtils from 'ethers';
import { pack } from "@ethersproject/solidity";
import { defaultAbiCoder } from "@ethersproject/abi";
import { ecsign } from 'ethereumjs-util'

export const PRIVATEKEY = "";

// Define the EIP712Domain struct type
export const DOMAINTYPE = 'EIP712Domain(string name,string version,uint256 chainId,address verifyingContract)'
// Calculate the hash of the domain struct type
export const PERMIT_DOMAINTYPE = ethersUtils.keccak256(ethersUtils.toUtf8Bytes(DOMAINTYPE));

// Define the contract function struct type
export const CONTRACTFUNCTION = 'claimToken(uint256 uuid, uint256 amount, address userAddress, uint256 nonce, uint deadline)'
// Calculate the hash of the contract function struct type
export const PERMIT_CONTRACTFUNCTION = ethersUtils.keccak256(ethersUtils.toUtf8Bytes(CONTRACTFUNCTION))

/**
 * Calculates the domain separator for EIP712.
 * @param name - The name of the domain.
 * @param tokenAddress - The address of the token contract.
 * @returns The domain separator.
 */
export function getDomainSeparator(name: string, tokenAddress: string) {
    // Encode the EIP712Domain struct
    const encodedDomain = defaultAbiCoder.encode(
        ['bytes32', 'bytes32', 'bytes32', 'uint256', 'address'],
        [
            PERMIT_DOMAINTYPE,
            ethersUtils.keccak256(ethersUtils.toUtf8Bytes(name)),
            ethersUtils.keccak256(ethersUtils.toUtf8Bytes('1')),
            1,
            tokenAddress,
        ]
    )

    // Calculate the domain separator by hashing the encoded domain
    const domainSeparator = ethersUtils.keccak256(encodedDomain);
    return domainSeparator;
}


/**
 * Calculates the UserClaimTokenDigest.
 * 
 * @param tokenName - The name of the token.
 * @param tokenAddress - The address of the token.
 * @param permit - An object containing permit details.
 * @returns The calculated UserClaimTokenDigest.
 */
export async function getUserClaimTokenDigest(
    tokenName: string,
    tokenAddress: string,
    permit: {
        uuid: string,
        amount: string,
        address: string,
        nonce: number,
        deadline: number,
    },
): Promise<string> {
    // Step 1: Get the DOMAIN_SEPARATOR
    const DOMAIN_SEPARATOR = getDomainSeparator(tokenName, tokenAddress);
    console.log(DOMAIN_SEPARATOR, "DOMAIN_SEPARATOR");

    // Step 2: Encode the permit details
    const encodedPermit = defaultAbiCoder.encode(
        ['bytes32', 'uint256', 'uint256', 'address', 'uint256', 'uint256'],
        [
            PERMIT_CONTRACTFUNCTION,
            permit.uuid,
            permit.amount,
            permit.address,
            permit.nonce,
            permit.deadline,
        ],
    );

    // Step 3: Get the keccak256 hash of the encoded permit details
    const permitHash = ethersUtils.keccak256(encodedPermit);
    console.log(PERMIT_CONTRACTFUNCTION);
    console.log(permitHash);

    // Step 4: Pack the values to be hashed
    const packedValues = pack(
        ['bytes1', 'bytes1', 'bytes32', 'bytes32'],
        [
            '0x19',
            '0x01',
            DOMAIN_SEPARATOR,
            permitHash,
        ],
    );
    console.log(packedValues);

    // Step 5: Get the keccak256 hash of the packed values
    const userClaimTokenDigest = ethersUtils.keccak256(packedValues);

    return userClaimTokenDigest;
}

/**
 * This function is the entry point of the program.
 * It calculates the digest using the getUserClaimTokenDigest function,
 * signs the digest using the ecsign function, and then logs the results.
 */
async function main() {
    // Calculate the digest using the getUserClaimTokenDigest function
    const digest = await getUserClaimTokenDigest(
        "ClaimToken",
        "0xddaAd340b0f1Ef65169Ae5E41A8b10776a75482d",
        {
            uuid: "123456789",
            amount: "1000000000",
            address: "0x10e3a183db48d854870feda31630bc1eb0ddd52a",
            nonce: 1,
            deadline: 1698591527
        },
    );

    // Log the digest
    console.log(digest);

    // Sign the digest using the ecsign function
    const { v, r, s } = ecsign(Buffer.from(digest.slice(2), 'hex'), Buffer.from(PRIVATEKEY.slice(2), 'hex'));

    // Log the signature components
    console.log(v);
    console.log("0x" + r.toString("hex"));
    console.log("0x" + s.toString("hex"));
}

// We recommend this pattern to be able to use async/await everywhere
// and properly handle errors.
main().catch((error) => {
    console.error(error);
    process.exitCode = 1;
});
