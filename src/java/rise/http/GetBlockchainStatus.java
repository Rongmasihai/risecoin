package rise.http;

import rise.Block;
import rise.BlockchainProcessor;
import rise.Rise;
import rise.peer.Peer;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetBlockchainStatus extends APIServlet.APIRequestHandler {

    static final GetBlockchainStatus instance = new GetBlockchainStatus();

    private GetBlockchainStatus() {
        super(new APITag[] {APITag.BLOCKS, APITag.INFO});
    }

    @Override
    JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        response.put("application", Rise.APPLICATION);
        response.put("version", Rise.VERSION);
        response.put("time", Rise.getEpochTime());
        Block lastBlock = Rise.getBlockchain().getLastBlock();
        response.put("lastBlock", lastBlock.getStringId());
        response.put("cumulativeDifficulty", lastBlock.getCumulativeDifficulty().toString());
        response.put("numberOfBlocks", lastBlock.getHeight() + 1);
        BlockchainProcessor blockchainProcessor = Rise.getBlockchainProcessor();
        Peer lastBlockchainFeeder = blockchainProcessor.getLastBlockchainFeeder();
        response.put("lastBlockchainFeeder", lastBlockchainFeeder == null ? null : lastBlockchainFeeder.getAnnouncedAddress());
        response.put("lastBlockchainFeederHeight", blockchainProcessor.getLastBlockchainFeederHeight());
        response.put("isScanning", blockchainProcessor.isScanning());
        return response;
    }

}
